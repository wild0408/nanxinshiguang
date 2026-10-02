package com.wild0408.nanxinshiguang.tool

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 在真实设备/模拟器上验证"检查更新"的完整链路。
 *
 * 单测（androidUnitTest）跑在 JVM 上，`Build.SUPPORTED_ABIS` 为空、也没有真实 TLS 栈，
 * 因此无法覆盖"按设备 ABI 选包 + 真实网络"这一段；这里补上。
 *
 * 用例只打印结果、不断言具体版本号，避免每次发版失效；同时把结果打到 logcat，
 * 便于在无法读取测试输出时用 `adb logcat -d -s UpdateCheckIT` 查看。
 */
@RunWith(AndroidJUnit4::class)
class UpdateCheckInstrumentedTest {

    @Test
    fun checkUpdateReachesServerAndReportsStatus() = runBlocking {
        val status: UpdateStatus = UpdateChecker().checkUpdate(currentVersionName = "1.0.0")
        Log.i(TAG, "RESULT=$status")
        println("RESULT=$status")

        // 唯一硬性要求：不许是"两个通道都失败"的错误态。
        // 具体是 Latest 还是 Found 取决于当前发布版本，不做断言。
        if (status is UpdateStatus.Error) {
            throw AssertionError("检查更新失败：${status.message}")
        }
        assertNotNull(status)
    }

    private companion object {
        const val TAG = "UpdateCheckIT"
    }
}
