package com.wild0408.nanxinshiguang.ui.miuix.settings.style

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wild0408.nanxinshiguang.data.model.schedule_style.BorderTypeProto
import com.wild0408.nanxinshiguang.data.model.schedule_style.ScheduleModeProto
import com.wild0408.nanxinshiguang.ui.components.AdvancedColorPicker
import com.wild0408.nanxinshiguang.ui.components.ColorPickerConfig
import com.wild0408.nanxinshiguang.ui.schedule.MergedCourseBlock
import com.wild0408.nanxinshiguang.ui.schedule.WeeklyScheduleUiState
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ScheduleGrid
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ScheduleGridActions
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ScheduleGridStyleComposed
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ScheduleGridViewState
import com.wild0408.nanxinshiguang.ui.material.schedule.components.rememberScheduleGridState
import com.wild0408.nanxinshiguang.ui.settings.style.StyleSettingsViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.action_reset
import nanxinshiguang.generated.resources.action_reset_style
import nanxinshiguang.generated.resources.border_type_dashed
import nanxinshiguang.generated.resources.border_type_solid
import nanxinshiguang.generated.resources.check_24px
import nanxinshiguang.generated.resources.desc_wallpaper_set
import nanxinshiguang.generated.resources.desc_wallpaper_unset
import nanxinshiguang.generated.resources.dialog_reset_message
import nanxinshiguang.generated.resources.dialog_reset_title
import nanxinshiguang.generated.resources.format_week_display
import nanxinshiguang.generated.resources.image_24px
import nanxinshiguang.generated.resources.label_border_type
import nanxinshiguang.generated.resources.label_corner_radius
import nanxinshiguang.generated.resources.label_course_text_color
import nanxinshiguang.generated.resources.label_day_header_height
import nanxinshiguang.generated.resources.label_font_scale
import nanxinshiguang.generated.resources.label_hide_date_under_day
import nanxinshiguang.generated.resources.label_hide_grid_lines
import nanxinshiguang.generated.resources.label_hide_location
import nanxinshiguang.generated.resources.label_hide_section_time
import nanxinshiguang.generated.resources.label_hide_teacher
import nanxinshiguang.generated.resources.label_inner_padding
import nanxinshiguang.generated.resources.label_none
import nanxinshiguang.generated.resources.label_opacity
import nanxinshiguang.generated.resources.label_outer_padding
import nanxinshiguang.generated.resources.label_page_text_color
import nanxinshiguang.generated.resources.label_range
import nanxinshiguang.generated.resources.label_remove_location_at
import nanxinshiguang.generated.resources.label_schedule_mode_24h
import nanxinshiguang.generated.resources.label_section_height
import nanxinshiguang.generated.resources.label_show_start_time
import nanxinshiguang.generated.resources.label_text_align_center_h
import nanxinshiguang.generated.resources.label_text_align_center_v
import nanxinshiguang.generated.resources.label_time_column_width
import nanxinshiguang.generated.resources.label_wallpaper
import nanxinshiguang.generated.resources.placeholder_input_value
import nanxinshiguang.generated.resources.preview_dark_mode
import nanxinshiguang.generated.resources.preview_light_mode
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.status_not_set
import nanxinshiguang.generated.resources.style_category_color_scheme
import nanxinshiguang.generated.resources.style_category_course_block
import nanxinshiguang.generated.resources.style_category_grid_size
import nanxinshiguang.generated.resources.style_category_interface
import nanxinshiguang.generated.resources.title_dark_color_pool
import nanxinshiguang.generated.resources.title_light_color_pool
import kotlin.math.roundToInt
import kotlin.time.Clock

