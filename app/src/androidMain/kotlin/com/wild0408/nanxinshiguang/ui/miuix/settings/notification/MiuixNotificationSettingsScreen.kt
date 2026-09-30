package com.wild0408.nanxinshiguang.ui.miuix.settings.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wild0408.nanxinshiguang.data.model.AutoControlMode
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.notification.NotificationDialogType
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.notification.NotificationSettingsUiState
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.notification.NotificationSettingsViewModel
import com.wild0408.nanxinshiguang.ui.material.settings.notification.hasDndPermission
import com.wild0408.nanxinshiguang.ui.material.settings.notification.hasExactAlarmPermission
import com.wild0408.nanxinshiguang.ui.material.settings.notification.hasNotificationPermission
import com.wild0408.nanxinshiguang.ui.material.settings.notification.openAppSettings
import com.wild0408.nanxinshiguang.ui.material.settings.notification.openDndSettings
import com.wild0408.nanxinshiguang.ui.material.settings.notification.openExactAlarmSettings
import com.wild0408.nanxinshiguang.ui.material.settings.notification.openIgnoreBatteryOptimizationSettings
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_close
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.action_go_to_settings
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.auto_mode_dnd
import nanxinshiguang.generated.resources.auto_mode_dnd_permission_warning
import nanxinshiguang.generated.resources.auto_mode_off
import nanxinshiguang.generated.resources.auto_mode_silent
import nanxinshiguang.generated.resources.desc_compat_wearable_sync
import nanxinshiguang.generated.resources.dialog_text_clear_confirmation
import nanxinshiguang.generated.resources.dialog_text_dnd_permission
import nanxinshiguang.generated.resources.dialog_text_exact_alarm_permission
import nanxinshiguang.generated.resources.dialog_title_auto_mode_selection
import nanxinshiguang.generated.resources.dialog_title_clear_confirmation
import nanxinshiguang.generated.resources.dialog_title_dnd_permission
import nanxinshiguang.generated.resources.dialog_title_exact_alarm_permission
import nanxinshiguang.generated.resources.dialog_title_set_remind_time
import nanxinshiguang.generated.resources.dialog_title_view_skipped_dates
import nanxinshiguang.generated.resources.error_input_empty
import nanxinshiguang.generated.resources.error_input_range
import nanxinshiguang.generated.resources.item_auto_mode
import nanxinshiguang.generated.resources.item_background_and_autostart
import nanxinshiguang.generated.resources.item_clear_skipped_dates
import nanxinshiguang.generated.resources.item_compat_wearable_sync
import nanxinshiguang.generated.resources.item_course_reminder
import nanxinshiguang.generated.resources.item_dnd_permission
import nanxinshiguang.generated.resources.item_exact_alarm_permission
import nanxinshiguang.generated.resources.item_ignore_battery_optimization
import nanxinshiguang.generated.resources.item_remind_time_before
import nanxinshiguang.generated.resources.item_update_holiday_info
import nanxinshiguang.generated.resources.item_view_skipped_dates
import nanxinshiguang.generated.resources.label_minutes_input
import nanxinshiguang.generated.resources.remind_time_minutes_format
import nanxinshiguang.generated.resources.section_title_advanced
import nanxinshiguang.generated.resources.section_title_general
import nanxinshiguang.generated.resources.section_title_skip_dates
import nanxinshiguang.generated.resources.skipped_dates_count_format
import nanxinshiguang.generated.resources.skipped_dates_none
import nanxinshiguang.generated.resources.status_authorized
import nanxinshiguang.generated.resources.status_disabled
import nanxinshiguang.generated.resources.status_enabled
import nanxinshiguang.generated.resources.status_unauthorized
import nanxinshiguang.generated.resources.text_auto_mode_dependency
import nanxinshiguang.generated.resources.text_permission_importance_detail
import nanxinshiguang.generated.resources.text_permission_importance_title
import nanxinshiguang.generated.resources.text_skip_dates_experimental
import nanxinshiguang.generated.resources.title_course_notification_settings
import nanxinshiguang.generated.resources.toast_clear_failed
import nanxinshiguang.generated.resources.toast_clear_success
import nanxinshiguang.generated.resources.toast_enable_reminder_first
import nanxinshiguang.generated.resources.toast_holidays_update_success
import nanxinshiguang.generated.resources.toast_notification_permission_denied
import nanxinshiguang.generated.resources.toast_update_failed
import nanxinshiguang.generated.resources.update_holiday_info_hint
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun MiuixNotificationSettingsScreen(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var showExactAlarmGuide by remember { mutableStateOf(false) }
    var showDndGuide by remember { mutableStateOf(false) }

    val notificationPermissionDenied = stringResource(Res.string.toast_notification_permission_denied)
    val enableReminderFirst = stringResource(Res.string.toast_enable_reminder_first)
    val holidayUpdateSuccess = stringResource(Res.string.toast_holidays_update_success)
    val clearSuccess = stringResource(Res.string.toast_clear_success)
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) ToastManager.show(notificationPermissionDenied)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.updateExactAlarmStatus(hasExactAlarmPermission(context))
                viewModel.updateDndPermissionStatus(hasDndPermission(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.updateExactAlarmStatus(hasExactAlarmPermission(context))
        viewModel.updateDndPermissionStatus(hasDndPermission(context))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission(context)
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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
                title = stringResource(Res.string.title_course_notification_settings),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                NotificationGeneralSection(
                    uiState = uiState,
                    onReminderToggle = { enabled ->
                        if (!enabled || hasExactAlarmPermission(context)) {
                            viewModel.updateReminderEnabled(enabled)
                        } else {
                            showExactAlarmGuide = true
                        }
                    },
                    onCompatWearableToggle = viewModel::updateCompatWearableSync,
                    onAutoModeClick = {
                        if (uiState.reminderEnabled) {
                            viewModel.showDialog(NotificationDialogType.AutoModeSelection)
                        } else {
                            ToastManager.show(enableReminderFirst)
                        }
                    },
                    onRemindTimeClick = {
                        viewModel.showDialog(NotificationDialogType.EditRemindMinutes)
                    },
                    onExactAlarmClick = { openExactAlarmSettings(context) },
                    onDndClick = { openDndSettings(context) },
                    onAppSettingsClick = { openAppSettings(context) },
                    onBatteryOptimizationClick = {
                        openIgnoreBatteryOptimizationSettings(context)
                    },
                )
            }
            item {
                NotificationAdvancedSection(
                    uiState = uiState,
                    onUpdateHolidays = {
                        if (!uiState.isLoading) {
                            viewModel.updateHolidays { result ->
                                result.fold(
                                    onSuccess = { ToastManager.show(holidayUpdateSuccess) },
                                    onFailure = { error ->
                                        scope.launch {
                                            ToastManager.show(
                                                getString(
                                                    Res.string.toast_update_failed,
                                                    error.message.orEmpty(),
                                                ),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    },
                    onClearSkippedDates = {
                        viewModel.showDialog(NotificationDialogType.ClearConfirmation)
                    },
                    onViewSkippedDates = {
                        viewModel.showDialog(NotificationDialogType.ViewSkippedDates)
                    },
                )
            }
        }
        }
        NotificationDialogs(
            uiState = uiState,
            viewModel = viewModel,
            onDndGuideRequested = { showDndGuide = true },
            clearSuccessMessage = clearSuccess,
        )
        if (showExactAlarmGuide) {
            PermissionGuideDialog(
                title = stringResource(Res.string.dialog_title_exact_alarm_permission),
                message = stringResource(Res.string.dialog_text_exact_alarm_permission),
                onDismiss = { showExactAlarmGuide = false },
                onOpenSettings = { openExactAlarmSettings(context) },
            )
        }
        if (showDndGuide) {
            PermissionGuideDialog(
                title = stringResource(Res.string.dialog_title_dnd_permission),
                message = stringResource(Res.string.dialog_text_dnd_permission),
                onDismiss = { showDndGuide = false },
                onOpenSettings = { openDndSettings(context) },
            )
        }
    }

}

@Composable
private fun NotificationGeneralSection(
    uiState: NotificationSettingsUiState,
    onReminderToggle: (Boolean) -> Unit,
    onCompatWearableToggle: (Boolean) -> Unit,
    onAutoModeClick: () -> Unit,
    onRemindTimeClick: () -> Unit,
    onExactAlarmClick: () -> Unit,
    onDndClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
    onBatteryOptimizationClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallTitle(
            text = stringResource(Res.string.section_title_general),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = stringResource(Res.string.text_permission_importance_title),
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(Res.string.text_permission_importance_detail),
                    modifier = Modifier.padding(top = 6.dp),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            Column {
                BasicComponent(
                    title = stringResource(Res.string.item_course_reminder),
                    endActions = {
                        Switch(
                            checked = uiState.reminderEnabled,
                            onCheckedChange = onReminderToggle,
                        )
                    },
                )
                BasicComponent(
                    title = stringResource(Res.string.item_compat_wearable_sync),
                    summary = stringResource(Res.string.desc_compat_wearable_sync),
                    endActions = {
                        Switch(
                            checked = uiState.compatWearableSync,
                            onCheckedChange = onCompatWearableToggle,
                        )
                    },
                )
                BasicComponent(
                    title = stringResource(Res.string.item_auto_mode),
                    summary = if (uiState.reminderEnabled) {
                        autoModeLabel(uiState)
                    } else {
                        stringResource(Res.string.text_auto_mode_dependency)
                    },
                    onClick = onAutoModeClick,
                )
                BasicComponent(
                    title = stringResource(Res.string.item_remind_time_before),
                    summary = stringResource(
                        Res.string.remind_time_minutes_format,
                        uiState.remindBeforeMinutes,
                    ),
                    onClick = onRemindTimeClick,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BasicComponent(
                        title = stringResource(Res.string.item_exact_alarm_permission),
                        summary = stringResource(
                            if (uiState.exactAlarmStatus) {
                                Res.string.status_enabled
                            } else {
                                Res.string.status_disabled
                            },
                        ),
                        onClick = onExactAlarmClick,
                    )
                }
                BasicComponent(
                    title = stringResource(Res.string.item_dnd_permission),
                    summary = stringResource(
                        if (uiState.dndPermissionStatus) {
                            Res.string.status_authorized
                        } else {
                            Res.string.status_unauthorized
                        },
                    ),
                    onClick = onDndClick,
                )
                BasicComponent(
                    title = stringResource(Res.string.item_background_and_autostart),
                    onClick = onAppSettingsClick,
                )
                BasicComponent(
                    title = stringResource(Res.string.item_ignore_battery_optimization),
                    onClick = onBatteryOptimizationClick,
                )
            }
        }
    }
}

@Composable
private fun NotificationAdvancedSection(
    uiState: NotificationSettingsUiState,
    onUpdateHolidays: () -> Unit,
    onClearSkippedDates: () -> Unit,
    onViewSkippedDates: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallTitle(
            text = stringResource(Res.string.section_title_advanced),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            Column {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = stringResource(Res.string.section_title_skip_dates),
                        style = MiuixTheme.textStyles.title3,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.text_skip_dates_experimental),
                        modifier = Modifier.padding(top = 6.dp),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                BasicComponent(
                    title = stringResource(Res.string.item_update_holiday_info),
                    summary = stringResource(Res.string.update_holiday_info_hint),
                    onClick = onUpdateHolidays,
                    endActions = {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        }
                    },
                )
                BasicComponent(
                    title = stringResource(Res.string.item_clear_skipped_dates),
                    onClick = onClearSkippedDates,
                )
                BasicComponent(
                    title = stringResource(Res.string.item_view_skipped_dates),
                    summary = if (uiState.skippedDates.isEmpty()) {
                        stringResource(Res.string.skipped_dates_none)
                    } else {
                        stringResource(
                            Res.string.skipped_dates_count_format,
                            uiState.skippedDates.size,
                        )
                    },
                    onClick = onViewSkippedDates,
                )
            }
        }
    }
}

