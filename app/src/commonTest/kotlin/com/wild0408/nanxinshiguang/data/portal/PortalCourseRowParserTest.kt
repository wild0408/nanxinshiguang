package com.wild0408.nanxinshiguang.data.portal

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PortalCourseRowParserTest {
    private fun row(json: String) = parsePortalCourseRow(Json.parseToJsonElement(json))

    @Test
    fun parsesFullRowWithDisplayFields() {
        val parsed = row(
            """
            {
              "KCM_DISPLAY": "数据结构",
              "SKXQ": "3",
              "KSJC": "1",
              "JSJC": "2",
              "SKZC": "1010100000000000000",
              "JASMC_DISPLAY": "文德楼 N203",
              "XXXQDM_DISPLAY": "中苑",
              "SKJS_DISPLAY": "张三/李四"
            }
            """.trimIndent()
        )!!

        assertEquals("数据结构", parsed.name)
        assertEquals(3, parsed.day)
        assertEquals(1, parsed.startSection)
        assertEquals(2, parsed.endSection)
        assertEquals(listOf(1, 3, 5), parsed.weeks)
        assertEquals("文德楼 N203（中苑）", parsed.position)
        assertEquals("张三", parsed.teacher)
    }

    @Test
    fun dropsRowWithoutCourseName() {
        assertNull(row("""{"SKXQ":"3","KSJC":"1","JSJC":"2","SKZC":"1"}"""))
    }

    @Test
    fun dropsRowWithOutOfRangeWeekday() {
        assertNull(row("""{"KCM":"A","SKXQ":"0","KSJC":"1","JSJC":"2","SKZC":"1"}"""))
        assertNull(row("""{"KCM":"A","SKXQ":"8","KSJC":"1","JSJC":"2","SKZC":"1"}"""))
    }

    @Test
    fun dropsRowWithInvalidSections() {
        assertNull(row("""{"KCM":"A","SKXQ":"1","KSJC":"0","JSJC":"2","SKZC":"1"}"""))
        assertNull(row("""{"KCM":"A","SKXQ":"1","KSJC":"3","JSJC":"2","SKZC":"1"}"""))
        assertNull(row("""{"KCM":"A","SKXQ":"1","KSJC":"1","JSJC":"x","SKZC":"1"}"""))
    }

    @Test
    fun dropsRowWithoutAnyActiveWeek() {
        assertNull(row("""{"KCM":"A","SKXQ":"1","KSJC":"1","JSJC":"2","SKZC":"0000"}"""))
        assertNull(row("""{"KCM":"A","SKXQ":"1","KSJC":"1","JSJC":"2"}"""))
    }

    @Test
    fun fallsBackForMissingRoomCampusAndTeacher() {
        val parsed = row("""{"KCM":"A","SKXQ":"1","KSJC":"1","JSJC":"2","SKZC":"1"}""")!!
        assertEquals("待定", parsed.position)
        assertEquals("未知", parsed.teacher)
    }

    @Test
    fun rejectsNonObjectElement() {
        assertNull(parsePortalCourseRow(Json.parseToJsonElement("""[1,2,3]""")))
        assertNull(parsePortalCourseRow(Json.parseToJsonElement(""""just a string"""")))
    }

    @Test
    fun extractsSectionRowsAndReturnsNullWhenMissing() {
        val root = Json.parseToJsonElement(
            """{"datas":{"kbxx":{"rows":[{"KCM":"A"}]}}}"""
        )
        assertEquals(1, root.let { (it as kotlinx.serialization.json.JsonObject) }.portalSectionRows("kbxx")!!.size)
        assertEquals(null, (root as kotlinx.serialization.json.JsonObject).portalSectionRows("nope"))
    }
}
