package com.wild0408.nanxinshiguang.ui.miuix.settings.quickactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixDatePickerDialog
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.quickactions.AffectedCourseItem
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.quickactions.QuickDeleteViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.action_deselect_all
import nanxinshiguang.generated.resources.action_select_all
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.calendar_today_24px
import nanxinshiguang.generated.resources.check_24px
import nanxinshiguang.generated.resources.close_24px
import nanxinshiguang.generated.resources.confirm_delete
import nanxinshiguang.generated.resources.course_time_day_section_details_tweak
import nanxinshiguang.generated.resources.course_time_day_time_details_tweak
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.dialog_delete_confirm_msg
import nanxinshiguang.generated.resources.filter_list_24px
import nanxinshiguang.generated.resources.hint_affected_count
import nanxinshiguang.generated.resources.hint_no_selection
import nanxinshiguang.generated.resources.item_quick_delete
import nanxinshiguang.generated.resources.label_day_of_week
import nanxinshiguang.generated.resources.label_dimension_dates
import nanxinshiguang.generated.resources.label_dimension_weeks_days
import nanxinshiguang.generated.resources.label_none
import nanxinshiguang.generated.resources.label_weeks_format
import nanxinshiguang.generated.resources.quick_delete_dialog_select_date_title
import nanxinshiguang.generated.resources.quick_delete_filter_date_range_hint
import nanxinshiguang.generated.resources.quick_delete_filter_weeks_days_hint
import nanxinshiguang.generated.resources.quick_delete_label_days_prefix
import nanxinshiguang.generated.resources.title_current_week
import nanxinshiguang.generated.resources.title_select_weeks
import nanxinshiguang.generated.resources.week_days_full_names
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.time.Instant

