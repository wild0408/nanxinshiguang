package com.wild0408.nanxinshiguang.data.portal

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * 统一门户/教务 EMAP 课表行解析出的课程数据（与 UI、Android 无关的纯数据）。
 */
data class PortalCourseRow(
    val name: String,
    val teacher: String,
    val position: String,
    val day: Int,
    val startSection: Int,
    val endSection: Int,
    val weeks: List<Int>,
)

/**
 * 解析一行教务 EMAP 课表数据。
 *
 * 规则（与原 `PortalSession.toImportCourse()` 完全一致，仅抽出来以便单测覆盖边界）：
 * - `KCM`（课程名，优先取 `KCM_DISPLAY`）缺失 → 整行丢弃；
 * - `SKXQ`（星期）必须在 1..7，`KSJC`（开始节次）必须 > 0，`JSJC`（结束节次）必须 >= 开始节次；
 * - `SKZC`（上课周次）是按位记录的字符串，第 N 位为 '1' 表示第 N 周上课，全为 0 则丢弃该行；
 * - 地点 = 教室（`JASMC`）与校区（`XXXQDM`）的组合，都没有时为"待定"；
 * - 教师取 `SKJS` 中第一个姓名（可能用 /、顿号或逗号分隔），为空时为"未知"。
 *
 * 返回 null 表示该行不可用，调用方应跳过而不是生成一条空课程。
 */
fun parsePortalCourseRow(element: JsonElement): PortalCourseRow? {
    val obj = element as? JsonObject ?: return null
    val name = obj.portalDisplayText("KCM") ?: return null
    val day = obj.portalRawText("SKXQ")?.toIntOrNull()?.takeIf { it in 1..7 } ?: return null
    val startSection = obj.portalRawText("KSJC")?.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val endSection = obj.portalRawText("JSJC")?.toIntOrNull()?.takeIf { it >= startSection } ?: return null
    val weeks = obj.portalRawText("SKZC").orEmpty().mapIndexedNotNull { weekIndex, value ->
        (weekIndex + 1).takeIf { value == '1' }
    }
    if (weeks.isEmpty()) return null

    val campus = obj.portalDisplayText("XXXQDM").orEmpty()
    val room = obj.portalDisplayText("JASMC").orEmpty()
    val position = when {
        campus.isNotBlank() && room.isNotBlank() -> "$room（$campus）"
        room.isNotBlank() -> room
        campus.isNotBlank() -> campus
        else -> "待定"
    }
    val teacher = obj.portalDisplayText("SKJS").orEmpty()
        .split(Regex("[\\/、,，]"))
        .firstOrNull()
        ?.trim()
        .orEmpty()
        .ifBlank { "未知" }

    return PortalCourseRow(
        name = name,
        teacher = teacher,
        position = position,
        day = day,
        startSection = startSection,
        endSection = endSection,
        weeks = weeks,
    )
}

/** 优先读取 `KEY_DISPLAY`，其次 `KEY`；空串与 "null" 都视为无值。 */
internal fun JsonObject.portalDisplayText(vararg keys: String): String? = keys.asSequence()
    .mapNotNull { key ->
        sequenceOf("${key}_DISPLAY", key).mapNotNull { candidate ->
            runCatching { this[candidate]?.jsonPrimitive?.contentOrNull?.trim() }.getOrNull()
        }.firstOrNull { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    }
    .firstOrNull()

/** 只读取原始键（不尝试 `_DISPLAY`）。 */
internal fun JsonObject.portalRawText(vararg keys: String): String? = keys.asSequence()
    .mapNotNull { key ->
        runCatching {
            val primitive = this[key] as? JsonPrimitive
            primitive?.contentOrNull?.trim()
        }.getOrNull()
    }
    .firstOrNull { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

/** 从 `datas.<name>.rows` 取出数组，不存在时返回 null。 */
fun JsonObject.portalSectionRows(name: String): JsonArray? =
    ((this["datas"] as? JsonObject)?.get(name) as? JsonObject)?.let { it["rows"] as? JsonArray }