@Composable
fun MiuixSettingsListContent(
    currentStyle: ScheduleGridStyleComposed,
    viewModel: StyleSettingsViewModel,
    onWallpaperClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPick: (isDark: Boolean, index: Int) -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        top.yukonga.miuix.kmp.basic.TextButton(
            text = stringResource(Res.string.action_reset_style),
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColors(color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.error),
        )

        MiuixStyleGroup(stringResource(Res.string.style_category_interface)) {
            MiuixWallpaperItem(currentStyle.backgroundImagePath, onWallpaperClick) { viewModel.removeWallpaper() }
            MiuixStyleSwitchItem(stringResource(Res.string.label_schedule_mode_24h), currentStyle.scheduleMode == ScheduleModeProto.TIME_24H_MODE) {
                viewModel.updateScheduleMode(if (it) ScheduleModeProto.TIME_24H_MODE else ScheduleModeProto.SECTION_MODE)
            }
            MiuixStyleSwitchItem(stringResource(Res.string.label_hide_section_time), currentStyle.hideSectionTime, viewModel::updateHideSectionTime)
            MiuixStyleSwitchItem(stringResource(Res.string.label_hide_grid_lines), currentStyle.hideGridLines, viewModel::updateHideGridLines)
        }

        MiuixStyleGroup(stringResource(Res.string.style_category_grid_size)) {
            MiuixStyleSliderItem(stringResource(Res.string.label_section_height), currentStyle.sectionHeight.value, 40f..120f, 1f, viewModel::updateSectionHeight)
            MiuixStyleSliderItem(stringResource(Res.string.label_time_column_width), currentStyle.timeColumnWidth.value, 20f..80f, 1f, viewModel::updateTimeColumnWidth)
            MiuixStyleSliderItem(stringResource(Res.string.label_day_header_height), currentStyle.dayHeaderHeight.value, 30f..80f, 1f, viewModel::updateDayHeaderHeight)
        }

        MiuixStyleGroup(stringResource(Res.string.style_category_course_block)) {
            MiuixColorPickerItem(stringResource(Res.string.label_course_text_color), currentStyle.courseTextColor, viewModel::updateCourseTextColor) { viewModel.updateCourseTextColor(null) }
            MiuixStyleSwitchItem(stringResource(Res.string.label_show_start_time), currentStyle.showStartTime, viewModel::updateShowStartTime)
            MiuixStyleSwitchItem(stringResource(Res.string.label_hide_location), currentStyle.hideLocation, viewModel::updateHideLocation)
            MiuixStyleSwitchItem(stringResource(Res.string.label_hide_teacher), currentStyle.hideTeacher, viewModel::updateHideTeacher)
            MiuixStyleSwitchItem(stringResource(Res.string.label_remove_location_at), currentStyle.removeLocationAt, viewModel::updateRemoveLocationAt)
            MiuixStyleSwitchItem(stringResource(Res.string.label_text_align_center_h), currentStyle.textAlignCenterHorizontal, viewModel::updateTextAlignCenterHorizontal)
            MiuixStyleSwitchItem(stringResource(Res.string.label_text_align_center_v), currentStyle.textAlignCenterVertical, viewModel::updateTextAlignCenterVertical)
            MiuixBorderTypeSelector(currentStyle.borderType, viewModel::updateBorderType)
            MiuixStyleSliderItem(stringResource(Res.string.label_font_scale), currentStyle.fontScale, 0.5f..2f, 0.1f, viewModel::updateCourseBlockFontScale)
            MiuixStyleSliderItem(stringResource(Res.string.label_corner_radius), currentStyle.courseBlockCornerRadius.value, 0f..24f, 1f, viewModel::updateCornerRadius)
            MiuixStyleSliderItem(stringResource(Res.string.label_inner_padding), currentStyle.courseBlockInnerPadding.value, 0f..12f, 1f, viewModel::updateInnerPadding)
            MiuixStyleSliderItem(stringResource(Res.string.label_outer_padding), currentStyle.courseBlockOuterPadding.value, 0f..8f, 1f, viewModel::updateOuterPadding)
            MiuixStyleSliderItem(stringResource(Res.string.label_opacity), currentStyle.courseBlockAlpha, 0.1f..1f, 0.05f, viewModel::updateAlpha)
        }

        top.yukonga.miuix.kmp.basic.SmallTitle(stringResource(Res.string.style_category_color_scheme))
        MiuixColorSchemeSection(stringResource(Res.string.title_light_color_pool), false, currentStyle.courseColorMaps.map { it.light }) { onPick(false, it) }
        MiuixColorSchemeSection(stringResource(Res.string.title_dark_color_pool), true, currentStyle.courseColorMaps.map { it.dark }) { onPick(true, it) }
        Spacer(Modifier.height(16.dp))
    }

    top.yukonga.miuix.kmp.overlay.OverlayDialog(
        title = stringResource(Res.string.dialog_reset_title),
        summary = stringResource(Res.string.dialog_reset_message),
        show = showResetDialog,
        onDismissRequest = { showResetDialog = false },
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            top.yukonga.miuix.kmp.basic.TextButton(stringResource(Res.string.action_cancel), { showResetDialog = false }, Modifier.weight(1f))
            top.yukonga.miuix.kmp.basic.TextButton(
                text = stringResource(Res.string.action_confirm),
                onClick = { viewModel.resetStyleSettings(); showResetDialog = false },
                modifier = Modifier.weight(1f),
                colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColors(color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.error),
            )
        }
    }
}
@Composable
private fun MiuixStyleGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    top.yukonga.miuix.kmp.basic.SmallTitle(title)
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(vertical = 4.dp),
        content = content,
    )
}

@Composable
private fun MiuixStyleSwitchItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    top.yukonga.miuix.kmp.basic.BasicComponent(
        modifier = Modifier.fillMaxWidth(),
        title = label,
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        endActions = { top.yukonga.miuix.kmp.basic.Switch(checked, onCheckedChange) },
        onClick = { onCheckedChange(!checked) },
    )
}

