package com.wild0408.nanxinshiguang.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GradeModelsTest {
    @Test
    fun defaultsToLatestAvailableSemesterRegardlessOfRecordOrder() {
        val semesters = listOf("2024-2025-2", "2025-2026-1", "2025-2026-2", "2025-2026-1")

        assertEquals("2025-2026-2", latestGradeSemester(semesters))
        assertEquals(listOf("2025-2026-2", "2025-2026-1", "2024-2025-2"), orderedGradeSemesters(semesters))
        assertNull(latestGradeSemester(emptyList()))
    }

    @Test
    fun averageUsesNumericScoresOnlyAndRespectsSemester() {
        val records = listOf(
            record("older", "2024-2025-2", "100", "2"),
            record("first", "2025-2026-1", "80", "4"),
            record("second", "2025-2026-1", "90分", "2"),
            record("text", "2025-2026-1", "优秀", "1"),
        )

        val semester = summarizeGradeRecords(records, "2025-2026-1")
        assertEquals("85", semester.averageScore)
        assertEquals(3, semester.courseCount)
        assertEquals("7", semester.totalCredits)
        assertEquals("90", summarizeAllGradeRecords(records)?.averageScore)
        assertEquals("--", summarizeGradeRecords(listOf(records.last()), "2025-2026-1").averageScore)
    }

    private fun record(id: String, semester: String, score: String, credits: String) = GradeRecord(
        id = id,
        courseName = id,
        courseType = "test",
        credits = credits,
        score = score,
        gradePoint = "3",
        semester = semester,
    )
}
