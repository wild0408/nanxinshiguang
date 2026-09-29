package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.data.db.main.TimeSlot
import com.wild0408.nanxinshiguang.data.db.main.TimeTable
import com.wild0408.nanxinshiguang.data.db.main.TimeTableComboRule
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixDatePickerDialog
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.settings.time.ComboScheduleEditUiState
import com.wild0408.nanxinshiguang.ui.settings.time.ComboScheduleEditViewModel
import com.wild0408.nanxinshiguang.ui.settings.time.SingleScheduleEditViewModel
import com.wild0408.nanxinshiguang.ui.settings.time.components.calculateInitialTimes
import com.wild0408.nanxinshiguang.ui.settings.time.components.formatTime
import com.wild0408.nanxinshiguang.ui.settings.time.components.parseLocalTimeSafely
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_add_time_slot
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.a11y_delete
import nanxinshiguang.generated.resources.a11y_delete_rule
import nanxinshiguang.generated.resources.a11y_save
import nanxinshiguang.generated.resources.a11y_save_all_settings
import nanxinshiguang.generated.resources.action_add
import nanxinshiguang.generated.resources.action_add_time_range_rule
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.action_save_changes
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.common_action_continue_editing
import nanxinshiguang.generated.resources.common_action_exit_without_save
import nanxinshiguang.generated.resources.common_dialog_msg_unsaved_changes
import nanxinshiguang.generated.resources.common_dialog_title_abandon_changes
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.check_24px
import nanxinshiguang.generated.resources.dialog_title_add_time_slot
import nanxinshiguang.generated.resources.dialog_title_edit_time_slot
import nanxinshiguang.generated.resources.dialog_title_select_effective_date_range
import nanxinshiguang.generated.resources.error_name_cannot_be_empty
import nanxinshiguang.generated.resources.error_rule_date_overlap
import nanxinshiguang.generated.resources.label_applied_schedule
import nanxinshiguang.generated.resources.label_base_schedule
import nanxinshiguang.generated.resources.label_break_duration_minutes
import nanxinshiguang.generated.resources.label_class_duration_minutes
import nanxinshiguang.generated.resources.label_effective_date_range
import nanxinshiguang.generated.resources.label_schedule_scheme_name
import nanxinshiguang.generated.resources.label_time_picker_end
import nanxinshiguang.generated.resources.label_time_picker_hour
import nanxinshiguang.generated.resources.label_time_picker_minute
import nanxinshiguang.generated.resources.label_time_picker_start
import nanxinshiguang.generated.resources.label_time_slot_alias
import nanxinshiguang.generated.resources.msg_please_create_public_schedule_first
import nanxinshiguang.generated.resources.option_exclusive_schedule
import nanxinshiguang.generated.resources.option_select_base_schedule
import nanxinshiguang.generated.resources.option_select_schedule_table
import nanxinshiguang.generated.resources.option_unnamed_schedule
import nanxinshiguang.generated.resources.text_no_time_slots_hint
import nanxinshiguang.generated.resources.title_add_combo_schedule
import nanxinshiguang.generated.resources.title_default_duration_settings
import nanxinshiguang.generated.resources.title_edit_combo_schedule
import nanxinshiguang.generated.resources.title_edit_exclusive_schedule
import nanxinshiguang.generated.resources.title_edit_public_schedule
import nanxinshiguang.generated.resources.title_rule_list_by_date
import nanxinshiguang.generated.resources.toast_break_duration_non_negative
import nanxinshiguang.generated.resources.toast_class_duration_positive
import nanxinshiguang.generated.resources.toast_end_time_must_be_later
import nanxinshiguang.generated.resources.toast_settings_saved
import nanxinshiguang.generated.resources.toast_slot_added_unsaved
import nanxinshiguang.generated.resources.toast_slot_modified_unsaved
import nanxinshiguang.generated.resources.toast_slot_removed_unsaved
import nanxinshiguang.generated.resources.toast_time_conflict
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.math.max

