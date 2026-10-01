package com.wild0408.nanxinshiguang.data.parser

import com.wild0408.nanxinshiguang.data.model.LaborMajorLive
import com.wild0408.nanxinshiguang.data.model.LaborOfficialResult
import com.wild0408.nanxinshiguang.data.model.LaborScore

private fun htmlText(value: String): String = value
    .replace(Regex("<[^>]+>"), " ")
    .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
    .replace(Regex("\\s+"), " ").trim()

private fun tableRows(html: String, id: String): List<List<String>> {
    val table = Regex("(?is)<table[^>]*id=[\\\"']$id[\\\"'][^>]*>(.*?)</table>").find(html)?.groupValues?.get(1) ?: return emptyList()
    return Regex("(?is)<tr[^>]*>(.*?)</tr>").findAll(table).map { row ->
        Regex("(?is)<t[dh][^>]*>(.*?)</t[dh]>").findAll(row.groupValues[1]).map { htmlText(it.groupValues[1]) }.toList()
    }.filter { it.isNotEmpty() }.toList()
}

private fun firstDataRow(html: String, id: String): Map<String, String>? {
    val rows = tableRows(html, id)
    if (rows.size < 2) return null
    val headers = rows.first()
    return rows.drop(1).firstOrNull()?.let { cells -> headers.mapIndexedNotNull { i, h -> cells.getOrNull(i)?.let { h to it } }.toMap() }
}

private fun Map<String, String>.value(vararg names: String): String = names.firstNotNullOfOrNull { this[it] }.orEmpty()

fun parseLaborScore(result: String, life: String, service: String, major: String, fetchedAt: Long): LaborScore {
    val officialRow = firstDataRow(result, "ResultManage_StudentResult_Index_table")
    val lifeRow = firstDataRow(life, "c_app_page_index_StudentJiFen_table")
    val serviceRow = firstDataRow(service, "c_app_page_index_StudentJiFen_table")
    val majorRow = firstDataRow(major, "ResultManage_ZYLDScoreHZ_Index_table")
    return LaborScore(
        official = officialRow?.let { LaborOfficialResult(it.value("理论积分"), it.value("生活劳动"), it.value("服务劳动"), it.value("专业劳动课程积分"), it.value("竞赛积分"), it.value("专业劳动"), it.value("总积分"), it.value("是否确认"), it.value("更新日期"), it.value("是否归档"), it.value("归档时间")) },
        liveLife = lifeRow?.value("总积分"), liveService = serviceRow?.value("总积分"),
        liveMajor = majorRow?.let { LaborMajorLive(it.value("专业劳动课程积分"), it.value("竞赛项目积分", "竞赛积分"), it.value("专业劳动累计积分", "专业劳动")) },
        fetchedAt = fetchedAt,
    )
}