@Composable
private fun MiuixWallpaperItem(path: String, onClick: () -> Unit, onLongClick: () -> Unit) {
    val hasWallpaper = path.isNotEmpty()
    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            top.yukonga.miuix.kmp.basic.Text(stringResource(Res.string.label_wallpaper), style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body1)
            top.yukonga.miuix.kmp.basic.Text(
                if (hasWallpaper) stringResource(Res.string.desc_wallpaper_set) else stringResource(Res.string.desc_wallpaper_unset),
                style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote1,
                color = if (hasWallpaper) top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary else top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        if (hasWallpaper) {
            AsyncImage(
                model = path,
                contentDescription = null,
                modifier = Modifier.size(width = 52.dp, height = 38.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            top.yukonga.miuix.kmp.basic.Icon(
                vectorResource(Res.drawable.image_24px),
                null,
                tint = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun MiuixStyleSliderItem(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    stepValue: Float,
    onValueChange: (Float) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var input by remember(value, showDialog) { mutableStateOf(if (stepValue >= 1f) value.toInt().toString() else "%.2f".format(value).trimEnd('0').trimEnd('.')) }
    val steps = (((range.endInclusive - range.start) / stepValue).roundToInt() - 1).coerceAtLeast(0)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            top.yukonga.miuix.kmp.basic.Text(label, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body1)
            top.yukonga.miuix.kmp.basic.TextButton(
                text = input,
                onClick = { showDialog = true },
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        top.yukonga.miuix.kmp.basic.Slider(
            value,
            onValueChange,
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            valueRange = range,
            steps = steps,
        )
    }
    top.yukonga.miuix.kmp.overlay.OverlayDialog(title = label, summary = "${stringResource(Res.string.label_range)}: ${range.start} - ${range.endInclusive}", show = showDialog, onDismissRequest = { showDialog = false }) {
        Column(Modifier.fillMaxWidth()) {
            top.yukonga.miuix.kmp.basic.TextField(input, { input = it.filter { char -> char.isDigit() || char == '.' } }, Modifier.fillMaxWidth(), label = stringResource(Res.string.placeholder_input_value), singleLine = true)
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                top.yukonga.miuix.kmp.basic.TextButton(stringResource(Res.string.action_cancel), { showDialog = false }, Modifier.weight(1f))
                top.yukonga.miuix.kmp.basic.TextButton(stringResource(Res.string.action_confirm), {
                    input.toFloatOrNull()?.let { raw ->
                        val clamped = raw.coerceIn(range.start, range.endInclusive)
                        val snapped = range.start + ((clamped - range.start) / stepValue).roundToInt() * stepValue
                        onValueChange(snapped); showDialog = false
                    }
                }, Modifier.weight(1f), colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable
private fun MiuixBorderTypeSelector(currentType: BorderTypeProto, onTypeChange: (BorderTypeProto) -> Unit) {
    val types = listOf(BorderTypeProto.BORDER_TYPE_NONE, BorderTypeProto.BORDER_TYPE_SOLID, BorderTypeProto.BORDER_TYPE_DASHED)
    val labels = listOf(stringResource(Res.string.label_none), stringResource(Res.string.border_type_solid), stringResource(Res.string.border_type_dashed))
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        top.yukonga.miuix.kmp.basic.Text(stringResource(Res.string.label_border_type), style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            labels.forEachIndexed { index, label ->
                val selected = types[index] == currentType
                top.yukonga.miuix.kmp.basic.Button(
                    onClick = { onTypeChange(types[index]) },
                    modifier = Modifier.weight(1f),
                    minHeight = 40.dp,
                    insideMargin = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                        color = if (selected) top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary
                        else top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (selected) top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onPrimary
                        else top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurface,
                    ),
                ) { top.yukonga.miuix.kmp.basic.Text(label) }
            }
        }
    }
}

@Composable
private fun MiuixColorPickerItem(label: String, currentColor: Color?, onColorChanged: (Color) -> Unit, onReset: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    top.yukonga.miuix.kmp.basic.BasicComponent(
        modifier = Modifier.fillMaxWidth(),
        title = label,
        summary = if (currentColor == null) stringResource(Res.string.status_not_set) else null,
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        endActions = { Box(Modifier.size(28.dp).clip(CircleShape).background(currentColor ?: top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainerHigh)) },
        onClick = { showDialog = true },
    )
    top.yukonga.miuix.kmp.overlay.OverlayDialog(title = label, show = showDialog, onDismissRequest = { showDialog = false }) {
        Column(Modifier.fillMaxWidth()) {
            AdvancedColorPicker(currentColor ?: top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary, onColorChanged, ColorPickerConfig(showAlpha = false, showInputMode = true))
            top.yukonga.miuix.kmp.basic.TextButton(stringResource(Res.string.action_reset), { onReset(); showDialog = false }, Modifier.fillMaxWidth(), colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary())
        }
    }
}

@Composable
private fun MiuixColorSchemeSection(title: String, dark: Boolean, colors: List<Color>, onEditColor: (Int) -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = if (dark) Color(0xFF242424) else Color.White),
    ) {
        top.yukonga.miuix.kmp.basic.Text(title, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body1, color = if (dark) Color.White else Color.Black)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            colors.forEachIndexed { index, color ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(color).clickable { onEditColor(index) })
                    top.yukonga.miuix.kmp.basic.Text("${index + 1}", style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote1, color = if (dark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f))
                }
            }
        }
    }
}
