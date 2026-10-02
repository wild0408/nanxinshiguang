package com.wild0408.nanxinshiguang.tool

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
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
                // 用户只看到一直"正在检查更新"。这里给一个能快速失败的明确上限。
                install(HttpTimeout) {
                    connectTimeoutMillis = 10_000
                    requestTimeoutMillis = 15_000
                    socketTimeoutMillis = 15_000
                }
            }
        }
    }

    suspend fun checkUpdate(currentVersionName: String): UpdateStatus = withContext(Dispatchers.IO) {
        if (!PlatformUpdateStrategy.isUpdateSupported) {
            return@withContext UpdateStatus.NotSupported
        }

        try {
            val response: HttpResponse = httpClient.get("https://api.github.com/repos/$GITHUB_REPO/releases/latest")

            // 先判状态码：403/429 是接口访问受限（缺 User-Agent 或触发限流），404 是仓库/发布不存在，
            // 这些都不该被笼统地报成"远程数据异常"。
            if (!response.status.isSuccess()) {
                AppLog.e(TAG, "检查更新失败：HTTP ${response.status.value}")
                return@withContext when (response.status.value) {
                    403, 429 -> UpdateStatus.Error("GitHub 接口访问受限（HTTP ${response.status.value}），请稍后重试")
                    404 -> UpdateStatus.Error("未找到发布信息（HTTP 404）")
                    else -> UpdateStatus.Error("检查更新失败：HTTP ${response.status.value}")
                }
            }

            val release = response.body<ApiReleaseResponse>()

            if (release.tagName.isBlank()) {
                AppLog.e(TAG, "发布信息缺少 tag_name")
                return@withContext UpdateStatus.Error("发布信息不完整，请稍后重试")
            }

            val latestVersion = release.tagName.removePrefix("v").removePrefix("V").trim()
            val currentVersion = currentVersionName.removePrefix("v").removePrefix("V").trim()

            if (!isNewerVersion(latestVersion, currentVersion)) {
                return@withContext UpdateStatus.Latest(currentVersionName)
            }

            val downloadUrl = PlatformUpdateStrategy.parseTargetUrl(release)

            val (targetUrl, isDirectDownload) = if (!downloadUrl.isNullOrEmpty()) {
                Pair(downloadUrl, true)
            } else {
                val fallbackTagUrl = "https://github.com/$GITHUB_REPO/releases/tag/${release.tagName}"
                Pair(fallbackTagUrl, false)
            }

            UpdateStatus.Found(
                versionName = release.tagName,
                changelog = release.body,
                targetUrl = targetUrl,
                isDirectDownload = isDirectDownload
            )

        } catch (e: Exception) {
            AppLog.e(TAG, "检查更新失败: ${e::class.simpleName}", e)
            UpdateStatus.Error("检查更新失败，请检查网络后重试")
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
