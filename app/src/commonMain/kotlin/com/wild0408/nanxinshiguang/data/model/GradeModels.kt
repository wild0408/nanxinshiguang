package com.wild0408.nanxinshiguang.data.model

data class GradeRecord(
    val id: String,
    val courseName: String,
    val courseType: String,
    val credits: String,
    val score: String,
    val gradePoint: String,
    val semester: String,
    val status: String? = null,
    val teacher: String? = null,
    val examType: String? = null,
    val scoreComposition: String? = null
)

data class GradeSemesterSummary(
    val semester: String,
    val gpa: String,
    val totalCredits: String,
    val courseCount: Int,
    val ranking: Int? = null,
    val totalStudents: Int? = null,
    val passedCount: Int = 0,
    val averageScore: String = "--",
)

const val ALL_GRADE_SEMESTERS = "__ALL_SEMESTERS__"

fun latestGradeSemester(semesters: Iterable<String>): String? =
    semesters.filter { it.isNotBlank() }.maxByOrNull(::gradeSemesterSortKey)

fun orderedGradeSemesters(semesters: Iterable<String>): List<String> =
    semesters.filter { it.isNotBlank() }.distinct().sortedByDescending(::gradeSemesterSortKey)

fun summarizeGradeRecords(records: List<GradeRecord>, semester: String): GradeSemesterSummary {
    val selected = records.filter { it.semester == semester }
    return summarizeSelectedGradeRecords(selected, semester)
}

private fun summarizeSelectedGradeRecords(
    selected: List<GradeRecord>,
    semester: String,
): GradeSemesterSummary {
    val credits = selected.mapNotNull { it.credits.toDoubleOrNull() }.sum()
    val creditPairs = selected.mapNotNull { record ->
        val courseCredits = record.credits.toDoubleOrNull()
        val point = record.gradePoint.toDoubleOrNull()
        if (courseCredits != null && point != null) courseCredits to point else null
    }
    val totalCredits = creditPairs.sumOf { it.first }
    val gpa = if (totalCredits > 0.0) {
        creditPairs.sumOf { it.first * it.second } / totalCredits
    } else null
    val averageScore = selected.mapNotNull { it.score.toGradeNumber() }
        .takeIf { it.isNotEmpty() }
        ?.average()
    return GradeSemesterSummary(
        semester = semester,
        gpa = gpa.formatGradeNumber(),
        averageScore = averageScore.formatGradeNumber(),
        totalCredits = credits.formatGradeNumber(),
        courseCount = selected.size,
        passedCount = selected.count(GradeRecord::isPassed),
    )
}

fun summarizeAllGradeRecords(records: List<GradeRecord>): GradeSemesterSummary? {
    if (records.isEmpty()) return null
    return summarizeSelectedGradeRecords(records, ALL_GRADE_SEMESTERS)
}

private fun gradeSemesterSortKey(semester: String): String =
    Regex("\\d+").findAll(semester)
        .map { it.value.padStart(6, '0') }
        .joinToString("")
        .ifBlank { semester }

private fun String.toGradeNumber(): Double? =
    replace("分", "").trim().toDoubleOrNull()

private fun Double?.formatGradeNumber(): String =
    this?.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.2f".format(it) } ?: "--"

fun GradeRecord.isPassed(): Boolean {
    val statusText = status.orEmpty()
    if (statusText.contains("不及格") ||
        statusText.contains("不合格") ||
        statusText.contains("不通过") ||
        statusText == "否"
    ) {
        return false
    }
    if (statusText.contains("及格") ||
        statusText.contains("合格") ||
        statusText.contains("通过") ||
        statusText == "是"
    ) {
        return true
    }
    val numericScore = score
        .replace("分", "")
        .trim()
        .toDoubleOrNull()
    return numericScore != null && numericScore >= 60.0 ||
        when (score.trim()) {
            "优秀", "优" -> true
            "良好", "良" -> true
            "中等", "中" -> true
            "及格", "合格", "通过" -> true
            else -> false
        }
}