@Composable
internal fun MiuixSingleScheduleEditScreen(
    tableId: String?,
    isPublic: Boolean,
    copyFromId: String?,
    onBack: () -> Unit,
    viewModel: SingleScheduleEditViewModel = koinViewModel { parametersOf(tableId ?: "", isPublic, copyFromId ?: "") },
) {
    val state by viewModel.uiState.collectAsState()
    if (!state.isDataLoaded) return LoadingMiuixScreen()

    val slots = remember { mutableStateListOf<TimeSlot>().apply { addAll(state.slots.sortedBy { it.number }) } }
    var name by remember { mutableStateOf(state.name) }
    var classDuration by remember { mutableIntStateOf(state.defaultClassDuration) }
    var breakDuration by remember { mutableIntStateOf(state.defaultBreakDuration) }
    var editorSlot by remember { mutableStateOf<TimeSlot?>(null) }
    var showSlotEditor by remember { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDataLoaded) {
        slots.clear(); slots.addAll(state.slots.sortedBy { it.number })
        name = state.name; classDuration = state.defaultClassDuration; breakDuration = state.defaultBreakDuration
    }
    val savedMessage = stringResource(Res.string.toast_settings_saved)
    val removedMessage = stringResource(Res.string.toast_slot_removed_unsaved)
    val modifiedMessage = stringResource(Res.string.toast_slot_modified_unsaved)
    val addedMessage = stringResource(Res.string.toast_slot_added_unsaved)
    LaunchedEffect(state.isSaved) { if (state.isSaved) { ToastManager.show(savedMessage); onBack() } }
    LaunchedEffect(state.errorMsg) { state.errorMsg?.takeIf { it.isNotBlank() }?.let { ToastManager.show(it); viewModel.clearError() } }

    val validName = !isPublic || name.isNotBlank()
    val changed = (isPublic && name != state.name) || slots.toList() != state.slots.sortedBy { it.number } ||
        classDuration != state.defaultClassDuration || breakDuration != state.defaultBreakDuration
    val requestBack = { if (changed) showExit = true else onBack() }
    val navState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(state = navState, isBackEnabled = true, onBackCompleted = requestBack)

    val title = stringResource(if (isPublic) Res.string.title_edit_public_schedule else Res.string.title_edit_exclusive_schedule)
    val background = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val scrollBehavior = rememberSharedScrollBehavior()
    val direction = LocalLayoutDirection.current
    val hostPadding = LocalNavigationHostPadding.current
    Scaffold(
        modifier = Modifier.fillMaxSize(), containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title, backdrop = backdrop, scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(requestBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
                endAction = { a, s -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    HyperLiquidTopBarButton({ editorSlot = null; showSlotEditor = true }, backdrop, vectorResource(Res.drawable.add_24px), stringResource(Res.string.a11y_add_time_slot), backdropAlpha = a, shadowAlpha = s)
                    HyperLiquidTopBarButton({
                        val normalized = slots.sortedBy { parseLocalTimeSafely(it.startTime) }.mapIndexed { i, slot -> slot.copy(number = i + 1) }
                        viewModel.onDefaultDurationChange(classDuration, breakDuration)
                        viewModel.saveWithData(name, classDuration, breakDuration, normalized)
                    }, backdrop, vectorResource(Res.drawable.check_24px), stringResource(Res.string.a11y_save_all_settings), backdropAlpha = a, shadowAlpha = s)
                } },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyColumn(
                Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(start = padding.calculateLeftPadding(direction) + 20.dp, top = padding.calculateTopPadding() + 12.dp, end = padding.calculateRightPadding(direction) + 20.dp, bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isPublic) item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
                        TextField(name, { name = it }, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_schedule_scheme_name), singleLine = true)
                        if (!validName) Text(stringResource(Res.string.error_name_cannot_be_empty), color = MiuixTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
                        Text(stringResource(Res.string.title_default_duration_settings), style = MiuixTheme.textStyles.title3)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DurationField(stringResource(Res.string.label_class_duration_minutes), classDuration, 1, stringResource(Res.string.toast_class_duration_positive), Modifier.weight(1f)) { classDuration = it }
                            DurationField(stringResource(Res.string.label_break_duration_minutes), breakDuration, 0, stringResource(Res.string.toast_break_duration_non_negative), Modifier.weight(1f)) { breakDuration = it }
                        }
                    }
                }
                if (slots.isEmpty()) item { EmptyTimeSlots() }
                itemsIndexed(slots, key = { _, slot -> "${slot.number}-${slot.startTime}-${slot.endTime}" }) { _, slot ->
                    MiuixTimeSlotCard(slot, { editorSlot = slot; showSlotEditor = true }, {
                        slots.removeAll { it.number == slot.number }
                        val renumbered = slots.sortedBy { parseLocalTimeSafely(it.startTime) }.mapIndexed { i, item -> item.copy(number = i + 1) }
                        slots.clear(); slots.addAll(renumbered); ToastManager.show(removedMessage)
                    })
                }
            }
            if (showSlotEditor) {
                val editing = editorSlot != null
                val initial = calculateInitialTimes(editing, editorSlot, slots, breakDuration, classDuration)
                MiuixTimeSlotDialog(
                    existing = slots.toList(), initialNumber = editorSlot?.number ?: ((slots.maxOfOrNull { it.number } ?: 0) + 1),
                    initialStart = initial.first, initialEnd = initial.second, initialAlias = editorSlot?.alias, editing = editing,
                    onDismiss = { showSlotEditor = false; editorSlot = null },
                    onConfirm = { number, start, end, alias ->
                        val replacement = TimeSlot(timeTableId = tableId ?: "", number = number, startTime = start, endTime = end, alias = alias)
                        val updated = slots.toMutableList()
                        val idx = editorSlot?.let { value -> updated.indexOfFirst { it.number == value.number } } ?: -1
                        if (idx >= 0) { updated[idx] = replacement; ToastManager.show(modifiedMessage) } else { updated.add(replacement); ToastManager.show(addedMessage) }
                        val normalized = updated.sortedBy { parseLocalTimeSafely(it.startTime) }.mapIndexed { i, item -> item.copy(number = i + 1) }
                        slots.clear(); slots.addAll(normalized); showSlotEditor = false; editorSlot = null
                    },
                )
            }
            if (showExit) OverlayDialog(show = true, title = stringResource(Res.string.common_dialog_title_abandon_changes), onDismissRequest = { showExit = false }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.common_dialog_msg_unsaved_changes), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(stringResource(Res.string.common_action_continue_editing), { showExit = false }, Modifier.weight(1f))
                        TextButton(stringResource(Res.string.common_action_exit_without_save), { showExit = false; onBack() }, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable private fun LoadingMiuixScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }

