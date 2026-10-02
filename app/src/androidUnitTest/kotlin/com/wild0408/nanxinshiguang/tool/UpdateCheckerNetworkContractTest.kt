package com.wild0408.nanxinshiguang.tool

import io.ktor.client.call.body
import io.ktor.client.request.get
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

    @Test
    fun configuredClientReachesGitHubAndParsesRelease() = runBlocking {
        val response = UpdateChecker.defaultHttpClient.get(endpoint)
        assertEquals(200, response.status.value, "GitHub Releases 接口应返回 200")

        val release = response.body<ApiReleaseResponse>()
        assertTrue(release.tagName.isNotBlank(), "tag_name 不应为空")
        assertTrue(release.assets.any { it.name.endsWith(".apk") }, "发布资产中应包含 APK")
    }
}
