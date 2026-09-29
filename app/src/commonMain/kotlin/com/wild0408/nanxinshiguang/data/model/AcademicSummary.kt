package com.wild0408.nanxinshiguang.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Instant

@Serializable
data class AcademicSummary(
    val studentId: String,
    val requiredCredits: String,
    val earnedCredits: String,
    val averageGradePoint: String,
    val gpa: String,
    val averageScore: String,
    val weightedAverageScore: String,
    val classRank: String,
    val majorRank: String,
    val passRate: String,
    val creditProgress: String,
    val fetchedAt: Long,
) {
    val passRateDisplay: String
        get() {
            val value = passRate.trim()
            val ratio = value.toDoubleOrNull() ?: return value.ifBlank { "--" }
            if (!ratio.isFinite()) return value
            val percent = (ratio * 1000).roundToInt() / 10.0
            return "${percent.toString().removeSuffix(".0")}%"
        }

    val creditProgressFraction: Float?
        get() {
            val percentage = creditProgress.trim()
            if (percentage.endsWith("%")) {
                percentage.dropLast(1).toFloatOrNull()?.takeIf(Float::isFinite)
                    ?.let { return (it / 100f).coerceIn(0f, 1f) }
            }
            val earned = earnedCredits.toDoubleOrNull()
            val required = requiredCredits.toDoubleOrNull()
            return if (earned != null && earned.isFinite() && required != null &&
                required.isFinite() && required > 0) {
                (earned / required).toFloat().coerceIn(0f, 1f)
            } else null
        }
}

fun AcademicSummary.updatedAtText(): String =
    Instant.fromEpochMilliseconds(fetchedAt)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toString().replace('T', ' ').take(16)

/**
 * The portal returns a flat object, not the EMAP grade list. An empty body or
 * HTML login page is not a valid summary even when its HTTP status is 200.
 */
fun parseAcademicSummary(body: String, fetchedAt: Long): AcademicSummary? {
    val data = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull() ?: return null
    fun JsonObject.field(key: String): String =
        this[key]?.takeUnless { it is JsonNull }
            ?.let { runCatching { it.jsonPrimitive.content.trim() }.getOrNull() }.orEmpty()
    val studentId = data.field("XH").takeIf(String::isNotBlank) ?: return null
    return AcademicSummary(
        studentId = studentId,
        requiredCredits = data.field("YXZXF"),
        earnedCredits = data.field("YHXF"),
        averageGradePoint = data.field("PJJD"),
        gpa = data.field("GPA"),
        averageScore = data.field("PJF"),
        weightedAverageScore = data.field("JQPJF"),
        classRank = data.field("BJPM"),
        majorRank = data.field("ZYPM"),
        passRate = data.field("KCTGL"),
        creditProgress = data.field("XFJD"),
        fetchedAt = fetchedAt,
    )
}
