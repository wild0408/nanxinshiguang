package com.wild0408.nanxinshiguang.data.model

/**
 * 「每日一言」一条内容。
 *
 * @param text 引文正文
 * @param source 出处/作者，可为空（部分来源没有标注）
 */
data class DailyQuote(
    val text: String,
    val source: String?,
)

/**
 * 把一言接口返回的 `from`（出处）与 `from_who`（作者）整理成一行署名。
 *
 * 接口的 `from` 有时自带书名号（如 `《论语》`），有时没有（如 `礼记·中庸`），
 * 因此统一规范为：书名号缺失时补上，避免出现 `《《论语》》`，也避免同一行里两种写法并存。
 */
fun formatQuoteSource(from: String?, fromWho: String?): String? {
    val rawWork = from?.trim().orEmpty()
    val who = fromWho?.trim().orEmpty()
    val work = when {
        rawWork.isEmpty() -> ""
        rawWork.contains("《") -> rawWork
        else -> "《$rawWork》"
    }
    return when {
        work.isEmpty() && who.isEmpty() -> null
        who.isEmpty() -> "——$work"
        work.isEmpty() -> "——$who"
        else -> "——$who$work"
    }
}
