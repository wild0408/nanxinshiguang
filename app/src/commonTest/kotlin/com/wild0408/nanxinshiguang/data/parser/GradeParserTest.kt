package com.wild0408.nanxinshiguang.data.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class GradeParserTest {
    @Test
    fun rejectsSchoolSpecificRawRecord() {
        val records = parseGradeRecords(
            """
            [{
              "WID":"52EDE6CCEAF176C8E0633201A7C02B68",
              "XSKCM":"人工智能与科技前沿（科学广场）",
              "KCXZDM_DISPLAY":"通识(选)",
              "XF":2.0,
              "ZCJ":83.0,
              "XFJD":3.3,
              "XNXQDM":"2025-2026-2",
              "CXCKDM_DISPLAY":"初修",
              "KSLXDM_DISPLAY":"线上考试",
              "QMCJXS":"100"
            }]
            """.trimIndent()
        )

        assertEquals(0, records.size)
    }

    @Test
    fun parsesUnifiedScriptRecordAndSummarizesIt() {
        val records = parseGradeRecords(
            """
            {"records":[{
              "id":"course-1",
              "courseName":"数据结构",
              "courseType":"专业必修",
              "credits":"4.0",
              "score":"86",
              "gradePoint":"3.7",
              "semester":"2025-2026-2"
            }]}
            """.trimIndent()
        )

        val summary = summarizeGrades(records, "2025-2026-2")
        assertEquals("3.70", summary.gpa)
        assertEquals("86", summary.averageScore)
        assertEquals("4", summary.totalCredits)
        assertEquals(1, summary.courseCount)
        assertEquals(null, summary.ranking)
    }

    @Test
    fun parsesNuistEmapRows() {
        val records = parseEmapGradeRecords(
            """
            {
              "datas": {
                "xscjcx": {
                  "rows": [{
                    "WID": "row-1",
                    "XSKCM": "数据结构",
                    "KCXZDM_DISPLAY": "专业必修",
                    "XF": 4.0,
                    "ZCJ_DISPLAY": "86",
                    "XFJD": 3.7,
                    "XNXQDM": "2025-2026-2",
                    "CXCKDM_DISPLAY": "初修",
                    "SKJS": "教师甲",
                    "KSLXDM_DISPLAY": "线上考试",
                    "PSCJXS": 30,
                    "QMCJXS": 70
                  }]
                }
              }
            }
            """.trimIndent()
        )

        assertEquals(1, records.size)
        assertEquals("数据结构", records.single().courseName)
        assertEquals("专业必修", records.single().courseType)
        assertEquals("初修", records.single().status)
        assertEquals("平时 30% + 期末 70%", records.single().scoreComposition)
    }
}
