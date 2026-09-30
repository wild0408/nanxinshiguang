package com.wild0408.nanxinshiguang.ui.miuix.settings.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.db.main.CourseTable
import com.wild0408.nanxinshiguang.data.di.AppStorage
import com.wild0408.nanxinshiguang.data.repository.AppSettingsRepository
import com.wild0408.nanxinshiguang.data.repository.CourseTableRepository
import com.wild0408.nanxinshiguang.tool.FileManagerCallbacks
import com.wild0408.nanxinshiguang.tool.rememberFileManager
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.components.platformShareFile
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.course.ConversionEvent
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.course.ConversionUiState
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.course.CourseTableConversionViewModel
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.course.ExportType
import kotlinx.coroutines.launch
import okio.Buffer
import okio.FileSystem
import okio.SYSTEM
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_add_new_table
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.a11y_details
import nanxinshiguang.generated.resources.action_add
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.action_next_step
import nanxinshiguang.generated.resources.action_share
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.alarm_option_none
import nanxinshiguang.generated.resources.alarm_option_on_time
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.desc_backup_restore
import nanxinshiguang.generated.resources.desc_export_ics_with_alarm
import nanxinshiguang.generated.resources.desc_export_json_with_config
import nanxinshiguang.generated.resources.desc_import_json
import nanxinshiguang.generated.resources.desc_nuist_one_click_import
import nanxinshiguang.generated.resources.desc_sync_to_system_calendar
import nanxinshiguang.generated.resources.dialog_text_file_saved_share_prompt
import nanxinshiguang.generated.resources.dialog_title_add_table
import nanxinshiguang.generated.resources.dialog_title_file_saved
import nanxinshiguang.generated.resources.dialog_title_ics_export_settings
import nanxinshiguang.generated.resources.dialog_title_select_export_table
import nanxinshiguang.generated.resources.dialog_title_select_import_table
import nanxinshiguang.generated.resources.error_input_empty
import nanxinshiguang.generated.resources.error_input_range
import nanxinshiguang.generated.resources.item_backup_restore
import nanxinshiguang.generated.resources.item_export_course_file
import nanxinshiguang.generated.resources.item_export_ics_file
import nanxinshiguang.generated.resources.item_import_course_file
import nanxinshiguang.generated.resources.item_nuist_one_click_import
import nanxinshiguang.generated.resources.item_sync_to_system_calendar
import nanxinshiguang.generated.resources.label_current
import nanxinshiguang.generated.resources.label_minutes_input
import nanxinshiguang.generated.resources.label_select_alarm_time
import nanxinshiguang.generated.resources.label_table_name
import nanxinshiguang.generated.resources.section_file_conversion
import nanxinshiguang.generated.resources.section_school_import
import nanxinshiguang.generated.resources.section_sync
import nanxinshiguang.generated.resources.snackbar_file_save_canceled
import nanxinshiguang.generated.resources.snackbar_file_selection_canceled
import nanxinshiguang.generated.resources.text_no_course_tables
import nanxinshiguang.generated.resources.title_conversion
import nanxinshiguang.generated.resources.toast_add_table_success
import nanxinshiguang.generated.resources.toast_name_empty
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.time.Clock

