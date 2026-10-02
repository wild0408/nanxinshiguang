package com.wild0408.nanxinshiguang.data.api.quote

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/** 在真实设备/模拟器上验证一言接口可达且能解析（走 Ktor CIO + 真实 TLS）。 */
@RunWith(AndroidJUnit4::class)
class DailyQuoteApiInstrumentedTest {

    @Test
    fun fetchReturnsParsableQuote() = runBlocking {
        // 第三方服务偶发变慢（实测出现过 15s 超时，而同一时刻本机请求 0.6s 返回），
        // 因此最多尝试两次再判定失败，避免把环境抖动当成回归。
        var result: Result<com.wild0408.nanxinshiguang.data.model.DailyQuote?> =
            runCatching { createDailyQuoteApi().fetch() }
        if (result.getOrNull() == null) {
            Log.i("DailyQuoteApi", "首次未取到内容（${result.exceptionOrNull()}），重试一次")
            result = runCatching { createDailyQuoteApi().fetch() }
        }
        Log.i("DailyQuoteApi", "RESULT=${result.getOrNull()} ERROR=${result.exceptionOrNull()}")
        result.exceptionOrNull()?.printStackTrace()
        assertNotNull("一言接口应能取到内容：${result.exceptionOrNull()}", result.getOrNull())
    }
}
