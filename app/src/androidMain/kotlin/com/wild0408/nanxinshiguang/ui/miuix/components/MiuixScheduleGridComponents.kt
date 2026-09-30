package com.wild0408.nanxinshiguang.ui.miuix.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wild0408.nanxinshiguang.data.db.main.CourseWithWeeks
import com.wild0408.nanxinshiguang.data.db.main.TimeSlot
import com.wild0408.nanxinshiguang.data.model.schedule_style.BorderTypeProto
import com.wild0408.nanxinshiguang.data.model.schedule_style.ScheduleModeProto
import com.wild0408.nanxinshiguang.ui.schedule.components.ScheduleGridStyleComposed
import com.wild0408.nanxinshiguang.ui.theme.LocalIsDarkTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import top.yukonga.miuix.kmp.basic.Icon
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.add_24px
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Android Miuix 课表课程块，避免周课表列表回退到 commonMain 的 Material 组件。 */
@Composable
fun MiuixCourseBlock(
    courseWrapper: CourseWithWeeks,
    isVisualDemoted: Boolean,
    style: ScheduleGridStyleComposed,
    timeSlots: List<TimeSlot>,
    modifier: Modifier = Modifier,
    isFloating: Boolean = false,
) {
    val course = courseWrapper.course
    val isDarkTheme = LocalIsDarkTheme.current
    val colorIndex = course.colorInt.takeIf { it in style.courseColorMaps.indices }
    val courseColor = colorIndex?.let { index ->
        val colorMap = style.courseColorMaps[index]
        if (isDarkTheme) colorMap.dark else colorMap.light
    } ?: if (isDarkTheme) style.courseColorMaps.first().dark else style.courseColorMaps.first().light
    val blockColor = courseColor.copy(alpha = if (isFloating) 0.95f else style.courseBlockAlpha)
    val textColor = style.courseTextColor ?: MiuixTheme.colorScheme.onSurface
    val borderColor = if (isFloating) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantActions
    val borderWidth = if (isFloating) 2.dp else 1.dp
    val borderAlpha = if (isFloating) 1f else style.courseBlockAlpha
    val shape = RoundedCornerShape(style.courseBlockCornerRadius)

    val customTimeString = course.customStartTime?.let { start ->
        course.customEndTime?.let { end -> "$start - $end" }
    }
    val timeText = if (style.scheduleMode == ScheduleModeProto.TIME_24H_MODE) {
        customTimeString ?: timeSlots.find { it.number == course.startSection }?.let { start ->
            timeSlots.find { it.number == course.endSection }?.let { end ->
                "${start.startTime} - ${end.endTime}"
            }
        }
    } else {
        customTimeString ?: if (style.showStartTime) {
            timeSlots.find { it.number == course.startSection }?.startTime
        } else {
            null
        }
    }

    val borderModifier = when (style.borderType) {
        BorderTypeProto.BORDER_TYPE_SOLID -> Modifier.border(
            borderWidth,
            borderColor.copy(alpha = borderAlpha),
            shape,
        )

        BorderTypeProto.BORDER_TYPE_DASHED -> Modifier.drawBehind {
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, this),
                color = borderColor.copy(alpha = borderAlpha),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = borderWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f)),
                ),
            )
        }

        else -> if (isFloating) Modifier.border(borderWidth, borderColor, shape) else Modifier
    }
    val horizontalAlignment = if (style.textAlignCenterHorizontal) Alignment.CenterHorizontally else Alignment.Start
    val verticalArrangement = if (style.textAlignCenterVertical) Arrangement.Center else Arrangement.Top
    val textAlign = if (style.textAlignCenterHorizontal) TextAlign.Center else TextAlign.Start
    val floatingShadow = if (isFloating) Modifier.shadow(8.dp, shape, clip = false) else Modifier
    val s13 = (13 * style.fontScale).sp
    val s10 = (10 * style.fontScale).sp

    Box(
        modifier = modifier
            .then(floatingShadow)
            .fillMaxSize()
            .then(borderModifier)
            .clip(shape)
            .background(blockColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(style.courseBlockInnerPadding),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
        ) {
            if (timeText != null) {
                Text(
                    text = timeText,
                    fontSize = s10,
                    color = textColor.copy(alpha = 0.82f),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = textAlign,
                    style = TextStyle(lineHeight = 1.em),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = course.name,
                fontSize = s13,
                fontWeight = FontWeight.Bold,
                color = textColor,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign,
                modifier = Modifier.weight(1f, fill = false),
                style = TextStyle(lineHeight = 1.2.em),
                maxLines = 3,
            )
            if (!style.hideTeacher && course.teacher.isNotBlank()) {
                Text(
                    text = course.teacher,
                    fontSize = s10,
                    color = textColor.copy(alpha = 0.88f),
                    textAlign = textAlign,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = TextStyle(lineHeight = 1.em),
                )
            }
            if (!style.hideLocation && course.position.isNotBlank()) {
                val prefix = if (style.removeLocationAt) "" else "@"
                Text(
                    text = "$prefix${course.position}",
                    fontSize = s10,
                    color = textColor.copy(alpha = 0.88f),
                    textAlign = textAlign,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = TextStyle(lineHeight = 1.em),
                )
            }
        }

        if (isVisualDemoted && !isFloating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background((if (isDarkTheme) Color.Black else Color.White).copy(alpha = 0.618f))
                    .drawBehind {
                        val stripeWidth = 5.dp.toPx()
                        val stripeColor = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.06f)
                        drawRect(
                            brush = Brush.linearGradient(
                                0f to stripeColor,
                                0.45f to stripeColor,
                                0.55f to Color.Transparent,
                                1f to Color.Transparent,
                                start = Offset.Zero,
                                end = Offset(stripeWidth, stripeWidth),
                                tileMode = TileMode.Repeated,
                            ),
                        )
                    },
            )
        }
    }
}

