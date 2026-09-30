package com.wild0408.nanxinshiguang.ui.miuix.settings.quickactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.data.db.main.CourseWithWeeks
import com.wild0408.nanxinshiguang.data.repository.CourseTableRepository.TweakMode
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixCourseTablePickerDialog
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixDatePickerDialog
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.settings.quickactions.tweaks.TweakScheduleViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.a11y_save_tweak
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.arrow_downward_24px
import nanxinshiguang.generated.resources.arrow_forward_24px
import nanxinshiguang.generated.resources.check_24px
import nanxinshiguang.generated.resources.course_time_day_section_details_tweak
import nanxinshiguang.generated.resources.course_time_day_time_details_tweak
import nanxinshiguang.generated.resources.date_format_month_day
import nanxinshiguang.generated.resources.dialog_title_select_tweak_from_date
import nanxinshiguang.generated.resources.dialog_title_select_tweak_to_date
import nanxinshiguang.generated.resources.double_arrow_24px
import nanxinshiguang.generated.resources.label_select_tweak_table
import nanxinshiguang.generated.resources.label_tweak_from_date
import nanxinshiguang.generated.resources.label_tweak_to_date
import nanxinshiguang.generated.resources.sync_alt_24px
import nanxinshiguang.generated.resources.text_error
import nanxinshiguang.generated.resources.text_no_course
import nanxinshiguang.generated.resources.text_tweak_hint
import nanxinshiguang.generated.resources.title_select_tweak_mode
import nanxinshiguang.generated.resources.title_tweak_from_course
import nanxinshiguang.generated.resources.title_tweak_schedule
import nanxinshiguang.generated.resources.title_tweak_to_course
import nanxinshiguang.generated.resources.tweak_mode_exchange
import nanxinshiguang.generated.resources.tweak_mode_merge
import nanxinshiguang.generated.resources.tweak_mode_overwrite
import nanxinshiguang.generated.resources.week_days_full_names
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.time.Instant

