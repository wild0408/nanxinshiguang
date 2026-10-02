package com.wild0408.nanxinshiguang.tool

import io.ktor.client.call.body
import io.ktor.http.HttpHeaders
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 版本比较逻辑的确定性单测（不依赖网络）。
 */
class UpdateVersionCompareTest {

    private val checker = UpdateChecker()

    @Test
    fun detectsNewerVersionsAcrossSegments() {
        assertTrue(checker.isNewerVersion("1.0.4", "1.0.3"))
        assertTrue(checker.isNewerVersion("1.1.0", "1.0.9"))
        assertTrue(checker.isNewerVersion("2.0.0", "1.99.99"))
        // 位数不同也要正确比较：1.0.10 大于 1.0.9（按字符串比较会判错）
        assertTrue(checker.isNewerVersion("1.0.10", "1.0.9"))
        // 缺少的分段按 0 处理
        assertTrue(checker.isNewerVersion("1.1", "1.0.9"))
    }

    @Test
    fun treatsSameOrOlderVersionsAsLatest() {
        assertFalse(checker.isNewerVersion("1.0.4", "1.0.4"))
        assertFalse(checker.isNewerVersion("1.0.3", "1.0.4"))
        // 1.0.4-rc1 -> [1,0,4,1] 会被判为比 [1,0,4] 新，与既有实现保持一致
        assertFalse(checker.isNewerVersion("1.0.3-rc1", "1.0.4"))
    }

    @Test
    fun ignoresNonNumericSegmentsInsteadOfCrashing() {
        // 标签前缀由调用方剥离；这里验证纯比较不会因非法段抛异常
        assertTrue(checker.isNewerVersion("1.0.5-rc1", "1.0.4"))
        assertFalse(checker.isNewerVersion("v", "1.0.4"))
    }
}

/**
 * 检查更新的网络契约测试（**需要联网**）。
 *
 * 背景：设备端出现"远程数据异常"时，旧实现把网络不可达、HTTP 403、限流、反序列化失败
 * 统统吞成同一句提示，无法定位。这里固定住"配置正确时接口可达且可解析"这一契约。
 * 断网环境下该用例会失败，属于预期：它验证的就是网络契约本身。
 *
 * 注意：不断言具体版本号，否则每次发版都会失效。
 */
class UpdateCheckerNetworkContractTest {

    private val endpoint = "https://api.github.com/repos/wild0408/nanxinshiguang/releases/latest"
    private val webEndpoint = "https://github.com/wild0408/nanxinshiguang/releases.atom"

    /**
     * api.github.com 通道的契约。
     * 注意：该接口的匿名额度是**按出口 IP 共享的 60 次/小时**，跑测试很容易触顶并返回 403，
     * 这是环境限制而非接口故障，因此这里把 403 作为可接受结果（并说明此时应走 atom 回退通道）。
     */
    @Test
    fun configuredClientReachesGitHubApiUnlessRateLimited() = runBlocking {
        val response = UpdateChecker.defaultHttpClient.get(endpoint)
        assertTrue(
            response.status.value == 200 || response.status.value == 403,
            "预期 200，或被匿名限流时的 403，实际 ${response.status.value}",
        )
        if (response.status.value != 200) {
            println("api.github.com 被限流（HTTP 403），本轮依赖 github.com 回退通道")
            return@runBlocking
        }
        val release = response.body<ApiReleaseResponse>()
        assertTrue(release.tagName.isNotBlank(), "tag_name 不应为空")
        assertTrue(release.assets.any { it.name.endsWith(".apk") }, "发布资产中应包含 APK")
    }

    /**
     * 回退通道的契约：只依赖 github.com 的 releases.atom 订阅源即可拿到最新 tag。
     * 这是"api.github.com 不可达时仍能检查更新"的基础。
     */
    @Test
    fun webFeedChannelYieldsParsableTag() = runBlocking {
        val response = UpdateChecker.defaultHttpClient.get(webEndpoint)
        assertEquals(200, response.status.value, "github.com/releases.atom 应返回 200")
        val tag = UpdateChecker().parseTagFromAtomFeed(response.bodyAsText())
        assertTrue(tag != null && tag.startsWith("v"), "订阅源应能解析出 vX.Y.Z 形式的 tag，实际：$tag")
    }

