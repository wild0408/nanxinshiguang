package com.wild0408.nanxinshiguang.ui.miuix.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.tool.FileManagerCallbacks
import com.wild0408.nanxinshiguang.tool.rememberFileManager
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.settings.backup.BackupTarget
import com.wild0408.nanxinshiguang.ui.settings.backup.BackupUiState
import com.wild0408.nanxinshiguang.ui.settings.backup.BackupViewModel
import com.wild0408.nanxinshiguang.ui.settings.backup.TestResult
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import okio.Buffer
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.action_reset
import nanxinshiguang.generated.resources.backup_target_local_zip
import nanxinshiguang.generated.resources.backup_target_webdav
import nanxinshiguang.generated.resources.cloud_24px
import nanxinshiguang.generated.resources.desc_backup_data
import nanxinshiguang.generated.resources.desc_restore_data
import nanxinshiguang.generated.resources.desc_webdav_connected
import nanxinshiguang.generated.resources.desc_webdav_path_hint
import nanxinshiguang.generated.resources.desc_webdav_unconfigured
import nanxinshiguang.generated.resources.dialog_title_backup_target
import nanxinshiguang.generated.resources.dialog_title_config_webdav
import nanxinshiguang.generated.resources.dialog_title_restore_source
import nanxinshiguang.generated.resources.download_24px
import nanxinshiguang.generated.resources.error_stream_open_failed
import nanxinshiguang.generated.resources.error_webdav_unconfigured
import nanxinshiguang.generated.resources.item_backup_data
import nanxinshiguang.generated.resources.item_backup_restore
import nanxinshiguang.generated.resources.item_restore_data
import nanxinshiguang.generated.resources.item_webdav_config
import nanxinshiguang.generated.resources.label_webdav_account
import nanxinshiguang.generated.resources.label_webdav_path
import nanxinshiguang.generated.resources.label_webdav_pwd_empty
import nanxinshiguang.generated.resources.label_webdav_pwd_saved
import nanxinshiguang.generated.resources.label_webdav_url
import nanxinshiguang.generated.resources.section_data_maintenance
import nanxinshiguang.generated.resources.section_service_config
import nanxinshiguang.generated.resources.title_loading
import nanxinshiguang.generated.resources.toast_operation_failed
import nanxinshiguang.generated.resources.toast_operation_success
import nanxinshiguang.generated.resources.upload_24px
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Clock

@Composable
internal fun MiuixBackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showConfig by remember { mutableStateOf(false) }
    var targetDialog by remember { mutableStateOf<MiuixBackupAction?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }
    val title = stringResource(Res.string.item_backup_restore)
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }
    val hostPadding = LocalNavigationHostPadding.current
    val layoutDirection = LocalLayoutDirection.current
    val streamError = stringResource(Res.string.error_stream_open_failed)
    val unconfigured = stringResource(Res.string.error_webdav_unconfigured)
    val success = stringResource(Res.string.toast_operation_success)
    val failurePrefix = stringResource(Res.string.toast_operation_failed, "")

    val fileManager = rememberFileManager(
        FileManagerCallbacks(
            onFileImported = { bytes, _ ->
                if (bytes == null) ToastManager.show(streamError)
                else scope.launch { viewModel.importFromLocalZip(Buffer().write(bytes)) }
            },
            onFileExported = { ok -> if (!ok) ToastManager.show(streamError) },
        ),
    )

    LaunchedEffect(state.testResult) {
        when (val result = state.testResult) {
            TestResult.Idle -> Unit
            TestResult.Success -> ToastManager.show(success)
            is TestResult.Error -> ToastManager.show(failurePrefix + result.message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title,
                largeTitle = title,
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { backdropAlpha, shadowAlpha ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(Res.string.a11y_back),
                        backdropAlpha = backdropAlpha,
                        shadowAlpha = shadowAlpha,
                    )
                },
            )
        },
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .layerBackdrop(backdrop),
        ) {
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = scaffoldPadding.calculateLeftPadding(layoutDirection) + 20.dp,
                top = scaffoldPadding.calculateTopPadding() + 12.dp,
                end = scaffoldPadding.calculateRightPadding(layoutDirection) + 20.dp,
                bottom = scaffoldPadding.calculateBottomPadding() +
                    hostPadding.calculateBottomPadding() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isBusy || state.isTesting) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            item {
                MiuixBackupGroup(stringResource(Res.string.section_data_maintenance)) {
                    MiuixBackupRow(Res.drawable.upload_24px, stringResource(Res.string.item_backup_data), stringResource(Res.string.desc_backup_data), !state.isBusy) {
                        targetDialog = MiuixBackupAction.Backup
                        showTargetDialog = true
                    }
                    MiuixBackupRow(Res.drawable.download_24px, stringResource(Res.string.item_restore_data), stringResource(Res.string.desc_restore_data), !state.isBusy) {
                        targetDialog = MiuixBackupAction.Restore
                        showTargetDialog = true
                    }
                }
            }
            item {
                MiuixBackupGroup(stringResource(Res.string.section_service_config)) {
                    MiuixBackupRow(
                        Res.drawable.cloud_24px,
                        stringResource(Res.string.item_webdav_config),
                        if (state.baseUrl.isBlank()) stringResource(Res.string.desc_webdav_unconfigured) else stringResource(Res.string.desc_webdav_connected, state.baseUrl),
                    ) { showConfig = true }
                }
            }
        }
        }

        MiuixWebDavDialog(
            show = showConfig,
            state = state,
            onDismiss = { showConfig = false },
            onSave = { url, user, password, path ->
                viewModel.testWebDavConnection(url, user, password, path)
                showConfig = false
            },
            onReset = {
                viewModel.disconnectWebDav()
                showConfig = false
            },
        )
        MiuixBackupTargetDialog(
            show = showTargetDialog,
            action = targetDialog ?: MiuixBackupAction.Backup,
            onDismiss = { showTargetDialog = false },
            onDismissFinished = { targetDialog = null },
            onSelected = { action, target ->
                showTargetDialog = false
                when (target) {
                    BackupTarget.WEBDAV -> {
                        if (state.baseUrl.isBlank()) ToastManager.show(unconfigured)
                        else if (action == MiuixBackupAction.Backup) viewModel.backupToWebDav()
                        else viewModel.restoreFromWebDav()
                    }
                    BackupTarget.LOCAL_ZIP -> if (action == MiuixBackupAction.Restore) {
                        fileManager.importFile(listOf("zip"))
                    } else scope.launch {
                        val buffer = Buffer()
                        if (viewModel.exportToLocalZip(buffer)) {
                            val bytes = buffer.readByteArray()
                            if (bytes.isEmpty()) ToastManager.show(streamError)
                            else fileManager.exportFile(defaultBackupName(), bytes)
                        }
                    }
                }
            },
        )
    }
}