@Composable
internal fun MiuixQuickDeleteScreen(
    onBack: () -> Unit,
    viewModel: QuickDeleteViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val weekDays = stringArrayResource(Res.array.week_days_full_names)
    var showWeekPicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<LocalDate?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    val success = state.successMessage?.let { message -> stringResource(message.resource, *message.args.toTypedArray()) }
    val error = state.errorMessage?.let { message -> stringResource(message.resource, *message.args.toTypedArray()) }
    LaunchedEffect(success, error) {
        (success ?: error)?.let(ToastManager::show)
        if (success != null || error != null) viewModel.resetMessages()
    }

    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val direction = LocalLayoutDirection.current
    val hostPadding = LocalNavigationHostPadding.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = stringResource(Res.string.item_quick_delete),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
            )
        },
        bottomBar = {
            if (state.affectedCourses.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                    insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    TextButton(
                        text = stringResource(Res.string.confirm_delete),
                        onClick = { showConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.textButtonColors(color = MiuixTheme.colorScheme.error),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = innerPadding.calculateLeftPadding(direction) + 20.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                end = innerPadding.calculateRightPadding(direction) + 20.dp,
                bottom = innerPadding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SmallTitle(stringResource(Res.string.label_dimension_weeks_days), modifier = Modifier.padding(horizontal = 12.dp)) }
            item {
                BasicComponent(
                    modifier = Modifier.fillMaxWidth(),
                    title = if (state.selectedWeeks.isEmpty()) stringResource(Res.string.quick_delete_filter_weeks_days_hint) else stringResource(Res.string.label_weeks_format, state.selectedWeeks.sorted().joinToString(", ")),
                    summary = if (state.selectedDays.isEmpty()) stringResource(Res.string.label_day_of_week) else stringResource(Res.string.quick_delete_label_days_prefix, state.selectedDays.sorted().joinToString("、") { weekDays[it - 1] }),
                    onClick = { showWeekPicker = true },
                    startAction = { Icon(vectorResource(Res.drawable.filter_list_24px), null, tint = MiuixTheme.colorScheme.primary) },
                    endActions = {
                        if (state.selectedWeeks.isNotEmpty() || state.selectedDays.isNotEmpty()) {
                            IconButton({ viewModel.clearWeeksAndDays() }) { Icon(vectorResource(Res.drawable.close_24px), null, tint = MiuixTheme.colorScheme.onSurfaceVariantActions) }
                        }
                    },
                )
            }
            item { SmallTitle(stringResource(Res.string.label_dimension_dates), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
            item {
                BasicComponent(
                    modifier = Modifier.fillMaxWidth(),
                    title = state.startDate?.let { start -> state.endDate?.let { end -> "$start - $end" } } ?: stringResource(Res.string.quick_delete_filter_date_range_hint),
                    summary = stringResource(Res.string.quick_delete_dialog_select_date_title),
                    onClick = { showStartPicker = true },
                    startAction = { Icon(vectorResource(Res.drawable.calendar_today_24px), null, tint = MiuixTheme.colorScheme.primary) },
                    endActions = {
                        if (state.startDate != null) IconButton({ viewModel.clearDateRange() }) { Icon(vectorResource(Res.drawable.close_24px), null, tint = MiuixTheme.colorScheme.onSurfaceVariantActions) }
                    },
                )
            }
            item {
                val count = state.affectedCourses.size
                Text(
                    text = if (count > 0) stringResource(Res.string.hint_affected_count, count) else stringResource(Res.string.hint_no_selection),
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.SemiBold,
                    color = if (count > 0) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            items(state.affectedCourses) { PreviewCourse(it, weekDays) }
            item { Spacer(Modifier.height(12.dp)) }
        }
        }

    if (showWeekPicker) {
        WindowDialog(
            show = true,
            title = stringResource(Res.string.title_select_weeks),
            onDismissRequest = { showWeekPicker = false },
            insideMargin = DpSize(16.dp, 16.dp),
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..20).forEach { week -> SelectTile(week.toString(), week in state.selectedWeeks) { viewModel.toggleWeek(week) } }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SmallTitle(stringResource(Res.string.label_day_of_week), modifier = Modifier.weight(1f))
                    TextButton(if (state.selectedDays.size == 7) stringResource(Res.string.action_deselect_all) else stringResource(Res.string.action_select_all), { if (state.selectedDays.size == 7) viewModel.clearAllDays() else viewModel.selectAllDays() })
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..7).forEach { day -> SelectTile(weekDays[day - 1], day in state.selectedDays, wide = true) { viewModel.toggleDay(day) } }
                }
                TextButton(stringResource(Res.string.action_confirm), { showWeekPicker = false }, Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
    if (showStartPicker) {
        MiuixDatePickerDialog(
            title = stringResource(Res.string.quick_delete_dialog_select_date_title),
            onDismiss = { showStartPicker = false },
            onDateSelected = { millis ->
                pendingStart = millis?.toLocalDate()
                showStartPicker = false
                if (pendingStart != null) showEndPicker = true
            },
        )
    }
    if (showEndPicker) {
        MiuixDatePickerDialog(
            title = stringResource(Res.string.quick_delete_dialog_select_date_title),
            onDismiss = { showEndPicker = false; pendingStart = null },
            onDateSelected = { millis ->
                val first = pendingStart
                val second = millis?.toLocalDate()
                if (first != null && second != null) {
                    if (first <= second) viewModel.setDateRange(first, second) else viewModel.setDateRange(second, first)
                }
                showEndPicker = false
                pendingStart = null
            },
        )
    }
    if (showConfirm) {
        WindowDialog(
            show = true,
            title = stringResource(Res.string.confirm_delete),
            summary = stringResource(Res.string.dialog_delete_confirm_msg),
            onDismissRequest = { showConfirm = false },
            insideMargin = DpSize(16.dp, 16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(stringResource(Res.string.action_cancel), { showConfirm = false }, Modifier.weight(1f))
                TextButton(stringResource(Res.string.action_confirm), { showConfirm = false; viewModel.executeDelete() }, Modifier.weight(1f), colors = ButtonDefaults.textButtonColors(color = MiuixTheme.colorScheme.error))
            }
        }
    }
    }
}

@Composable
private fun SelectTile(text: String, selected: Boolean, wide: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(if (wide) 54.dp else 42.dp, 42.dp).clickable(onClick = onClick),
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, color = if (selected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface, style = MiuixTheme.textStyles.body2)
        }
    }
}

@Composable
private fun PreviewCourse(item: AffectedCourseItem, weekDays: List<String>) {
    val course = item.courseWithWeeks.course
    val day = weekDays.getOrElse(course.day - 1) { "" }
    val detail = if (course.isCustomTime) {
        stringResource(Res.string.course_time_day_time_details_tweak, day, course.customStartTime ?: stringResource(Res.string.label_none), course.customEndTime ?: stringResource(Res.string.label_none))
    } else {
        stringResource(Res.string.course_time_day_section_details_tweak, day, (course.startSection ?: 0).toString(), (course.endSection ?: 0).toString())
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.18f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(course.name, style = MiuixTheme.textStyles.body1, fontWeight = FontWeight.SemiBold, color = MiuixTheme.colorScheme.error)
                Text("${stringResource(Res.string.title_current_week, item.targetWeek.toString())} · $detail", style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            Icon(vectorResource(Res.drawable.delete_24px), null, tint = MiuixTheme.colorScheme.error)
        }
    }
}

private fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
