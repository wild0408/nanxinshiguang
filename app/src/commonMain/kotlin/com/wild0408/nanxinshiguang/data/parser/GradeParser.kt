package com.wild0408.nanxinshiguang.data.parser

import com.wild0408.nanxinshiguang.data.model.GradeRecord
import com.wild0408.nanxinshiguang.data.model.GradeSemesterSummary
import com.wild0408.nanxinshiguang.data.model.summarizeGradeRecords
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val gradeJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * 成绩解析结果。
 *
 * 原实现把「响应无法解析」和「解析成功但没有记录」都退化成 `emptyList()`，
 * 页面只能显示"暂无成绩"，用户无法区分是自己确实没成绩还是接口变了。
 */
sealed interface GradeParseResult {
    data class Success(val records: List<GradeRecord>) : GradeParseResult
    data class Invalid(val reason: String) : GradeParseResult
}

/** 解析 NUIST 教务 EMAP 成绩响应，并区分"解析失败"与"确实没有数据"。 */
fun parseEmapGradeRecordsResult(responseJson: String): GradeParseResult {
    val root = runCatching { gradeJson.parseToJsonElement(responseJson) }
        .getOrElse { return GradeParseResult.Invalid("响应不是合法 JSON") }
    if (root.findEmapRows() == null) {
        return GradeParseResult.Invalid("响应中没有成绩列表字段")
    }
    return GradeParseResult.Success(parseEmapGradeRecords(responseJson))
}

/** 解析适配脚本回传的统一成绩数组，不处理任何学校的原始字段。 */
fun parseGradeRecords(responseJson: String): List<GradeRecord> {
    val root = runCatching { gradeJson.parseToJsonElement(responseJson) }.getOrNull() ?: return emptyList()
    val rows = root.findGradeRows() ?: return emptyList()

    return rows.mapIndexedNotNull { index, element ->
        element.toGradeRecord(index)
    }
}

/**
 * 解析 NUIST 教务 EMAP `cjcx` 接口返回的原始 rows。
 *
 * 教务字段同时存在原始值和 *_DISPLAY 展示值，统一在这里转换为成绩中心模型。
 */
fun parseEmapGradeRecords(responseJson: String): List<GradeRecord> {
    val root = runCatching { gradeJson.parseToJsonElement(responseJson) }.getOrNull() ?: return emptyList()
    val rows = root.findEmapRows() ?: return emptyList()
    return rows.mapIndexedNotNull { index, element ->
        val row = element as? JsonObject ?: return@mapIndexedNotNull null
        val courseName = row.displayText("XSKCM", "KCM", "KCMC", "KCM_DISPLAY")
            ?: return@mapIndexedNotNull null
        val semester = row.displayText("XNXQDM", "XNXQ") ?: ""
        val statusParts = listOfNotNull(
            row.displayText("CXCKDM"),
            row.displayText("XDFSDM")?.takeUnless { it == "正常" },
        )
        val composition = listOfNotNull(
            row.percentPart("平时", "PSCJXS"),
            row.percentPart("期中", "QZCJXS"),
            row.percentPart("期末", "QMCJXS"),
        ).joinToString(" + ").ifBlank { null }
        GradeRecord(
            id = row.displayText("WID", "XSKCH") ?: "grade-$index",
            courseName = courseName,
            courseType = row.displayText("KCXZDM", "KCLBDM") ?: "未分类",
            credits = row.displayText("XF") ?: "--",
            score = row.displayText("ZCJ", "XSZCJMC") ?: "暂无",
            gradePoint = row.displayText("XFJD") ?: "--",
            semester = semester,
            status = statusParts.joinToString(" / ").ifBlank { null },
            teacher = row.displayText("SKJS", "JSXM"),
            examType = row.displayText("KSLXDM", "KSXZDM", "KSLX", "KSXZ"),
            scoreComposition = composition,
        )
    }
}

private fun JsonElement.findGradeRows(): JsonArray? = when (this) {
    is JsonArray -> if (any { it.looksLikeGradeRecord() }) this else firstNotNullOfOrNull { it.findGradeRows() }
    is JsonObject -> {
        this["records"]?.findGradeRows()
    }
    else -> null
}

private fun JsonElement.findEmapRows(): JsonArray? = when (this) {
    is JsonObject -> {
        (this["rows"] as? JsonArray)
            ?: this.values.firstNotNullOfOrNull { it.findEmapRows() }
    }
    is JsonArray -> null
    else -> null
}

private fun JsonElement.looksLikeGradeRecord(): Boolean {
    val obj = this as? JsonObject ?: return false
    return obj.containsKey("courseName")
}

/** 按学期生成页面总览；排名必须由教务脚本提供，解析器不会自行估算。 */
fun summarizeGrades(records: List<GradeRecord>, semester: String): GradeSemesterSummary {
    return summarizeGradeRecords(records, semester)
}

private fun JsonElement.toGradeRecord(index: Int): GradeRecord? {
    val obj = this as? JsonObject ?: return null
    val courseName = obj.text("courseName") ?: return null
    val semester = obj.text("semester") ?: return null
    return GradeRecord(
        id = obj.text("id") ?: "grade-$index",
        courseName = courseName,
        courseType = obj.text("courseType") ?: "未分类",
        credits = obj.text("credits") ?: "--",
        score = obj.text("score") ?: "暂无",
        gradePoint = obj.text("gradePoint") ?: "--",
        semester = semester,
        status = obj.text("status"),
        teacher = obj.text("teacher"),
        examType = obj.text("examType"),
        scoreComposition = obj.text("scoreComposition")
    )
}

private fun JsonObject.text(vararg keys: String): String? = keys.asSequence()
    .mapNotNull { this[it]?.jsonPrimitive?.contentOrNull }
    .map(String::trim)
    .firstOrNull { it.isNotEmpty() && it.lowercase() != "null" }

private fun JsonObject.displayText(vararg keys: String): String? =
    keys.asSequence().mapNotNull { key ->
        text("${key}_DISPLAY", key)
    }.firstOrNull()

private fun JsonObject.percentPart(label: String, key: String): String? =
    displayText(key)?.let { "$label $it%" }
