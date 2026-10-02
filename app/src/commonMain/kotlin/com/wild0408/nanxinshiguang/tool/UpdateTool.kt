package com.wild0408.nanxinshiguang.tool

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

sealed class UpdateStatus {
    data class Found(
        val versionName: String,
        val changelog: String,
        val targetUrl: String,
        val isDirectDownload: Boolean
    ) : UpdateStatus()

    data class Latest(val currentVersion: String) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
    data object NotSupported : UpdateStatus()
    data object Checking : UpdateStatus()
    data object Idle : UpdateStatus()
}

@Serializable
data class ApiReleaseResponse(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("assets") val assets: List<ApiAsset> = emptyList()
)

@Serializable
data class ApiAsset(
    @SerialName("name") val name: String = "",
    @SerialName("browser_download_url") val downloadUrl: String = ""
)

expect object PlatformUpdateStrategy {
    val isUpdateSupported: Boolean
    fun parseTargetUrl(response: ApiReleaseResponse): String?
    fun openUrl(url: String)
}

/**
 * 添加 @Single 注解，SharedModule 的 @ComponentScan 会自动发现并注册为单例
 */
@Single
class UpdateChecker(
    private val httpClient: HttpClient = defaultHttpClient
) {
    companion object {
        private const val GITHUB_REPO = "wild0408/nanxinshiguang"

        /** GitHub API 强制要求携带 User-Agent，缺失会直接返回 403。 */
        private const val USER_AGENT = "nanxinshiguang-android"

        private const val TAG = "UpdateChecker"

        /** 这些包装异常本身不含信息，展示时跳过，往下找更具体的原因。 */
        private val GENERIC_EXCEPTION_NAMES = setOf("Exception", "IOException", "RuntimeException")

        /**
         * 通道级超时。此前设的 8s 在慢网络/代理下会误判失败（实测模拟器偶发瞬时卡顿即超时），
         * 但也不能不设：网络被黑洞时界面会一直挂在"正在检查更新"。
         */
        private const val CONNECT_TIMEOUT_MS = 15_000L
        private const val REQUEST_TIMEOUT_MS = 30_000L
        private const val SOCKET_TIMEOUT_MS = 30_000L

        val defaultHttpClient by lazy {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    })
                }
                // Ktor CIO 默认会带 User-Agent；这里显式声明，避免更换引擎后因缺头被 GitHub
                // 以 403 "Request forbidden by administrative rules" 拒绝。
                defaultRequest {
                    header(HttpHeaders.UserAgent, USER_AGENT)
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                }
                // 此前完全没有超时：网络不可达（例如 api.github.com 被阻断）时会长时间挂起，
                // 用户只看到一直"正在检查更新"。这里给一个明确的失败上限，但取值要容忍
                // 慢网络与代理（8s 实测会误判，见 CONNECT_TIMEOUT_MS 的说明）。
                install(HttpTimeout) {
                    connectTimeoutMillis = CONNECT_TIMEOUT_MS
                    requestTimeoutMillis = REQUEST_TIMEOUT_MS
                    socketTimeoutMillis = SOCKET_TIMEOUT_MS
                }
            }
        }
    }

    suspend fun checkUpdate(currentVersionName: String): UpdateStatus = withContext(Dispatchers.IO) {
        if (!PlatformUpdateStrategy.isUpdateSupported) {
            return@withContext UpdateStatus.NotSupported
        }

        // 两个通道并行发起：api.github.com 能带出更新日志与真实资产地址，但它的匿名额度是
        // **按出口 IP 共享的 60 次/小时**（校园网这类共享网络极易触顶）；github.com 的
        // releases.atom 不受该限制。并行可避免"主通道超时后才开始回退"的额外等待。
        return@withContext coroutineScope {
            val apiDeferred = async { runCatching { fetchViaApi() } }
            val feedDeferred = async { runCatching { fetchViaWebFeed() } }

            val viaApi = apiDeferred.await()
            viaApi.getOrNull()?.let { release ->
                if (release.tagName.isNotBlank()) {
                    return@coroutineScope evaluate(release, currentVersionName)
                }
            }
            viaApi.exceptionOrNull()?.let { error ->
                AppLog.e(TAG, "api.github.com 通道失败: ${error::class.simpleName}", error)
            }

            val viaWeb = feedDeferred.await()
            viaWeb.getOrNull()?.let { release ->
                AppLog.d(TAG, "已通过 github.com 回退通道取得最新版本 ${release.tagName}")
                return@coroutineScope evaluate(release, currentVersionName)
            }
            viaWeb.exceptionOrNull()?.let { error ->
                AppLog.e(TAG, "github.com 回退通道失败: ${error::class.simpleName}", error)
            }

            UpdateStatus.Error(
                // 把失败原因一并显示：出现 TLS/证书类问题时，"无法连接服务器"这句话本身
                // 完全无法定位（曾因 network_security_config 的 domain-config 触发
                // CertificateException，而界面只提示无法连接）。细节仍在 logcat。
                buildString {
                    append("无法连接更新服务器")
                    val detail = listOfNotNull(shortReason(viaApi), shortReason(viaWeb))
                        .distinct()
                        .joinToString("/")
                    if (detail.isNotEmpty()) append("（").append(detail).append("）")
                    append("，请检查网络后重试（也可到 GitHub Releases 页面手动下载）")
                }
            )
        }
    }

    /**
     * 取失败原因的简短标识（异常类名或通道自身的说明），用于界面提示。
     * 会沿 cause 链找第一个有信息量的类型，避免只显示外层包装异常。
     */
    private fun shortReason(result: Result<*>): String? {
        val error = result.exceptionOrNull() ?: return null
        if (error is UpdateChannelException) return error.message
        return generateSequence(error as Throwable?) { it.cause }
            .take(6)
            .mapNotNull { it::class.simpleName }
            .firstOrNull { it !in GENERIC_EXCEPTION_NAMES }
    }

    /** 主通道：api.github.com。非 2xx 或缺字段都视为该通道失败，交给回退通道处理。 */
    private suspend fun fetchViaApi(): ApiReleaseResponse {
        val response: HttpResponse = httpClient.get("https://api.github.com/repos/$GITHUB_REPO/releases/latest") {
            // 比客户端默认更短：api.github.com 不可达时要尽快让回退通道接手。
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MS; connectTimeoutMillis = CONNECT_TIMEOUT_MS }
        }
        if (!response.status.isSuccess()) {
            throw UpdateChannelException("api.github.com HTTP ${response.status.value}")
        }
        val release = response.body<ApiReleaseResponse>()
        if (release.tagName.isBlank()) {
            throw UpdateChannelException("api.github.com 返回缺少 tag_name")
        }
        return release
    }

    /**
     * 回退通道：只依赖 github.com。
     * 读取 `releases.atom` 订阅源（纯 github.com 域名，无需 API 鉴权），取其中最新的
     * `/releases/tag/<tag>`；再按 `nanxinshiguang-v<version>-<abi>-release.apk` 的命名规则
     * 拼出当前设备 ABI 的直链，并做一次 Range 探测确认资产真实存在，避免把用户送到 404。
     */
    private suspend fun fetchViaWebFeed(): ApiReleaseResponse {
        val response = httpClient.get("https://github.com/$GITHUB_REPO/releases.atom") {
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MS; connectTimeoutMillis = CONNECT_TIMEOUT_MS }
        }
        if (!response.status.isSuccess()) {
            throw UpdateChannelException("github.com/releases.atom HTTP ${response.status.value}")
        }
        val tag = parseTagFromAtomFeed(response.bodyAsText())
            ?: throw UpdateChannelException("订阅源中未找到 release tag")

        val assets = buildReleaseAssetUrls(tag)
        val candidate = PlatformUpdateStrategy.parseTargetUrl(ApiReleaseResponse(tagName = tag, assets = assets))
            ?: throw UpdateChannelException("无法为当前设备 ABI 构造下载地址")

        val probe = httpClient.get(candidate) {
            header(HttpHeaders.Range, "bytes=0-0")
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MS; connectTimeoutMillis = CONNECT_TIMEOUT_MS }
        }
        if (probe.status.value !in 200..299) {
            throw UpdateChannelException("构造的下载地址不可用（HTTP ${probe.status.value}）")
        }
        return ApiReleaseResponse(tagName = tag, assets = assets)
    }

    /** 比较版本并组装结果；两个通道拿到数据后走同一套判断。 */
    private fun evaluate(release: ApiReleaseResponse, currentVersionName: String): UpdateStatus {
        val latestVersion = release.tagName.removePrefix("v").removePrefix("V").trim()
        val currentVersion = currentVersionName.removePrefix("v").removePrefix("V").trim()
        if (!isNewerVersion(latestVersion, currentVersion)) {
            return UpdateStatus.Latest(currentVersionName)
        }
        val directUrl = PlatformUpdateStrategy.parseTargetUrl(release)
        // 回退通道（releases.atom）拿不到更新日志，给一句说明总比空白框好。
        val changelog = release.body.ifBlank { "（未能获取更新说明，可在发布页面查看）" }
        return if (!directUrl.isNullOrEmpty()) {
            UpdateStatus.Found(
                versionName = release.tagName,
                changelog = changelog,
                targetUrl = directUrl,
                isDirectDownload = true,
            )
        } else {
            UpdateStatus.Found(
                versionName = release.tagName,
                changelog = changelog,
                targetUrl = "https://github.com/$GITHUB_REPO/releases/tag/${release.tagName}",
                isDirectDownload = false,
            )
        }
    }

    /**
     * 从 GitHub 的 `releases.atom` 订阅源里取出最新 release 的 tag。
     * 订阅源按时间倒序，第一条 `/releases/tag/<tag>` 即最新版本。
     */
    internal fun parseTagFromAtomFeed(feed: String): String? {
        val match = Regex("""/releases/tag/([^"'<>\s]+)""").find(feed) ?: return null
        return match.groupValues[1].trim().takeIf { it.isNotBlank() }
    }

    /**
     * 按发布资产命名规则（`nanxinshiguang-v<version>-<abi>-release.apk`）构造各 ABI 的直链。
     * 回退通道拿不到 API 的 assets 列表，只能靠约定拼出来，因此调用方还要探测一次真实存在性。
     */
    internal fun buildReleaseAssetUrls(tag: String): List<ApiAsset> {
        val version = tag.removePrefix("v").removePrefix("V").trim()
        return listOf("arm64-v8a", "armeabi-v7a", "x86_64").map { abi ->
            val fileName = "nanxinshiguang-v$version-$abi-release.apk"
            ApiAsset(
                name = fileName,
                downloadUrl = "https://github.com/$GITHUB_REPO/releases/download/$tag/$fileName",
            )
        }
    }

    /**
     * 比较版本号：判断 latest 是否大于 current。
     * 标为 internal 以便单元测试覆盖分段比较的边界。
     */
    internal fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split('.', '-').mapNotNull { it.toIntOrNull() }
        val currentParts = current.split('.', '-').mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun launchUpdate(targetUrl: String) {
        PlatformUpdateStrategy.openUrl(targetUrl)
    }
}

/** 单个更新通道不可用；用于触发通道回退，不直接暴露给界面。 */
internal class UpdateChannelException(message: String) : Exception(message)