@Composable
private fun autoModeLabel(uiState: NotificationSettingsUiState): String {
    if (!uiState.autoModeEnabled) return stringResource(Res.string.auto_mode_off)
    return when (uiState.autoControlMode) {
        AutoControlMode.DND -> stringResource(Res.string.auto_mode_dnd)
        AutoControlMode.SILENT -> stringResource(Res.string.auto_mode_silent)
    }
}

@Composable
private fun NotificationDialogs(
    uiState: NotificationSettingsUiState,
    viewModel: NotificationSettingsViewModel,
    onDndGuideRequested: () -> Unit,
    clearSuccessMessage: String,
) {
    val scope = rememberCoroutineScope()
    when (uiState.activeDialog) {
        NotificationDialogType.EditRemindMinutes -> {
            var input by remember(uiState.remindBeforeMinutes) {
                mutableStateOf(uiState.remindBeforeMinutes.toString())
            }
            val parsed = input.toIntOrNull()
            val error = when {
                input.isEmpty() -> stringResource(Res.string.error_input_empty)
                parsed == null || parsed !in 0..60 -> stringResource(Res.string.error_input_range)
                else -> null
            }
            WindowDialog(
                show = true,
                title = stringResource(Res.string.dialog_title_set_remind_time),
                onDismissRequest = viewModel::dismissDialog,
                insideMargin = DpSize(16.dp, 16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextField(
                        value = input,
                        onValueChange = { value -> input = value.filter(Char::isDigit).take(2) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.label_minutes_input),
                        singleLine = true,
                    )
                    if (error != null) {
                        Text(
                            text = error,
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.error,
                        )
                    }
                    DialogActions(
                        onDismiss = viewModel::dismissDialog,
                        onConfirm = { parsed?.let(viewModel::updateRemindBeforeMinutes) },
                        confirmEnabled = error == null,
                    )
                }
            }
        }

        NotificationDialogType.AutoModeSelection -> {
            var selection by remember(uiState.autoModeEnabled, uiState.autoControlMode) {
                mutableStateOf(
                    if (!uiState.autoModeEnabled) AutoModeChoice.OFF else when (uiState.autoControlMode) {
                        AutoControlMode.DND -> AutoModeChoice.DND
                        AutoControlMode.SILENT -> AutoModeChoice.SILENT
                    },
                )
            }
            WindowDialog(
                show = true,
                title = stringResource(Res.string.dialog_title_auto_mode_selection),
                onDismissRequest = viewModel::dismissDialog,
                insideMargin = DpSize(16.dp, 16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!uiState.dndPermissionStatus) {
                        Text(
                            text = stringResource(Res.string.auto_mode_dnd_permission_warning),
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.error,
                        )
                    }
                    AutoModeChoice.entries.forEach { choice ->
                        BasicComponent(
                            title = when (choice) {
                                AutoModeChoice.OFF -> stringResource(Res.string.auto_mode_off)
                                AutoModeChoice.DND -> stringResource(Res.string.auto_mode_dnd)
                                AutoModeChoice.SILENT -> stringResource(Res.string.auto_mode_silent)
                            },
                            onClick = { selection = choice },
                            endActions = {
                                RadioButton(
                                    selected = selection == choice,
                                    onClick = { selection = choice },
                                )
                            },
                        )
                    }
                    DialogActions(
                        onDismiss = viewModel::dismissDialog,
                        onConfirm = {
                            if (selection != AutoModeChoice.OFF && !uiState.dndPermissionStatus) {
                                viewModel.dismissDialog()
                                onDndGuideRequested()
                            } else {
                                when (selection) {
                                    AutoModeChoice.OFF -> viewModel.updateAutoMode(
                                        false,
                                        uiState.autoControlMode,
                                    )
                                    AutoModeChoice.DND -> viewModel.updateAutoMode(
                                        true,
                                        AutoControlMode.DND,
                                    )
                                    AutoModeChoice.SILENT -> viewModel.updateAutoMode(
                                        true,
                                        AutoControlMode.SILENT,
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }

        NotificationDialogType.ClearConfirmation -> {
            WindowDialog(
                show = true,
                title = stringResource(Res.string.dialog_title_clear_confirmation),
                onDismissRequest = viewModel::dismissDialog,
                insideMargin = DpSize(16.dp, 16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.dialog_text_clear_confirmation),
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    DialogActions(
                        onDismiss = viewModel::dismissDialog,
                        onConfirm = {
                            viewModel.clearSkippedDates { result ->
                                result.fold(
                                    onSuccess = { ToastManager.show(clearSuccessMessage) },
                                    onFailure = { error ->
                                        scope.launch {
                                            ToastManager.show(
                                                getString(
                                                    Res.string.toast_clear_failed,
                                                    error.message.orEmpty(),
                                                ),
                                            )
                                        }
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }

        NotificationDialogType.ViewSkippedDates -> {
            WindowDialog(
                show = true,
                title = stringResource(Res.string.dialog_title_view_skipped_dates),
                onDismissRequest = viewModel::dismissDialog,
                insideMargin = DpSize(16.dp, 16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.skippedDates.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.skipped_dates_none),
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(108.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                        ) {
                            items(uiState.skippedDates.sorted()) { date ->
                                Card(
                                    modifier = Modifier.padding(4.dp),
                                    colors = CardDefaults.defaultColors(
                                        color = MiuixTheme.colorScheme.surfaceContainer,
                                    ),
                                ) {
                                    Text(
                                        text = date,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        text = stringResource(Res.string.action_close),
                        onClick = viewModel::dismissDialog,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }

        NotificationDialogType.None -> Unit
    }
}

@Composable
private fun PermissionGuideDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    WindowDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss,
        insideMargin = DpSize(16.dp, 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(Res.string.action_go_to_settings),
                    onClick = {
                        onOpenSettings()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun DialogActions(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(
            text = stringResource(Res.string.action_cancel),
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            text = stringResource(Res.string.action_confirm),
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = confirmEnabled,
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
    }
}

private enum class AutoModeChoice {
    OFF,
    DND,
    SILENT,
}