/** 空白时段加号占位卡片组件 */
@Composable
fun MiuixPlaceholderBlock(
    style: ScheduleGridStyleComposed,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongPress: ((Rect) -> Unit)? = null,
) {
    val shape = RoundedCornerShape(style.courseBlockCornerRadius)
    val outerPadding = if (style.courseBlockOuterPadding < 2.dp) 2.dp else style.courseBlockOuterPadding
    val primaryColor = MiuixTheme.colorScheme.primary
    var blockBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier
            .padding(outerPadding)
            .fillMaxSize()
            .clip(shape)
            .background(primaryColor.copy(alpha = 0.12f))
            .drawBehind {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = primaryColor.copy(alpha = 0.6f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f)),
                    ),
                )
            }
            .onGloballyPositioned { blockBounds = it.boundsInRoot() }
            .pointerInput(blockBounds) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        if (onLongPress != null) {
                            onLongPress(blockBounds)
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.add_24px),
            contentDescription = "添加课程",
            tint = primaryColor,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** Android Miuix 时间栏，当前节次只改变文字颜色，不绘制 Material 选中背景。 */
@Composable
fun MiuixTimeColumn(
    style: ScheduleGridStyleComposed,
    timeSlots: List<TimeSlot>,
    maxGridSections: Int,
    is24HourMode: Boolean,
    onTimeSlotClicked: () -> Unit,
    modifier: Modifier,
    lineColor: Color,
    currentSectionIndex: Int = -1,
    textColor: Color,
    subTextColor: Color,
    strokeWidthPx: Float,
    activeTextColor: Color = MiuixTheme.colorScheme.primary,
    showActiveBackground: Boolean = false,
    activeDragHour: Int? = null,
    activeDragMinuteStr: String? = null,
) {
    val currentHour = remember {
        runCatching { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour }.getOrDefault(-1)
    }
    Column(modifier.width(style.timeColumnWidth)) {
        for (index in 0 until maxGridSections) {
            val isCurrentActive = if (is24HourMode) {
                currentHour == index && currentSectionIndex != -1
            } else {
                index + 1 == currentSectionIndex
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(style.sectionHeight)
                    .clickable(onClick = onTimeSlotClicked)
                    .background(if (isCurrentActive && showActiveBackground) MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                    .drawBehind {
                        if (!style.hideGridLines) {
                            drawLine(lineColor, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidthPx)
                            if (!is24HourMode) drawLine(lineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidthPx)
                        }
                    },
                contentAlignment = if (is24HourMode) Alignment.TopCenter else Alignment.Center,
            ) {
                val height = maxHeight
                if (is24HourMode && activeDragMinuteStr != null && index == activeDragHour) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = ":$activeDragMinuteStr",
                            fontSize = if (height < 32.dp) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentActive) activeTextColor else textColor,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (is24HourMode) Arrangement.Top else Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 1.dp).then(if (is24HourMode) Modifier.offset(y = (-7).dp) else Modifier),
                ) {
                    if (is24HourMode) {
                        Text(
                            text = "${index.toString().padStart(2, '0')}:00",
                            fontSize = if (height < 32.dp) 11.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCurrentActive) activeTextColor else textColor,
                        )
                    } else {
                        timeSlots.getOrNull(index)?.let { slot ->
                            Text(
                                text = slot.alias ?: slot.number.toString(),
                                fontSize = if (height < 32.dp) 11.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentActive) activeTextColor else textColor,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                            if (!style.hideSectionTime) {
                                when {
                                    height >= 52.dp -> {
                                        Spacer(Modifier.height(2.dp))
                                        MiuixTimeText(slot.startTime, if (isCurrentActive) activeTextColor else subTextColor)
                                        MiuixTimeText(slot.endTime, if (isCurrentActive) activeTextColor else subTextColor)
                                    }

                                    height >= 38.dp -> Text(
                                        text = "${slot.startTime}-${slot.endTime}",
                                        fontSize = 8.sp,
                                        color = if (isCurrentActive) activeTextColor else subTextColor,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixTimeText(text: String, color: Color) {
    Text(text = text, fontSize = 10.sp, color = color, style = TextStyle(lineHeight = 1.em), maxLines = 1)
}