@Composable private fun DurationField(label: String, value: Int, minimum: Int, error: String, modifier: Modifier = Modifier, onValue: (Int) -> Unit) {
    TextField(value = if (value < 0) "" else value.toString(), onValueChange = { raw ->
        if (raw.isBlank()) onValue(if (minimum == 0) -1 else 0) else raw.toIntOrNull()?.let { if (it >= minimum) onValue(it) else ToastManager.show(error) }
    }, modifier = modifier, label = label, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

@Composable private fun EmptyTimeSlots() { Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) { Text(stringResource(Res.string.text_no_time_slots_hint), color = MiuixTheme.colorScheme.onSurfaceVariantSummary) } }

@Composable private fun MiuixTimeSlotCard(slot: TimeSlot, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onEdit), insideMargin = PaddingValues(start = 18.dp, top = 14.dp, end = 8.dp, bottom = 14.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("第 ${slot.number} 节", style = MiuixTheme.textStyles.title3)
                Text(slot.alias.orEmpty().ifBlank { "未设置别名" }, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("${slot.startTime} - ${slot.endTime}", style = MiuixTheme.textStyles.body2)
            IconButton(onClick = onDelete) { Icon(vectorResource(Res.drawable.delete_24px), stringResource(Res.string.a11y_delete), tint = MiuixTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun MiuixTimeSlotDialog(
    existing: List<TimeSlot>, initialNumber: Int, initialStart: String, initialEnd: String, initialAlias: String?, editing: Boolean,
    onDismiss: () -> Unit, onConfirm: (Int, String, String, String?) -> Unit,
) {
    val start = remember { parseTime(initialStart) }; val end = remember { parseTime(initialEnd) }
    var startHour by remember { mutableIntStateOf(start.first) }; var startMinute by remember { mutableIntStateOf(start.second) }
    var endHour by remember { mutableIntStateOf(end.first) }; var endMinute by remember { mutableIntStateOf(end.second) }
    var alias by remember { mutableStateOf(initialAlias.orEmpty()) }
    val invalidOrderMessage = stringResource(Res.string.toast_end_time_must_be_later)
    val conflictMessage = stringResource(Res.string.toast_time_conflict)
    WindowDialog(show = true, title = stringResource(if (editing) Res.string.dialog_title_edit_time_slot else Res.string.dialog_title_add_time_slot), onDismissRequest = onDismiss, insideMargin = DpSize(16.dp, 16.dp)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(alias, { if (it.length <= 5) alias = it }, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_time_slot_alias), singleLine = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimePickerGroup(stringResource(Res.string.label_time_picker_start), startHour, startMinute, { startHour = it }, { startMinute = it }, Modifier.weight(1f))
                TimePickerGroup(stringResource(Res.string.label_time_picker_end), endHour, endMinute, { endHour = it }, { endMinute = it }, Modifier.weight(1f))
            }
            Text("${two(startHour)}:${two(startMinute)} - ${two(endHour)}:${two(endMinute)}", style = MiuixTheme.textStyles.title3, modifier = Modifier.align(Alignment.CenterHorizontally))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(stringResource(Res.string.action_cancel), onDismiss, Modifier.weight(1f))
                TextButton(stringResource(if (editing) Res.string.action_save_changes else Res.string.action_add), {
                    val s = LocalTime(startHour, startMinute); val e = LocalTime(endHour, endMinute)
                    if (e <= s) { ToastManager.show(invalidOrderMessage); return@TextButton }
                    val conflict = existing.any { slot -> if (editing && slot.number == initialNumber) false else maxOf(s, parseLocalTimeSafely(slot.startTime)) < minOf(e, parseLocalTimeSafely(slot.endTime)) }
                    if (conflict) { ToastManager.show(conflictMessage); return@TextButton }
                    onConfirm(initialNumber, formatTime(s), formatTime(e), alias.ifBlank { null })
                }, Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable private fun TimePickerGroup(title: String, hour: Int, minute: Int, onHour: (Int) -> Unit, onMinute: (Int) -> Unit, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MiuixTheme.textStyles.footnote1)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(value = hour, onValueChange = onHour, modifier = Modifier.weight(1f).height(132.dp), range = 0..23)
            Text(":", modifier = Modifier.padding(horizontal = 2.dp))
            NumberPicker(value = minute, onValueChange = onMinute, modifier = Modifier.weight(1f).height(132.dp), range = 0..59)
        }
    }
}

private fun parseTime(value: String): Pair<Int, Int> = runCatching { LocalTime.parse(value).let { it.hour to it.minute } }.getOrDefault(0 to 0)
private fun two(value: Int) = value.toString().padStart(2, '0')

@Composable
internal fun MiuixComboScheduleEditScreen(
    comboId: String?, copyFromId: String?, onBack: () -> Unit,
    viewModel: ComboScheduleEditViewModel = koinViewModel { parametersOf(comboId ?: "", copyFromId ?: "") },
) {
    val state by viewModel.uiState.collectAsState()
    if (!state.isDataLoaded) return LoadingMiuixScreen()
    val title = stringResource(if (comboId == null) Res.string.title_add_combo_schedule else Res.string.title_edit_combo_schedule)
    val exclusiveScheduleText = stringResource(Res.string.option_exclusive_schedule)
    val unnamedScheduleText = stringResource(Res.string.option_unnamed_schedule)
    val noPublicTablesText = stringResource(Res.string.msg_please_create_public_schedule_first)
    val saveA11y = stringResource(Res.string.a11y_save)
    val background = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val scrollBehavior = rememberSharedScrollBehavior(); val direction = LocalLayoutDirection.current; val hostPadding = LocalNavigationHostPadding.current
    val overlap = remember(state.rules) { state.rules.indices.any { index -> checkRuleOverlap(index, state.rules[index], state.rules) } }
    val validName = state.name.isNotBlank()
    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }
    LaunchedEffect(state.errorMsg) { state.errorMsg?.takeIf { it.isNotBlank() }?.let { ToastManager.show(it); viewModel.clearError() } }
    var dateRuleIndex by remember { mutableStateOf<Int?>(null) }; var pendingStart by remember { mutableStateOf<LocalDate?>(null) }
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = background, topBar = {
        HyperGlassTopBar(title = title, backdrop = backdrop, scrollBehavior = scrollBehavior,
            startAction = { a, s -> HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
            endAction = { a, s -> HyperLiquidTopBarButton({ if (validName && !overlap) viewModel.save() }, backdrop, vectorResource(Res.drawable.check_24px), saveA11y, backdropAlpha = a, shadowAlpha = s) })
    }) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyColumn(Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection), contentPadding = PaddingValues(start = padding.calculateLeftPadding(direction) + 20.dp, top = padding.calculateTopPadding() + 12.dp, end = padding.calculateRightPadding(direction) + 20.dp, bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextField(state.name, viewModel::onNameChange, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_schedule_scheme_name), singleLine = true)
                            if (!validName) Text(stringResource(Res.string.error_name_cannot_be_empty), color = MiuixTheme.colorScheme.error)
                            WindowDropdownPreference(items = listOf(exclusiveScheduleText) + state.availablePublicTables.map { it.name ?: unnamedScheduleText }, selectedIndex = state.baseTimeTableId?.let { id -> state.availablePublicTables.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.plus(1) } ?: 0, title = stringResource(Res.string.label_base_schedule), summary = state.baseTimeTableId?.let { id -> state.availablePublicTables.find { it.id == id }?.name ?: unnamedScheduleText } ?: exclusiveScheduleText, onSelectedIndexChange = { index -> viewModel.onBaseTableChange(if (index == 0) null else state.availablePublicTables.getOrNull(index - 1)?.id) })
                        }
                    }
                }
                item { Text(stringResource(Res.string.title_rule_list_by_date), style = MiuixTheme.textStyles.title3) }
                if (state.availablePublicTables.isEmpty()) item { Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.errorContainer)) { Text(noPublicTablesText, color = MiuixTheme.colorScheme.error) } }
                itemsIndexed(state.rules, key = { _, rule -> rule.id }) { index, rule ->
                    MiuixRuleCard(rule, state.availablePublicTables, checkRuleOverlap(index, rule, state.rules), { updated -> viewModel.updateRule(index, updated) }, { viewModel.removeRule(index) }, { dateRuleIndex = index })
                }
                item { Button(onClick = { if (state.availablePublicTables.isNotEmpty()) viewModel.addRule() else ToastManager.show(noPublicTablesText) }, modifier = Modifier.fillMaxWidth(), enabled = state.availablePublicTables.isNotEmpty()) { Text(stringResource(Res.string.action_add_time_range_rule)) } }
            }
            dateRuleIndex?.let { index ->
                val rule = state.rules.getOrNull(index)
                if (rule != null) {
                    if (pendingStart == null) MiuixDatePickerDialog(stringResource(Res.string.dialog_title_select_effective_date_range), { millis -> pendingStart = millis?.let(::millisToDate) }, { if (pendingStart == null) dateRuleIndex = null })
                    else MiuixDatePickerDialog(stringResource(Res.string.dialog_title_select_effective_date_range), { millis -> millis?.let(::millisToDate)?.let { end -> val start = pendingStart!!; viewModel.updateRule(index, rule.copy(startDate = minOf(start, end).toString(), endDate = maxOf(start, end).toString())) }; pendingStart = null; dateRuleIndex = null }, { pendingStart = null; dateRuleIndex = null })
                }
            }
        }
    }
}

