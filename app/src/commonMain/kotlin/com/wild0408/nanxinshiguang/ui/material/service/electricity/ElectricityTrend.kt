package com.wild0408.nanxinshiguang.ui.material.service.electricity

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.electricity_trend_range_days30
import nanxinshiguang.generated.resources.electricity_trend_range_days7
import org.jetbrains.compose.resources.StringResource

enum class ElectricityTrendRange(val labelRes: StringResource) {
    DAYS_7(Res.string.electricity_trend_range_days7), DAYS_30(Res.string.electricity_trend_range_days30),
}

data class ElectricityTrendPoint(val time: Long, val balance: Double)

fun List<Pair<Long, Double>>.withoutConsecutiveDuplicates(): List<Pair<Long, Double>> =
    fold(mutableListOf()) { result, item ->
        if (result.lastOrNull()?.second != item.second) result += item
        result
    }

fun List<Pair<Long, Double>>.forRange(range: ElectricityTrendRange): List<ElectricityTrendPoint> {
    val zone = TimeZone.currentSystemDefault()
    val grouped = groupBy { (time, _) ->
        val date = Instant.fromEpochMilliseconds(time).toLocalDateTime(zone)
        when (range) {
            ElectricityTrendRange.DAYS_7, ElectricityTrendRange.DAYS_30 -> date.year.toString() + "-" + date.month.number + "-" + date.day
        }
    }
    val days = if (range == ElectricityTrendRange.DAYS_7) 7 else 30
    val latestTime = maxOfOrNull { it.first } ?: return emptyList()
    return grouped.values.map { entries ->
        val latest = entries.maxBy { it.first }
        ElectricityTrendPoint(latest.first, latest.second)
    }.filter { it.time >= latestTime - days * 24L * 60L * 60L * 1000L }.sortedBy { it.time }
}
