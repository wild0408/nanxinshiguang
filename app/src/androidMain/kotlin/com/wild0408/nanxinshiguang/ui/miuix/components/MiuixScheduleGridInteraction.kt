package com.wild0408.nanxinshiguang.ui.miuix.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.wild0408.nanxinshiguang.data.model.schedule_style.ScheduleModeProto
import com.wild0408.nanxinshiguang.ui.schedule.MergedCourseBlock
import com.wild0408.nanxinshiguang.ui.schedule.WeeklyScheduleUiState
import com.wild0408.nanxinshiguang.ui.material.schedule.components.CourseMoveIntent
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ISingleSchedulable
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ScheduleGridStyleComposed
import com.wild0408.nanxinshiguang.ui.material.schedule.components.calculateSingleSchedulables
import com.wild0408.nanxinshiguang.ui.material.schedule.components.mapDisplayIndexToDay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Text

data class SchedulePlaceholderSlot(
    val day: Int,
    val section: Float,
    val duration: Float = 1f,
)

private fun mapDayToDisplayIndex(day: Int, firstDayOfWeek: Int): Int {
    return (day - firstDayOfWeek + 7) % 7
}

private class MiuixGridState(val scrollState: ScrollState) {
    var expandedItem by mutableStateOf<ISingleSchedulable?>(null)
    var activeMoveIntent by mutableStateOf<CourseMoveIntent?>(null)
    var bodyDragOffsetX by mutableStateOf(0f)
    var bodyDragOffsetY by mutableStateOf(0f)
    var isTopHandleDragging by mutableStateOf(false)
    var isBottomHandleDragging by mutableStateOf(false)
    var topHandleDragOffsetY by mutableStateOf(0f)
    var bottomHandleDragOffsetY by mutableStateOf(0f)
    var gridWidthPx by mutableStateOf(0f)
    var viewportHeightPx by mutableStateOf(0f)

    val isEditingActive: Boolean
        get() = expandedItem != null && (activeMoveIntent != null || isTopHandleDragging || isBottomHandleDragging)

    fun reset() {
        expandedItem = null
        activeMoveIntent = null
        bodyDragOffsetX = 0f
        bodyDragOffsetY = 0f
        isTopHandleDragging = false
        isBottomHandleDragging = false
        topHandleDragOffsetY = 0f
        bottomHandleDragOffsetY = 0f
    }
}