@Composable private fun MiuixRuleCard(rule: TimeTableComboRule, tables: List<TimeTable>, overlap: Boolean, onUpdate: (TimeTableComboRule) -> Unit, onDelete: () -> Unit, onDateClick: () -> Unit) {
    val selected = tables.indexOfFirst { it.id == rule.targetTimeTableId }.coerceAtLeast(0)
    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 10.dp), colors = CardDefaults.defaultColors(color = if (overlap) MiuixTheme.colorScheme.errorContainer else MiuixTheme.colorScheme.surfaceContainer)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WindowDropdownPreference(items = tables.map { it.name ?: "" }, selectedIndex = selected, title = stringResource(Res.string.label_applied_schedule), summary = tables.getOrNull(selected)?.name ?: stringResource(Res.string.option_select_schedule_table), onSelectedIndexChange = { tables.getOrNull(it)?.let { table -> onUpdate(rule.copy(targetTimeTableId = table.id)) } })
            Card(Modifier.fillMaxWidth().clickable(onClick = onDateClick), insideMargin = PaddingValues(horizontal = 14.dp, vertical = 12.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainerHigh)) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(stringResource(Res.string.label_effective_date_range), style = MiuixTheme.textStyles.footnote1); Text("${rule.startDate} - ${rule.endDate}", color = if (overlap) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurface) }
            }
            if (overlap) Text(stringResource(Res.string.error_rule_date_overlap), color = MiuixTheme.colorScheme.error)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { IconButton(onClick = onDelete) { Icon(vectorResource(Res.drawable.delete_24px), stringResource(Res.string.a11y_delete_rule), tint = MiuixTheme.colorScheme.error) } }
        }
    }
}

private fun checkRuleOverlap(index: Int, rule: TimeTableComboRule, rules: List<TimeTableComboRule>): Boolean = rules.indices.any { other -> if (other == index) false else rule.startDate <= rules[other].endDate && rule.endDate >= rules[other].startDate }
private fun millisToDate(millis: Long): LocalDate = kotlinx.datetime.Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
