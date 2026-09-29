package com.wild0408.nanxinshiguang.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcademicSummaryTest {
    @Test
    fun parsesPortalFieldsWithoutGradeServer() {
        val summary = parseAcademicSummary(
            """
            {
              "XH": "202600000001",
              "YXZXF": "160", "YHXF": "52.5", "PJJD": "3.45",
              "GPA": "3.7", "PJF": "86.5", "JQPJF": "87.2",
              "BJPM": "5", "ZYPM": "28", "KCTGL": "0.995", "XFJD": "32.9%"
            }
            """.trimIndent(),
            fetchedAt = 123L,
        )

        assertEquals("202600000001", summary?.studentId)
        assertEquals("3.45", summary?.averageGradePoint)
        assertEquals("87.2", summary?.weightedAverageScore)
        assertEquals("99.5%", summary?.passRateDisplay)
        assertEquals(0.329f, summary!!.creditProgressFraction!!, 0.00001f)
        assertEquals(123L, summary?.fetchedAt)
    }

    @Test
    fun treatsEmptyOrLoginResponseAsExpiredSession() {
        assertNull(parseAcademicSummary("", 1))
        assertNull(parseAcademicSummary("<html>login</html>", 1))
        assertNull(parseAcademicSummary("""{"data":{}}""", 1))
    }

    @Test
    fun fallsBackToCreditRatioWhenProgressIsMissing() {
        val summary = parseAcademicSummary("""{"XH":"202600000001","YHXF":"40","YXZXF":"160","KCTGL":"1"}""", 1)
        assertEquals(0.25f, summary?.creditProgressFraction)
        assertEquals("100%", summary?.passRateDisplay)
    }
}
