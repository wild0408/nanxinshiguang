package com.wild0408.nanxinshiguang.data.model

import com.wild0408.nanxinshiguang.data.api.quote.HitokotoResponse
import com.wild0408.nanxinshiguang.data.api.quote.toDailyQuote
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 「每日一言」的署名整理与接口字段解析。 */
class DailyQuoteTest {

    @Test
    fun formatsSourceWithAuthorAndWork() {
        assertEquals("——孔子《论语》", formatQuoteSource("论语", "孔子"))
        // 接口的 from 有时自带书名号，不应再补一层
        assertEquals("——孔子《论语》", formatQuoteSource("《论语》", "孔子"))
    }

    @Test
    fun formatsSourceWithOnlyOneSide() {
        assertEquals("——《论语》", formatQuoteSource("《论语》", null))
        assertEquals("——《论语》", formatQuoteSource("《论语》", "   "))
        // 缺书名号时补上，保证同一位置写法一致
        assertEquals("——《礼记·中庸》", formatQuoteSource("礼记·中庸", null))
        assertEquals("——孔子", formatQuoteSource(null, "孔子"))
        assertEquals("——孔子", formatQuoteSource("", "孔子"))
        assertNull(formatQuoteSource(null, null), "两侧都为空时不应给出署名")
        assertNull(formatQuoteSource("  ", "  "), "全空白同样视为没有署名")
    }

    @Test
    fun parsesHitokotoResponse() {
        val quote = HitokotoResponse(
            text = "博学之，审问之，慎思之，明辨之，笃行之。",
            from = "礼记·中庸",
            fromWho = null,
        ).toDailyQuote()
        assertEquals("博学之，审问之，慎思之，明辨之，笃行之。", quote?.text)
        assertEquals("——《礼记·中庸》", quote?.source)
    }

    @Test
    fun trimsWhitespaceFromApiFields() {
        val quote = HitokotoResponse(text = "  路漫漫其修远兮  ", from = " 离骚 ", fromWho = "屈原 ")
            .toDailyQuote()
        assertEquals("路漫漫其修远兮", quote?.text)
        assertEquals("——屈原《离骚》", quote?.source)
    }

    @Test
    fun rejectsEmptyQuoteText() {
        assertNull(HitokotoResponse(text = "").toDailyQuote(), "正文为空时不应生成卡片内容")
        assertNull(HitokotoResponse(text = "   ").toDailyQuote(), "正文全空白同样不生成")
    }

    @Test
    fun missingSourceDoesNotProduceDashes() {
        val quote = HitokotoResponse(text = "今天也要加油。").toDailyQuote()
        assertEquals("今天也要加油。", quote?.text)
        assertNull(quote?.source, "没有出处时不应显示空的署名行")
    }
}