@Composable
internal fun MiuixTweakScheduleScreen(
    onBack: () -> Unit,
    viewModel: TweakScheduleViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showTablePicker by remember { mutableStateOf(false) }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showModePicker by remember { mutableStateOf(false) }

    val error = state.errorMessage?.let { message ->
        if (message.args.isEmpty()) stringResource(message.resource) else stringResource(message.resource, *message.args.toTypedArray())
    }
    val success = state.successMessage?.let { message ->
        if (message.args.isEmpty()) stringResource(message.resource) else stringResource(message.resource, *message.args.toTypedArray())
    }
    LaunchedEffect(error, success) {
        (error ?: success)?.let(ToastManager::show)
        if (error != null || success != null) viewModel.resetMessages()
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
                title = stringResource(Res.string.title_tweak_schedule),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
                endAction = { a, s -> HyperLiquidTopBarButton(viewModel::moveCourses, backdrop, vectorResource(Res.drawable.check_24px), stringResource(Res.string.a11y_save_tweak), backdropAlpha = a, shadowAlpha = s) },
            )
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
            item {
                BasicComponent(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.label_select_tweak_table),
                    summary = state.selectedCourseTable?.name ?: stringResource(Res.string.label_select_tweak_table),
                    onClick = { showTablePicker = true },
                    endActions = { Icon(vectorResource(Res.drawable.arrow_forward_24px), null, tint = MiuixTheme.colorScheme.onSurfaceVariantActions) },
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateComponent(stringResource(Res.string.label_tweak_from_date), state.fromDate, Modifier.weight(1f)) { showFromDatePicker = true }
                    DateComponent(stringResource(Res.string.label_tweak_to_date), state.toDate, Modifier.weight(1f)) { showToDatePicker = true }
                }
            }
            item {
                BasicComponent(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.title_select_tweak_mode),
                    summary = tweakModeLabel(state.tweakMode),
                    onClick = { showModePicker = true },
                    startAction = { Icon(tweakModeIcon(state.tweakMode), null, tint = MiuixTheme.colorScheme.primary) },
                    endActions = { Icon(vectorResource(Res.drawable.arrow_forward_24px), null, tint = MiuixTheme.colorScheme.onSurfaceVariantActions) },
                )
            }
            item {
                Text(
                    text = stringResource(Res.string.text_tweak_hint),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            item { CoursePreview(stringResource(Res.string.title_tweak_from_course), state.fromCourses) }
            item {
                Icon(
                    imageVector = vectorResource(Res.drawable.arrow_downward_24px),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().size(28.dp),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
            item { CoursePreview(stringResource(Res.string.title_tweak_to_course), state.toCourses) }
            }
        }
        if (showTablePicker) {
            MiuixCourseTablePickerDialog(
                title = stringResource(Res.string.label_select_tweak_table),
                onDismissRequest = { showTablePicker = false },
                onTableSelected = { viewModel.onCourseTableSelected(it); showTablePicker = false },
            )
        }
        if (showFromDatePicker) {
            MiuixDatePickerDialog(
                onDateSelected = { it?.let { value -> viewModel.onFromDateSelected(value.toLocalDate()) } },
                onDismiss = { showFromDatePicker = false },
                title = stringResource(Res.string.dialog_title_select_tweak_from_date),
            )
        }
        if (showToDatePicker) {
            MiuixDatePickerDialog(
                onDateSelected = { it?.let { value -> viewModel.onToDateSelected(value.toLocalDate()) } },
                onDismiss = { showToDatePicker = false },
                title = stringResource(Res.string.dialog_title_select_tweak_to_date),
            )
        }
        if (showModePicker) {
            WindowDialog(
                show = true,
                title = stringResource(Res.string.title_select_tweak_mode),
                onDismissRequest = { showModePicker = false },
                insideMargin = DpSize(16.dp, 16.dp),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    TweakMode.entries.forEach { mode ->
                        BasicComponent(
                            title = tweakModeLabel(mode),
                            onClick = { viewModel.onTweakModeChanged(mode); showModePicker = false },
                            startAction = { Icon(tweakModeIcon(mode), null, tint = MiuixTheme.colorScheme.primary) },
                            endActions = {
                                if (mode == state.tweakMode) Icon(vectorResource(Res.drawable.check_24px), null, tint = MiuixTheme.colorScheme.primary)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateComponent(label: String, date: LocalDate, modifier: Modifier, onClick: () -> Unit) {
    BasicComponent(
        modifier = modifier,
        title = label,
        summary = stringResource(Res.string.date_format_month_day, date.month.number, date.day),
        onClick = onClick,
    )
}

@Composable
private fun CoursePreview(title: String, courses: List<CourseWithWeeks>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallTitle(title)
            if (courses.isEmpty()) {
                Text(stringResource(Res.string.text_no_course), style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            } else {
                courses.forEach { item ->
                    val course = item.course
                    val day = localizedDay(course.day)
                    val detail = if (course.isCustomTime) {
                        stringResource(Res.string.course_time_day_time_details_tweak, day, course.customStartTime ?: "--:--", course.customEndTime ?: "--:--")
                    } else {
                        stringResource(Res.string.course_time_day_section_details_tweak, day, (course.startSection ?: 0).toString(), (course.endSection ?: 0).toString())
                    }
                    Column {
                        Text(course.name, style = MiuixTheme.textStyles.body1, fontWeight = FontWeight.Medium)
                        Text(detail, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
            }
        }
    }
}

@Composable
private fun tweakModeLabel(mode: TweakMode): String = when (mode) {
    TweakMode.MERGE -> stringResource(Res.string.tweak_mode_merge)
    TweakMode.OVERWRITE -> stringResource(Res.string.tweak_mode_overwrite)
    TweakMode.EXCHANGE -> stringResource(Res.string.tweak_mode_exchange)
}

@Composable
private fun tweakModeIcon(mode: TweakMode): ImageVector = when (mode) {
    TweakMode.MERGE -> vectorResource(Res.drawable.arrow_forward_24px)
    TweakMode.OVERWRITE -> vectorResource(Res.drawable.double_arrow_24px)
    TweakMode.EXCHANGE -> vectorResource(Res.drawable.sync_alt_24px)
}

@Composable
private fun localizedDay(day: Int): String {
    val days = stringArrayResource(Res.array.week_days_full_names)
    return if (day in 1..7) days[day - 1] else stringResource(Res.string.text_error)
}

private fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
