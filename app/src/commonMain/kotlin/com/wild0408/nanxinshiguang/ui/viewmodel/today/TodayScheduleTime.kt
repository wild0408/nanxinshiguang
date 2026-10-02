package com.wild0408.nanxinshiguang.ui.viewmodel.today

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/** 课程未绑定作息时间时的时间占位符。 */
const val TIME_PLACEHOLDER = "--:--"

/** 当前本地时刻。 */
fun currentLocalTime(): LocalTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time

/**
 * 会随时间推进的当前时刻，默认每分钟更新一次。
 *
 * 今日课表用它判断课程是否已结束，并据此定位到第一节未结束的课程。
 * 页面停留期间下课时间到达后会自行刷新样式，不需要重新进入页面。
 */
@Composable
fun rememberCurrentTime(updateIntervalMinutes: Int = 1): LocalTime {
    var now by remember { mutableStateOf(currentLocalTime()) }
    LaunchedEffect(updateIntervalMinutes) {
        while (true) {
            delay(updateIntervalMinutes.minutes)
            now = currentLocalTime()
        }
    }
    return now
}

/**
 * 课程是否已结束：结束时间早于 [now] 即为已结束。
 * 结束时间缺失或无法解析时按"未结束"处理，避免把异常数据显示成已下课。
 */
fun CourseDisplayModel.isFinishedAt(now: LocalTime): Boolean {
    val raw = endTime?.trim().orEmpty()
    if (raw.isEmpty()) return false
    val end = runCatching { LocalTime.parse(raw) }.getOrNull() ?: return false
    return end < now
}