private enum class MiuixBackupAction { Backup, Restore }

@Composable
private fun MiuixBackupGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallTitle(title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            insideMargin = PaddingValues(vertical = 8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun MiuixBackupRow(icon: org.jetbrains.compose.resources.DrawableResource, title: String, summary: String, enabled: Boolean = true, onClick: () -> Unit) {
    BasicComponent(
        title = title,
        summary = summary,
        enabled = enabled,
        onClick = onClick,
        startAction = { Icon(vectorResource(icon), null, tint = MiuixTheme.colorScheme.primary) },
    )
}

@Composable
private fun MiuixBackupTargetDialog(
    show: Boolean,
    action: MiuixBackupAction,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit,
    onSelected: (MiuixBackupAction, BackupTarget) -> Unit,
) {
    var selected by remember(action) { mutableStateOf(BackupTarget.entries.first()) }
    OverlayDialog(
        show = show,
        title = stringResource(if (action == MiuixBackupAction.Backup) Res.string.dialog_title_backup_target else Res.string.dialog_title_restore_source),
        onDismissRequest = onDismiss,
        onDismissFinished = onDismissFinished,
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BackupTarget.entries.forEach { target ->
                BasicComponent(
                    title = stringResource(if (target == BackupTarget.WEBDAV) Res.string.backup_target_webdav else Res.string.backup_target_local_zip),
                    onClick = { selected = target },
                    startAction = { RadioButton(selected == target, onClick = null) },
                )
            }
            MiuixBackupActions(onDismiss) { onSelected(action, selected) }
        }
    }
}

@Composable
private fun MiuixWebDavDialog(show: Boolean, state: BackupUiState, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit, onReset: () -> Unit) {
    var url by remember(state.baseUrl) { mutableStateOf(state.baseUrl) }
    var username by remember(state.username) { mutableStateOf(state.username) }
    var password by remember { mutableStateOf("") }
    var path by remember(state.rootPath) { mutableStateOf(state.rootPath) }
    OverlayDialog(show = show, title = stringResource(Res.string.dialog_title_config_webdav), onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(url, { url = it }, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_webdav_url), singleLine = true)
            TextField(username, { username = it }, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_webdav_account), singleLine = true)
            TextField(password, { password = it }, Modifier.fillMaxWidth(), label = stringResource(if (state.hasSavedPassword) Res.string.label_webdav_pwd_saved else Res.string.label_webdav_pwd_empty), singleLine = true, visualTransformation = PasswordVisualTransformation())
            TextField(path, { path = it }, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_webdav_path), singleLine = true)
            Text(stringResource(Res.string.desc_webdav_path_hint), color = MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.footnote1)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(stringResource(Res.string.action_reset), onReset, Modifier.weight(1f), colors = ButtonDefaults.textButtonColors(color = MiuixTheme.colorScheme.error))
                TextButton(if (state.isTesting) stringResource(Res.string.title_loading) else stringResource(Res.string.action_confirm), { onSave(url, username, password, path) }, Modifier.weight(1f), enabled = !state.isTesting && url.isNotBlank(), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable
private fun MiuixBackupActions(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(stringResource(Res.string.action_cancel), onDismiss, Modifier.weight(1f))
        TextButton(stringResource(Res.string.action_confirm), onConfirm, Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
    }
}

private fun defaultBackupName(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "Shiguang_Backup_%04d%02d%02d.zip".format(now.year, now.month.number, now.day)
}