@Composable
internal fun MiuixCourseTableConversionScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: CourseTableConversionViewModel = koinViewModel(),
    appStorage: AppStorage = koinInject(),
    courseTableRepository: CourseTableRepository = koinInject(),
    appSettingsRepository: AppSettingsRepository = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val fileSelectionCanceled = stringResource(Res.string.snackbar_file_selection_canceled)
    val fileSaveCanceled = stringResource(Res.string.snackbar_file_save_canceled)
    var pendingImportTableId by remember { mutableStateOf<String?>(null) }
    var pendingShareFilePath by remember { mutableStateOf<String?>(null) }
    var shareFilePath by remember { mutableStateOf<String?>(null) }
    var shareFileMimeType by remember { mutableStateOf("application/json") }

    val fileManager = rememberFileManager(
        callbacks = FileManagerCallbacks(
            onFileImported = { bytes, _ ->
                val tableId = pendingImportTableId
                if (bytes != null && tableId != null) {
                    viewModel.handleFileImport(tableId, Buffer().write(bytes))
                } else if (bytes == null) {
                    ToastManager.show(fileSelectionCanceled)
                }
                pendingImportTableId = null
            },
            onFileExported = { success ->
                if (success) {
                    shareFilePath = pendingShareFilePath
                } else {
                    pendingShareFilePath = null
                    ToastManager.show(fileSaveCanceled)
                }
            },
        ),
    )

    LaunchedEffect(viewModel, fileManager) {
        viewModel.events.collect { event ->
            when (event) {
                is ConversionEvent.LaunchImportFilePicker -> {
                    pendingImportTableId = event.tableId
                    fileManager.importFile(listOf("json"))
                }

                is ConversionEvent.LaunchExportFileCreator -> {
                    val fileName = "nanxinshiguang_${Clock.System.now().toEpochMilliseconds()}.json"
                    val bytes = event.jsonContent.encodeToByteArray()
                    pendingShareFilePath = prepareShareFile(appStorage, fileName, bytes)
                    shareFileMimeType = "application/json"
                    fileManager.exportFile(fileName, bytes)
                }

                is ConversionEvent.LaunchExportIcsFileCreator -> {
                    val fileName = "nanxinshiguang_${Clock.System.now().toEpochMilliseconds()}.ics"
                    val bytes = event.icsContent.encodeToByteArray()
                    pendingShareFilePath = prepareShareFile(appStorage, fileName, bytes)
                    shareFileMimeType = "text/calendar"
                    fileManager.exportFile(fileName, bytes)
                }

                is ConversionEvent.ShowMessage -> ToastManager.show(event.message)
            }
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
                title = stringResource(Res.string.title_conversion),
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
            if (uiState.isLoading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
            item {
                ConversionSection(title = stringResource(Res.string.section_file_conversion)) {
                    ConversionItem(
                        title = stringResource(Res.string.item_import_course_file),
                        summary = stringResource(Res.string.desc_import_json),
                        onClick = viewModel::onImportClick,
                    )
                    ConversionItem(
                        title = stringResource(Res.string.item_export_course_file),
                        summary = stringResource(Res.string.desc_export_json_with_config),
                        onClick = viewModel::onExportClick,
                    )
                    ConversionItem(
                        title = stringResource(Res.string.item_export_ics_file),
                        summary = stringResource(Res.string.desc_export_ics_with_alarm),
                        onClick = viewModel::onExportIcsClick,
                    )
                }
            }
            item {
                ConversionSection(title = stringResource(Res.string.section_school_import)) {
                    ConversionItem(
                        title = stringResource(Res.string.item_nuist_one_click_import),
                        summary = stringResource(Res.string.desc_nuist_one_click_import),
                        onClick = viewModel::onNuistImportClick,
                    )
                }
            }
            item {
                ConversionSection(title = stringResource(Res.string.section_sync)) {
                    ConversionItem(
                        title = stringResource(Res.string.item_sync_to_system_calendar),
                        summary = stringResource(Res.string.desc_sync_to_system_calendar),
                        onClick = viewModel::onSyncToCalendarClick,
                    )
                    ConversionItem(
                        title = stringResource(Res.string.item_backup_restore),
                        summary = stringResource(Res.string.desc_backup_restore),
                        onClick = { onNavigate(Destination.BackupAndRestore) },
                    )
                }
            }
        }
        }

        MiuixConversionDialogs(
            uiState = uiState,
            courseTableRepository = courseTableRepository,
            appSettingsRepository = appSettingsRepository,
            onDismiss = viewModel::dismissDialog,
            onImportSelected = viewModel::onImportTableSelected,
            onExportSelected = viewModel::onExportTableSelected,
        )

        shareFilePath?.let { path ->
        WindowDialog(
            show = true,
            title = stringResource(Res.string.dialog_title_file_saved),
            onDismissRequest = {
                shareFilePath = null
                pendingShareFilePath = null
            },
            insideMargin = DpSize(16.dp, 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(Res.string.dialog_text_file_saved_share_prompt),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        text = stringResource(Res.string.action_cancel),
                        onClick = {
                            shareFilePath = null
                            pendingShareFilePath = null
                        },
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        text = stringResource(Res.string.action_share),
                        onClick = {
                            platformShareFile(path, shareFileMimeType)
                            shareFilePath = null
                            pendingShareFilePath = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
        }
    }
}

private fun prepareShareFile(appStorage: AppStorage, fileName: String, bytes: ByteArray): String {
    val shareTempDir = appStorage.cacheDir / "share_temp"
    val tempFilePath = shareTempDir / fileName
    FileSystem.SYSTEM.createDirectories(shareTempDir)
    FileSystem.SYSTEM.write(tempFilePath) { write(bytes) }
    return tempFilePath.toString()
}

@Composable
private fun ConversionSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallTitle(text = title, modifier = Modifier.padding(horizontal = 12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun ConversionItem(title: String, summary: String, onClick: () -> Unit) {
    BasicComponent(
        modifier = Modifier.fillMaxWidth(),
        title = title,
        summary = summary,
        onClick = onClick,
        endActions = {
            Icon(
                imageVector = vectorResource(Res.drawable.chevron_right_24px),
                contentDescription = stringResource(Res.string.a11y_details),
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
    )
}

@Composable
private fun MiuixConversionDialogs(
    uiState: ConversionUiState,
    courseTableRepository: CourseTableRepository,
    appSettingsRepository: AppSettingsRepository,
    onDismiss: () -> Unit,
    onImportSelected: (String) -> Unit,
    onExportSelected: (String, Int?) -> Unit,
) {
    when {
        uiState.showImportTableDialog -> MiuixCourseTablePickerDialog(
            title = stringResource(Res.string.dialog_title_select_import_table),
            courseTableRepository = courseTableRepository,
            appSettingsRepository = appSettingsRepository,
            onDismiss = onDismiss,
            onSelected = onImportSelected,
        )

        uiState.showExportTableDialog && uiState.exportType == ExportType.JSON -> {
            MiuixCourseTablePickerDialog(
                title = stringResource(Res.string.dialog_title_select_export_table),
                courseTableRepository = courseTableRepository,
                appSettingsRepository = appSettingsRepository,
                onDismiss = onDismiss,
                onSelected = { onExportSelected(it, null) },
            )
        }

        uiState.showExportTableDialog && uiState.exportType == ExportType.ICS -> {
            MiuixIcsExportDialog(
                courseTableRepository = courseTableRepository,
                appSettingsRepository = appSettingsRepository,
                onDismiss = onDismiss,
                onConfirm = onExportSelected,
            )
        }
    }
}

@Composable
private fun MiuixIcsExportDialog(
    courseTableRepository: CourseTableRepository,
    appSettingsRepository: AppSettingsRepository,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?) -> Unit,
) {
    var choice by remember { mutableStateOf(AlarmChoice.BEFORE) }
    var minutesInput by remember { mutableStateOf("15") }
    var showTablePicker by remember { mutableStateOf(false) }
    val minutes = minutesInput.toIntOrNull()
    val inputError = choice == AlarmChoice.BEFORE && (minutes == null || minutes !in 1..60)

    if (showTablePicker) {
        MiuixCourseTablePickerDialog(
            title = stringResource(Res.string.dialog_title_select_export_table),
            courseTableRepository = courseTableRepository,
            appSettingsRepository = appSettingsRepository,
            onDismiss = onDismiss,
            onSelected = { tableId ->
                onConfirm(
                    tableId,
                    when (choice) {
                        AlarmChoice.NONE -> null
                        AlarmChoice.ON_TIME -> 0
                        AlarmChoice.BEFORE -> minutes
                    },
                )
            },
        )
        return
    }

    WindowDialog(
        show = true,
        title = stringResource(Res.string.dialog_title_ics_export_settings),
        onDismissRequest = onDismiss,
        insideMargin = DpSize(16.dp, 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.label_select_alarm_time),
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
            )
            AlarmChoice.entries.forEach { item ->
                BasicComponent(
                    title = when (item) {
                        AlarmChoice.NONE -> stringResource(Res.string.alarm_option_none)
                        AlarmChoice.ON_TIME -> stringResource(Res.string.alarm_option_on_time)
                        AlarmChoice.BEFORE -> stringResource(Res.string.label_minutes_input)
                    },
                    onClick = { choice = item },
                    endActions = {
                        RadioButton(
                            selected = choice == item,
                            onClick = { choice = item },
                        )
                    },
                )
            }
            if (choice == AlarmChoice.BEFORE) {
                TextField(
                    value = minutesInput,
                    onValueChange = { minutesInput = it.filter(Char::isDigit).take(2) },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.label_minutes_input),
                    singleLine = true,
                )
                if (inputError) {
                    Text(
                        text = if (minutesInput.isEmpty()) {
                            stringResource(Res.string.error_input_empty)
                        } else {
                            stringResource(Res.string.error_input_range)
                        },
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error,
                    )
                }
            }
            DialogActions(
                confirmText = stringResource(Res.string.action_next_step),
                confirmEnabled = !inputError,
                onDismiss = onDismiss,
                onConfirm = { showTablePicker = true },
            )
        }
    }
}

@Composable
private fun MiuixCourseTablePickerDialog(
    title: String,
    courseTableRepository: CourseTableRepository,
    appSettingsRepository: AppSettingsRepository,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    val courseTables by courseTableRepository.getAllCourseTables().collectAsState(emptyList())
    val appSettings by appSettingsRepository.getAppSettings().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var selectedTableId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseTables, appSettings?.currentCourseTableId) {
        if (selectedTableId == null) {
            selectedTableId = courseTables.firstOrNull {
                it.id == appSettings?.currentCourseTableId
            }?.id ?: courseTables.firstOrNull()?.id
        }
    }

    WindowDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss,
        insideMargin = DpSize(16.dp, 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (courseTables.isEmpty()) {
                Text(
                    text = stringResource(Res.string.text_no_course_tables),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(courseTables, key = CourseTable::id) { table ->
                        val selected = table.id == selectedTableId
                        val current = table.id == appSettings?.currentCourseTableId
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedTableId = table.id },
                            colors = CardDefaults.defaultColors(
                                color = if (selected) {
                                    MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                                } else {
                                    MiuixTheme.colorScheme.surfaceContainer
                                },
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = table.name,
                                        style = MiuixTheme.textStyles.body1,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    if (current) {
                                        Text(
                                            text = stringResource(Res.string.label_current),
                                            style = MiuixTheme.textStyles.footnote1,
                                            color = MiuixTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = selected,
                                    onClick = { selectedTableId = table.id },
                                )
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.add_24px),
                        contentDescription = stringResource(Res.string.a11y_add_new_table),
                        tint = MiuixTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.weight(1f))
                TextButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = onDismiss,
                )
                TextButton(
                    text = stringResource(Res.string.action_confirm),
                    onClick = { selectedTableId?.let(onSelected) },
                    enabled = selectedTableId != null,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }

    if (showAddDialog) {
        var tableName by remember { mutableStateOf("") }
        val addSuccess = stringResource(Res.string.toast_add_table_success, tableName)
        val nameEmpty = stringResource(Res.string.toast_name_empty)
        WindowDialog(
            show = true,
            title = stringResource(Res.string.dialog_title_add_table),
            onDismissRequest = { showAddDialog = false },
            insideMargin = DpSize(16.dp, 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextField(
                    value = tableName,
                    onValueChange = { tableName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.label_table_name),
                    singleLine = true,
                )
                DialogActions(
                    confirmText = stringResource(Res.string.action_add),
                    confirmEnabled = tableName.isNotBlank(),
                    onDismiss = { showAddDialog = false },
                    onConfirm = {
                        if (tableName.isBlank()) {
                            ToastManager.show(nameEmpty)
                        } else {
                            scope.launch { courseTableRepository.createNewCourseTable(tableName) }
                            ToastManager.show(addSuccess)
                            showAddDialog = false
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DialogActions(
    confirmText: String = stringResource(Res.string.action_confirm),
    confirmEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
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
            text = confirmText,
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = confirmEnabled,
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
    }
}

private enum class AlarmChoice {
    NONE,
    ON_TIME,
    BEFORE,
}
