package com.wild0408.nanxinshiguang.ui.material.service.electricity

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class ElectricityTrendRange(val label: String) {
    DAYS_7("近 7 天"), DAYS_30("近 30 天"),
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
            ElectricityTrendRange.DAYS_7, ElectricityTrendRange.DAYS_30 -> date.year.toString() + "-" + date.monthNumber + "-" + date.dayOfMonth
        }
    }
    val days = if (range == ElectricityTrendRange.DAYS_7) 7 else 30
    val latestTime = maxOfOrNull { it.first } ?: return emptyList()
    return grouped.values.map { entries ->
        val latest = entries.maxBy { it.first }
        ElectricityTrendPoint(latest.first, latest.second)
    }.filter { it.time >= latestTime - days * 24L * 60L * 60L * 1000L }.sortedBy { it.time }
}