    /**
     * 回退通道的最后一环：按命名规则拼出的直链必须真实可下。
     * 这里固定用 arm64-v8a 探测，避免依赖单测环境里为空的 Build.SUPPORTED_ABIS。
     */
    @Test
    fun constructedAssetUrlIsDownloadable() = runBlocking {
        val feed = UpdateChecker.defaultHttpClient.get(webEndpoint).bodyAsText()
        val tag = UpdateChecker().parseTagFromAtomFeed(feed)
        assertTrue(tag != null, "订阅源应能取得 tag")

        val asset = UpdateChecker()
            .buildReleaseAssetUrls(tag!!)
            .first { it.name.contains("arm64-v8a") }
        // 用 Range 请求确认资源存在，不下载整个 APK
        val probe = UpdateChecker.defaultHttpClient.get(asset.downloadUrl) {
            header(HttpHeaders.Range, "bytes=0-0")
        }
        assertTrue(
            probe.status.value in 200..299,
            "构造的直链应可下载，实际 HTTP ${probe.status.value}：${asset.downloadUrl}",
        )
    }
}


/**
 * 回退通道的纯函数单测（不依赖网络）。
 */
class UpdateFallbackHelpersTest {

    private val checker = UpdateChecker()

    @Test
    fun extractsNewestTagFromAtomFeed() {
        val feed = """
            <feed xmlns="http://www.w3.org/2005/Atom">
              <link type="text/html" rel="alternate" href="https://github.com/o/r/releases"/>
              <entry>
                <title>v1.0.5</title>
                <link rel="alternate" type="text/html" href="https://github.com/o/r/releases/tag/v1.0.5"/>
              </entry>
              <entry>
                <title>v1.0.4</title>
                <link rel="alternate" type="text/html" href="https://github.com/o/r/releases/tag/v1.0.4"/>
              </entry>
            </feed>
        """.trimIndent()
        // 订阅源倒序，取到的必须是第一条（最新）
        assertEquals("v1.0.5", checker.parseTagFromAtomFeed(feed))
    }

    @Test
    fun rejectsFeedWithoutReleaseTag() {
        assertEquals(null, checker.parseTagFromAtomFeed(""))
        assertEquals(null, checker.parseTagFromAtomFeed("<feed></feed>"))
        assertEquals(null, checker.parseTagFromAtomFeed("https://github.com/o/r/releases"))
        assertEquals(null, checker.parseTagFromAtomFeed("<link href=\"https://github.com/o/r/releases/tag/\"/>"))
    }

    @Test
    fun buildsAssetUrlsForEverySupportedAbi() {
        val assets = checker.buildReleaseAssetUrls("v1.0.5")
        assertEquals(3, assets.size)
        assertEquals(
            listOf(
                "nanxinshiguang-v1.0.5-arm64-v8a-release.apk",
                "nanxinshiguang-v1.0.5-armeabi-v7a-release.apk",
                "nanxinshiguang-v1.0.5-x86_64-release.apk",
            ),
            assets.map { it.name },
        )
        assets.forEach { asset ->
            assertEquals(
                "https://github.com/wild0408/nanxinshiguang/releases/download/v1.0.5/${asset.name}",
                asset.downloadUrl,
            )
        }
    }

    @Test
    fun toleratesTagWithoutVPrefix() {
        val assets = checker.buildReleaseAssetUrls("1.0.5")
        // 文件名里用去掉前缀的版本号，URL 路径保留原 tag
        assertTrue(assets.first().name == "nanxinshiguang-v1.0.5-arm64-v8a-release.apk")
        assertTrue(assets.first().downloadUrl.contains("/releases/download/1.0.5/"))
    }
}
