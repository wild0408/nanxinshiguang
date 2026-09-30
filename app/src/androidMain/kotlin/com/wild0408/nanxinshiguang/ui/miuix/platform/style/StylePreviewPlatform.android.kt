package com.wild0408.nanxinshiguang.ui.material.settings.style

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wild0408.nanxinshiguang.data.model.schedule_style.ScheduleModeProto
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixScheduleGrid
import com.wild0408.nanxinshiguang.ui.schedule.WeeklyScheduleUiState
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ScheduleGridStyleComposed
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.stringArrayResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.week_days_short_names
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Clock

@Composable
actual fun MiuixStylePreview(
    style: ScheduleGridStyleComposed,
    demoUiState: WeeklyScheduleUiState,
    modifier: Modifier,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val firstDay = DayOfWeek.entries.getOrNull(demoUiState.firstDayOfWeek - 1) ?: DayOfWeek.MONDAY
    val weekStart = remember(today, firstDay) {
        var start = today
        while (start.dayOfWeek != firstDay) start = start.minus(1, DateTimeUnit.DAY)
        start
    }
    val days = stringArrayResource(Res.array.week_days_short_names).toList()
    val reorderedDays = remember(days, demoUiState.firstDayOfWeek) {
        days.drop(demoUiState.firstDayOfWeek - 1) + days.take(demoUiState.firstDayOfWeek - 1)
    }
    val displayDays = if (demoUiState.showWeekends) reorderedDays else reorderedDays.take(5)
    val displayDates = remember(weekStart, displayDays.size) {
        displayDays.indices.map { weekStart.plus(it.toLong(), DateTimeUnit.DAY) }
    }
    val scrollState = rememberScrollState()
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    val effectiveStyle = remember(style) {
        style.copy(
            hideGridLines = true,
            sectionHeight = if (style.sectionHeight == 70.dp) 54.dp else style.sectionHeight,
            timeColumnWidth = if (style.timeColumnWidth == 40.dp) 36.dp else style.timeColumnWidth,
        )
    }
    val maxSections = if (effectiveStyle.scheduleMode == ScheduleModeProto.TIME_24H_MODE) {
        24
    } else {
        demoUiState.timeSlots.size.coerceAtLeast(1)
    }
    val previewContentHeight = 44.dp + (effectiveStyle.sectionHeight * maxSections) + 12.dp

    Box(modifier.height(previewContentHeight)) {
        if (style.backgroundImagePath.isNotEmpty()) {
            AsyncImage(
                model = style.backgroundImagePath,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )
            Box(Modifier.matchParentSize().background(MiuixTheme.colorScheme.surface.copy(alpha = 0.58f)))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewContentHeight)
                .onSizeChanged { viewportHeightPx = it.height },
        ) {
            MiuixPreviewWeekHeader(
                days = displayDays,
                dates = displayDates,
                today = today,
                modifier = Modifier.fillMaxWidth(),
            )
            MiuixScheduleGrid(
                style = effectiveStyle,
                viewState = demoUiState,
                courses = demoUiState.currentMergedCourses,
                scrollState = scrollState,
                viewportHeightPx = viewportHeightPx.toFloat(),
                editingBlock = null,
                onEditingChanged = {},
                onCourseClick = {},
                onCourseLongPress = { _, _ -> },
                onBlankTap = { _, _ -> },
                onBlankLongPress = { _, _, _ -> },
                onTimeSlotClick = {},
                onCourseMoved = { _, _, _, _ -> },
                onCourseTimeAdjusted = { _, _, _ -> },
                onInitiateFloatingMode = {},
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MiuixPreviewWeekHeader(
    days: List<String>,
    dates: List<kotlinx.datetime.LocalDate>,
    today: kotlinx.datetime.LocalDate,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(44.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(36.dp))
        days.forEachIndexed { index, day ->
            val date = dates.getOrNull(index)
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val active = date == today
                Text(
                    day,
                    style = MiuixTheme.textStyles.footnote1,
                    color = if (active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                )
                date?.let {
                    Text(
                        "%02d/%02d".format(it.month.number, it.day),
                        style = MiuixTheme.textStyles.footnote2,
                        color = if (active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantActions,
                    )
                }
            }
        }
    }
}
