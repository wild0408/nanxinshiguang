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
        val result = runCatching { createDailyQuoteApi().fetch() }
        Log.i("DailyQuoteApi", "RESULT=${result.getOrNull()} ERROR=${result.exceptionOrNull()}")
        result.exceptionOrNull()?.printStackTrace()
        assertNotNull("一言接口应能取到内容：${result.exceptionOrNull()}", result.getOrNull())
    }
}