@Composable
fun MiuixScheduleGrid(
    style: ScheduleGridStyleComposed,
    viewState: WeeklyScheduleUiState,
    courses: List<MergedCourseBlock>,
    scrollState: ScrollState = rememberScrollState(),
    viewportHeightPx: Float,
    editingBlock: MergedCourseBlock?,
    placeholderSlot: SchedulePlaceholderSlot? = null,
    onEditingChanged: (Boolean) -> Unit,
    onCourseClick: (MergedCourseBlock) -> Unit,
    onCourseLongPress: (MergedCourseBlock, Rect) -> Unit,
    onBlankTap: (day: Int, section: Int) -> Unit,
    onBlankLongPress: (day: Int, section: Int, anchor: Rect) -> Unit,
    onPlaceholderClick: ((day: Int, section: Float) -> Unit)? = null,
    onPlaceholderLongPress: ((day: Int, section: Float, anchor: Rect) -> Unit)? = null,
    onTimeSlotClick: () -> Unit,
    onCourseMoved: (MergedCourseBlock, Int, Float, Float) -> Unit,
    onCourseTimeAdjusted: (MergedCourseBlock, Float, Float) -> Unit,
    onInitiateFloatingMode: (MergedCourseBlock) -> Unit,
) {
    val displayDays = remember(viewState.showWeekends, viewState.firstDayOfWeek) {
        val days = (1..7).map { it.toString() }
        val reordered = days.drop(viewState.firstDayOfWeek - 1) + days.take(viewState.firstDayOfWeek - 1)
        if (viewState.showWeekends) reordered else reordered.take(5)
    }
    val is24Hour = style.scheduleMode == ScheduleModeProto.TIME_24H_MODE
    val maxSections = if (is24Hour) 24 else viewState.timeSlots.size.coerceAtLeast(1)
    val effectiveSectionHeight = if (style.sectionHeight == 70.dp) 54.dp else style.sectionHeight
    val effectiveTimeColumnWidth = if (style.timeColumnWidth == 40.dp) 36.dp else style.timeColumnWidth
    val gridStyle = remember(style, effectiveSectionHeight, effectiveTimeColumnWidth) {
        style.copy(
            hideGridLines = true,
            sectionHeight = effectiveSectionHeight,
            timeColumnWidth = effectiveTimeColumnWidth,
        )
    }
    val gridState = remember(scrollState) { MiuixGridState(scrollState) }
    val density = LocalDensity.current
    val items = remember(courses, viewState.firstDayOfWeek, viewState.showWeekends) {
        calculateSingleSchedulables(courses, viewState.firstDayOfWeek, viewState.showWeekends)
    }

    LaunchedEffect(viewportHeightPx) {
        gridState.viewportHeightPx = viewportHeightPx
    }

    LaunchedEffect(courses) {
        gridState.reset()
        onEditingChanged(false)
    }
    LaunchedEffect(editingBlock, items) {
        if (editingBlock == null) {
            gridState.reset()
            onEditingChanged(false)
        } else {
            gridState.expandedItem = items.firstOrNull { it.parentBlock == editingBlock }
            onEditingChanged(gridState.expandedItem != null)
        }
    }

    LaunchedEffect(gridState.isEditingActive) {
        if (gridState.isEditingActive) {
            while (true) {
                val item = gridState.expandedItem ?: break
                val sectionHeightPx = with(density) { gridStyle.sectionHeight.toPx() }
                val threshold = with(density) { 40.dp.toPx() }
                val scrollSpeed = with(density) { 8.dp.toPx() }
                val top = when {
                    gridState.activeMoveIntent != null -> item.startSection * sectionHeightPx + gridState.bodyDragOffsetY
                    gridState.isTopHandleDragging -> item.startSection * sectionHeightPx + gridState.topHandleDragOffsetY
                    else -> null
                }
                val bottom = when {
                    gridState.activeMoveIntent != null -> item.endSection * sectionHeightPx + gridState.bodyDragOffsetY
                    gridState.isBottomHandleDragging -> item.endSection * sectionHeightPx + gridState.bottomHandleDragOffsetY
                    else -> null
                }
                var amount = 0f
                if (top != null && top - gridState.scrollState.value < threshold && gridState.scrollState.value > 0) {
                    amount = -scrollSpeed.coerceAtMost(gridState.scrollState.value.toFloat())
                } else if (bottom != null && gridState.viewportHeightPx > 0f && bottom - gridState.scrollState.value > gridState.viewportHeightPx - threshold) {
                    amount = scrollSpeed.coerceAtMost((gridState.scrollState.maxValue - gridState.scrollState.value).toFloat())
                }
                if (amount != 0f) {
                    gridState.scrollState.scrollBy(amount)
                    when {
                        gridState.activeMoveIntent != null -> gridState.bodyDragOffsetY += amount
                        gridState.isTopHandleDragging -> gridState.topHandleDragOffsetY += amount
                        gridState.isBottomHandleDragging -> gridState.bottomHandleDragOffsetY += amount
                    }
                }
                delay(16.milliseconds)
            }
        }
    }

    val gridHeight = gridStyle.sectionHeight * maxSections
    BoxWithConstraints(Modifier.fillMaxWidth().height(gridHeight)) {
        val sectionHeightPx = with(density) { gridStyle.sectionHeight.toPx() }
        var gridBounds by remember { mutableStateOf(Rect.Zero) }
        Row(Modifier.fillMaxSize()) {
            MiuixTimeColumn(
                style = gridStyle,
                timeSlots = viewState.timeSlots,
                maxGridSections = maxSections,
                is24HourMode = is24Hour,
                onTimeSlotClicked = onTimeSlotClick,
                modifier = Modifier.fillMaxHeight(),
                lineColor = Color.Transparent,
                currentSectionIndex = viewState.currentSectionIndex,
                textColor = MiuixTheme.colorScheme.onSurface,
                subTextColor = MiuixTheme.colorScheme.onSurfaceVariantActions,
                strokeWidthPx = 0f,
                activeTextColor = MiuixTheme.colorScheme.primary,
                showActiveBackground = false,
            )
            Layout(
                content = {
                    items.forEach { item ->
                        val expanded = gridState.expandedItem?.parentBlock == item.parentBlock
                        var itemBounds by remember(item.parentBlock) { mutableStateOf(Rect.Zero) }
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned {
                                    itemBounds = it.boundsInRoot()
                                }
                                .padding(if (gridStyle.courseBlockOuterPadding < 2.dp) 2.dp else gridStyle.courseBlockOuterPadding)
                                .zIndex(if (expanded) 3f else 0f)
                                .pointerInput(item, expanded, itemBounds) {
                                    detectTapGestures(
                                        onTap = {
                                            if (expanded) {
                                                gridState.reset()
                                                onEditingChanged(false)
                                            } else {
                                                onCourseClick(item.parentBlock)
                                            }
                                        },
                                        onLongPress = {
                                            if (!expanded) onCourseLongPress(item.parentBlock, itemBounds)
                                        },
                                    )
                                },
                        ) {
                            MiuixCourseBlock(
                                courseWrapper = item.courseWrapper,
                                isVisualDemoted = item.parentBlock.isVisualDemoted,
                                style = gridStyle,
                                timeSlots = viewState.timeSlots,
                                isFloating = expanded,
                                modifier = if (expanded && !item.parentBlock.isVisualDemoted) {
                                    Modifier.pointerInput(item, gridState.gridWidthPx) {
                                        detectDragGestures(
                                            onDragStart = {
                                                gridState.activeMoveIntent = CourseMoveIntent(item.parentBlock, item.parentBlock.day, item.startSection, item.endSection - item.startSection)
                                                gridState.bodyDragOffsetX = 0f
                                                gridState.bodyDragOffsetY = 0f
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                gridState.bodyDragOffsetX += amount.x
                                                gridState.bodyDragOffsetY += amount.y
                                            },
                                            onDragEnd = {
                                                val intent = gridState.activeMoveIntent
                                                if (intent != null && gridState.gridWidthPx > 0f) {
                                                    val dayCount = displayDays.size.coerceAtLeast(1)
                                                    val cellWidth = gridState.gridWidthPx / dayCount
                                                    val initialX = item.columnIndex * cellWidth + item.subColumnIndex * (cellWidth / item.subColumnCount.coerceAtLeast(1))
                                                    val currentX = initialX + gridState.bodyDragOffsetX
                                                    val blockWidth = cellWidth / item.subColumnCount.coerceAtLeast(1)
                                                    val atEdge = currentX <= 0f || currentX + blockWidth >= gridState.gridWidthPx
                                                    if (atEdge) {
                                                        onInitiateFloatingMode(intent.parentBlock)
                                                        gridState.reset()
                                                        onEditingChanged(false)
                                                    } else {
                                                        val deltaColumns = (gridState.bodyDragOffsetX / cellWidth).roundToInt()
                                                        val targetDisplay = (item.columnIndex + deltaColumns).coerceIn(0, dayCount - 1)
                                                        val targetDay = mapDisplayIndexToDay(targetDisplay, viewState.firstDayOfWeek)
                                                        var targetStart = intent.initialStartSection + gridState.bodyDragOffsetY / sectionHeightPx
                                                        targetStart = if (is24Hour) (targetStart / 0.25f).roundToInt() * 0.25f else targetStart.roundToInt().toFloat()
                                                        targetStart = targetStart.coerceIn(0f, maxSections - intent.duration)
                                                        onCourseMoved(intent.parentBlock, targetDay, targetStart, targetStart + intent.duration)
                                                        gridState.reset()
                                                        onEditingChanged(false)
                                                    }
                                                } else {
                                                    gridState.reset()
                                                    onEditingChanged(false)
                                                }
                                            },
                                            onDragCancel = {
                                                gridState.reset()
                                                onEditingChanged(false)
                                            },
                                        )
                                    }
                                } else Modifier,
                            )
                            if (expanded && gridState.activeMoveIntent == null && !item.parentBlock.isVisualDemoted) {
                                MiuixCourseEditHandles(
                                    onDragStart = { top ->
                                        gridState.isTopHandleDragging = top
                                        gridState.isBottomHandleDragging = !top
                                        if (top) gridState.topHandleDragOffsetY = 0f else gridState.bottomHandleDragOffsetY = 0f
                                    },
                                    onDragging = { delta ->
                                        if (gridState.isTopHandleDragging) gridState.topHandleDragOffsetY += delta
                                        if (gridState.isBottomHandleDragging) gridState.bottomHandleDragOffsetY += delta
                                    },
                                    onDragEnd = {
                                        val current = gridState.expandedItem
                                        if (current != null) {
                                            val minGap = if (is24Hour) 0.25f else 1f
                                            var start = current.startSection
                                            var end = current.endSection
                                            if (gridState.isTopHandleDragging) {
                                                start = (start + gridState.topHandleDragOffsetY / sectionHeightPx)
                                                    .let { if (is24Hour) (it / 0.25f).roundToInt() * 0.25f else it.roundToInt().toFloat() }
                                                    .coerceIn(0f, end - minGap)
                                            } else if (gridState.isBottomHandleDragging) {
                                                end = (end + gridState.bottomHandleDragOffsetY / sectionHeightPx)
                                                    .let { if (is24Hour) (it / 0.25f).roundToInt() * 0.25f else it.roundToInt().toFloat() }
                                                    .coerceIn(start + minGap, maxSections.toFloat())
                                            }
                                            if (start != current.startSection || end != current.endSection) {
                                                onCourseTimeAdjusted(current.parentBlock, start, end)
                                            }
                                        }
                                        gridState.reset()
                                        onEditingChanged(false)
                                    },
                                )
                            }
                            if (expanded && gridState.isEditingActive && !item.parentBlock.isVisualDemoted) {
                                val previewStart = when {
                                    gridState.activeMoveIntent != null -> item.startSection + gridState.bodyDragOffsetY / sectionHeightPx
                                    gridState.isTopHandleDragging -> item.startSection + gridState.topHandleDragOffsetY / sectionHeightPx
                                    else -> item.startSection
                                }.snapToGrid(is24Hour)
                                val previewEnd = when {
                                    gridState.activeMoveIntent != null -> item.endSection + gridState.bodyDragOffsetY / sectionHeightPx
                                    gridState.isBottomHandleDragging -> item.endSection + gridState.bottomHandleDragOffsetY / sectionHeightPx
                                    else -> item.endSection
                                }.snapToGrid(is24Hour)
                                MiuixCourseTimePreview(
                                    start = previewStart.coerceIn(0f, maxSections.toFloat()),
                                    end = previewEnd.coerceIn(0f, maxSections.toFloat()),
                                    is24Hour = is24Hour,
                                )
                            }
                        }
                    }
                    if (placeholderSlot != null) {
                        MiuixPlaceholderBlock(
                            style = gridStyle,
                            onClick = {
                                onPlaceholderClick?.invoke(placeholderSlot.day, placeholderSlot.section)
                            },
                            onLongPress = { anchor ->
                                onPlaceholderLongPress?.invoke(placeholderSlot.day, placeholderSlot.section, anchor)
                            },
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .onGloballyPositioned { gridBounds = it.boundsInRoot(); gridState.gridWidthPx = it.size.width.toFloat() }
                    .pointerInput(displayDays.size, sectionHeightPx, viewState.firstDayOfWeek, maxSections, is24Hour, editingBlock) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (gridState.expandedItem != null) {
                                    gridState.reset()
                                    onEditingChanged(false)
                                } else {
                                    val dayCount = displayDays.size.coerceAtLeast(1)
                                    val dayIndex = (offset.x / (size.width / dayCount)).toInt().coerceIn(0, dayCount - 1)
                                    val sectionIndex = (offset.y / sectionHeightPx).toInt().coerceIn(0, maxSections - 1)
                                    onBlankTap(mapDisplayIndexToDay(dayIndex, viewState.firstDayOfWeek), if (is24Hour) sectionIndex else sectionIndex + 1)
                                }
                            },
                            onLongPress = { offset ->
                                if (gridState.expandedItem == null) {
                                    val dayCount = displayDays.size.coerceAtLeast(1)
                                    val cellWidth = size.width / dayCount
                                    val dayIndex = (offset.x / cellWidth).toInt().coerceIn(0, dayCount - 1)
                                    val sectionIndex = (offset.y / sectionHeightPx).toInt().coerceIn(0, maxSections - 1)
                                    val durationSections = if (is24Hour) 2f else 1f
                                    val cellLeft = gridBounds.left + dayIndex * cellWidth
                                    val cellTop = gridBounds.top + sectionIndex * sectionHeightPx
                                    val cellRight = cellLeft + cellWidth
                                    val cellBottom = cellTop + durationSections * sectionHeightPx
                                    val anchor = Rect(cellLeft, cellTop, cellRight, cellBottom)
                                    onBlankLongPress(mapDisplayIndexToDay(dayIndex, viewState.firstDayOfWeek), if (is24Hour) sectionIndex else sectionIndex + 1, anchor)
                                }
                            },
                        )
                    },
            ) { measurables, constraints ->
                val dayCount = displayDays.size.coerceAtLeast(1)
                val cellWidth = constraints.maxWidth / dayCount
                val minGapPx = if (is24Hour) 0 else with(density) { 30.dp.roundToPx() }
                val coursePlaceables = measurables.take(items.size).mapIndexed { index, measurable ->
                    val item = items[index]
                    val expanded = gridState.expandedItem?.parentBlock == item.parentBlock
                    val originalHeight = ((item.endSection - item.startSection) * sectionHeightPx).roundToInt().coerceAtLeast(1)
                    val height = when {
                        expanded && gridState.isTopHandleDragging -> (originalHeight - gridState.topHandleDragOffsetY.roundToInt()).coerceAtLeast(minGapPx)
                        expanded && gridState.isBottomHandleDragging -> (originalHeight + gridState.bottomHandleDragOffsetY.roundToInt()).coerceAtLeast(minGapPx)
                        else -> originalHeight
                    }
                    measurable.measure(Constraints.fixed((cellWidth / if (expanded) 1 else item.subColumnCount.coerceAtLeast(1)).coerceAtLeast(1), height))
                }

                val pSlot = placeholderSlot
                val placeholderPlaceable = if (pSlot != null && measurables.size > items.size) {
                    val pHeight = (pSlot.duration * sectionHeightPx).roundToInt().coerceAtLeast(1)
                    measurables.last().measure(Constraints.fixed(cellWidth, pHeight))
                } else null

                layout(constraints.maxWidth, constraints.maxHeight) {
                    coursePlaceables.forEachIndexed { index, placeable ->
                        val item = items[index]
                        val expanded = gridState.expandedItem?.parentBlock == item.parentBlock
                        val moving = gridState.activeMoveIntent?.parentBlock == item.parentBlock
                        val originalX = item.columnIndex * cellWidth + if (expanded) 0 else item.subColumnIndex * (cellWidth / item.subColumnCount.coerceAtLeast(1))
                        val originalY = (item.startSection * sectionHeightPx).roundToInt()
                        var x = originalX
                        var y = originalY
                        if (moving) {
                            x = (item.columnIndex * cellWidth + gridState.bodyDragOffsetX).roundToInt()
                            y = (gridState.activeMoveIntent!!.initialStartSection * sectionHeightPx + gridState.bodyDragOffsetY).roundToInt()
                        } else if (expanded && gridState.isTopHandleDragging) {
                            y = (originalY + gridState.topHandleDragOffsetY).roundToInt()
                        }
                        placeable.placeRelative(x, y)
                    }

                    if (pSlot != null && placeholderPlaceable != null) {
                        val dayIndex = mapDayToDisplayIndex(pSlot.day, viewState.firstDayOfWeek)
                        if (dayIndex in 0 until dayCount) {
                            val startSection = if (is24Hour) pSlot.section else pSlot.section - 1f
                            val px = dayIndex * cellWidth
                            val py = (startSection * sectionHeightPx).roundToInt()
                            placeholderPlaceable.placeRelative(px, py)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.MiuixCourseEditHandles(
    onDragStart: (Boolean) -> Unit,
    onDragging: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    MiuixEditHandle(Modifier.align(Alignment.TopCenter), true, onDragStart, onDragging, onDragEnd)
    MiuixEditHandle(Modifier.align(Alignment.BottomCenter), false, onDragStart, onDragging, onDragEnd)
}

@Composable
private fun BoxScope.MiuixCourseTimePreview(start: Float, end: Float, is24Hour: Boolean) {
    val label = if (is24Hour) {
        "${start.toClockText()} - ${end.toClockText()}"
    } else {
        val first = start.toInt() + 1
        val last = end.toInt().coerceAtLeast(first)
        if (first == last) "第 $first 节" else "第 $first-$last 节"
    }
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 11.sp, color = MiuixTheme.colorScheme.onSurface, maxLines = 1)
    }
}

private fun Float.snapToGrid(is24Hour: Boolean): Float =
    if (is24Hour) (this / 0.25f).roundToInt() * 0.25f else roundToInt().toFloat()

private fun Float.toClockText(): String {
    val totalMinutes = (this * 60f).roundToInt().coerceIn(0, 24 * 60)
    if (totalMinutes == 24 * 60) return "24:00"
    return "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)
}

@Composable
private fun MiuixEditHandle(
    modifier: Modifier,
    top: Boolean,
    onDragStart: (Boolean) -> Unit,
    onDragging: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .pointerInput(top) {
                detectDragGestures(
                    onDragStart = { onDragStart(top) },
                    onDragEnd = onDragEnd,
                    onDrag = { change, amount ->
                        change.consume()
                        onDragging(amount.y)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(12.dp).background(MiuixTheme.colorScheme.primary, CircleShape))
    }
}
