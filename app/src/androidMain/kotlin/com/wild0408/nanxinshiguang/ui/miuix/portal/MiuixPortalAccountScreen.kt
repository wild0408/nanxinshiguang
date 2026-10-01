package com.wild0408.nanxinshiguang.ui.miuix.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.data.portal.PORTAL_BACKUP_MIN_PASSWORD_LENGTH
import com.wild0408.nanxinshiguang.data.portal.PortalCredentialImportResult
import com.wild0408.nanxinshiguang.data.portal.PortalLoginResult
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.tool.FileManagerCallbacks
import com.wild0408.nanxinshiguang.tool.rememberFileManager
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.viewmodel.portal.PortalAccountViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.account_circle_24px
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.portal_account_title
import nanxinshiguang.generated.resources.portal_bind_action
import nanxinshiguang.generated.resources.portal_bind_success
import nanxinshiguang.generated.resources.portal_bound_at
import nanxinshiguang.generated.resources.portal_bound_description
import nanxinshiguang.generated.resources.portal_credential_device
import nanxinshiguang.generated.resources.portal_invalid
import nanxinshiguang.generated.resources.portal_profile_error
import nanxinshiguang.generated.resources.portal_profile_loading
import nanxinshiguang.generated.resources.portal_rebind_action
import nanxinshiguang.generated.resources.portal_status_loading
import nanxinshiguang.generated.resources.portal_student_id
import nanxinshiguang.generated.resources.portal_unbind_action
import nanxinshiguang.generated.resources.portal_unbind_confirm_message
import nanxinshiguang.generated.resources.portal_unbind_confirm_title
import nanxinshiguang.generated.resources.portal_unbound
import nanxinshiguang.generated.resources.portal_unbound_description
import nanxinshiguang.generated.resources.portal_export_action
import nanxinshiguang.generated.resources.portal_export_cancelled
import nanxinshiguang.generated.resources.portal_export_dialog_message
import nanxinshiguang.generated.resources.portal_export_dialog_title
import nanxinshiguang.generated.resources.portal_export_failed
import nanxinshiguang.generated.resources.portal_export_success
import nanxinshiguang.generated.resources.portal_import_action
import nanxinshiguang.generated.resources.portal_import_cancelled
import nanxinshiguang.generated.resources.portal_import_dialog_message
import nanxinshiguang.generated.resources.portal_import_dialog_title
import nanxinshiguang.generated.resources.portal_import_invalid
import nanxinshiguang.generated.resources.portal_import_success
import nanxinshiguang.generated.resources.portal_import_wrong_password
import nanxinshiguang.generated.resources.portal_password_confirm_label
import nanxinshiguang.generated.resources.portal_password_label
import nanxinshiguang.generated.resources.portal_password_mismatch
import nanxinshiguang.generated.resources.portal_password_too_short
import nanxinshiguang.generated.resources.portal_transfer_summary
import nanxinshiguang.generated.resources.portal_transfer_title
import nanxinshiguang.generated.resources.portal_verify_action
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun MiuixPortalAccountScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: PortalAccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val verifying by viewModel.verifying.collectAsStateWithLifecycle()
    val transferring by viewModel.transferring.collectAsStateWithLifecycle()
    var showClear by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirm by remember { mutableStateOf("") }
    var exportError by remember { mutableStateOf<String?>(null) }
    var importPassword by remember { mutableStateOf("") }

    val title = stringResource(Res.string.portal_account_title)
    val scrollBehavior = rememberSharedScrollBehavior()
    val background = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }
    val hostPadding = LocalNavigationHostPadding.current
    val layoutDirection = LocalLayoutDirection.current

    // 在 Composable 作用域内先解析文案，供文件选择/保存回调使用。
    val passwordTooShortMessage =
        stringResource(Res.string.portal_password_too_short, PORTAL_BACKUP_MIN_PASSWORD_LENGTH)
    val passwordMismatchMessage = stringResource(Res.string.portal_password_mismatch)
    val exportSuccessMessage = stringResource(Res.string.portal_export_success)
    val exportFailedMessage = stringResource(Res.string.portal_export_failed)
    val exportCancelledMessage = stringResource(Res.string.portal_export_cancelled)
    val importCancelledMessage = stringResource(Res.string.portal_import_cancelled)
    val importSuccessMessage = stringResource(Res.string.portal_import_success)
    val importWrongPasswordMessage = stringResource(Res.string.portal_import_wrong_password)
    val importInvalidPrefix = stringResource(Res.string.portal_import_invalid)

    val fileManager = rememberFileManager(
        FileManagerCallbacks(
            onFileImported = { bytes, _ ->
                if (bytes == null || bytes.isEmpty()) {
                    ToastManager.show(importCancelledMessage)
                } else {
                    viewModel.importCredential(bytes, importPassword) { result ->
                        when (result) {
                            is PortalCredentialImportResult.Success -> {
                                ToastManager.show(importSuccessMessage)
                                showImportDialog = false
                                importPassword = ""
                            }

                            PortalCredentialImportResult.WrongPassword ->
                                ToastManager.show(importWrongPasswordMessage)

                            is PortalCredentialImportResult.Invalid ->
                                ToastManager.show("$importInvalidPrefix${result.message}")
                        }
                    }
                }
            },
            onFileExported = { success ->
                ToastManager.show(if (success) exportSuccessMessage else exportCancelledMessage)
            },
        )
    )

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
        val contentPadding = PaddingValues(
            start = scaffoldPadding.calculateLeftPadding(layoutDirection) + 20.dp,
            top = scaffoldPadding.calculateTopPadding() + 12.dp,
            end = scaffoldPadding.calculateRightPadding(layoutDirection) + 20.dp,
            bottom = scaffoldPadding.calculateBottomPadding() +
                hostPadding.calculateBottomPadding() + 24.dp,
        )

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
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (val current = state) {
                    PortalBindingState.Loading -> item { MiuixLoadingCard() }
                    PortalBindingState.Unbound -> item {
                        MiuixEmptyCard(
                            title = stringResource(Res.string.portal_unbound),
                            summary = stringResource(Res.string.portal_unbound_description),
                            action = stringResource(Res.string.portal_bind_action),
                        ) { onNavigate(Destination.PortalBind) }
                    }

                    is PortalBindingState.Invalid -> item {
                        MiuixEmptyCard(
                            title = stringResource(Res.string.portal_invalid),
                            summary = current.message,
                            action = stringResource(Res.string.portal_rebind_action),
                        ) { onNavigate(Destination.PortalBind) }
                    }

                    is PortalBindingState.Error -> item {
                        MiuixEmptyCard(
                            title = stringResource(Res.string.portal_profile_error),
                            summary = current.message,
                            action = stringResource(Res.string.portal_rebind_action),
                        ) { onNavigate(Destination.PortalBind) }
                    }

                    is PortalBindingState.Bound -> {
                        item { MiuixProfileHero(current, profile) }
                        item { MiuixCredentialDetailsCard(current) }
                        item {
                            MiuixPortalActions(
                                verifying = verifying,
                                onVerify = {
                                    viewModel.verify { result -> ToastManager.show(result.toUserMessage()) }
                                },
                                onRebind = { onNavigate(Destination.PortalBind) },
                            )
                        }
                        item {
                            MiuixCredentialTransferCard(
                                transferring = transferring,
                                onExport = {
                                    exportPassword = ""
                                    exportConfirm = ""
                                    exportError = null
                                    showExportDialog = true
                                },
                                onImport = {
                                    importPassword = ""
                                    showImportDialog = true
                                },
                            )
                        }
                        item {
                            BasicComponent(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(Res.string.portal_unbind_action),
                                summary = stringResource(Res.string.portal_unbind_confirm_message),
                                startAction = {
                                    Icon(
                                        vectorResource(Res.drawable.delete_24px),
                                        null,
                                        tint = MiuixTheme.colorScheme.error,
                                    )
                                },
                                onClick = { showClear = true },
                            )
                        }
                    }
                }
            }

            // 官方 Overlay 与 Window 弹层必须留在 Scaffold 的 Popup Host 作用域内。
            if (showClear) {
                OverlayDialog(
                    show = true,
                    title = stringResource(Res.string.portal_unbind_confirm_title),
                    onDismissRequest = { showClear = false },
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(Res.string.portal_unbind_confirm_message),
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                stringResource(Res.string.action_cancel),
                                { showClear = false },
                                Modifier.weight(1f),
                            )
                            TextButton(
                                stringResource(Res.string.action_confirm),
                                { viewModel.clear(); showClear = false },
                                Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                }
            }

            if (showExportDialog) {
                WindowDialog(
                    show = true,
                    title = stringResource(Res.string.portal_export_dialog_title),
                    onDismissRequest = { showExportDialog = false },
                    insideMargin = DpSize(16.dp, 16.dp),
                ) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(Res.string.portal_export_dialog_message),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        TextField(
                            value = exportPassword,
                            onValueChange = { exportPassword = it; exportError = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.portal_password_label),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                        )
                        TextField(
                            value = exportConfirm,
                            onValueChange = { exportConfirm = it; exportError = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.portal_password_confirm_label),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                        )
                        exportError?.let { message ->
                            Text(
                                message,
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.error,
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                text = stringResource(Res.string.action_cancel),
                                onClick = { showExportDialog = false },
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                text = stringResource(Res.string.action_confirm),
                                onClick = {
                                    val validationError = when {
                                        exportPassword.length < PORTAL_BACKUP_MIN_PASSWORD_LENGTH ->
                                            passwordTooShortMessage

                                        exportPassword != exportConfirm -> passwordMismatchMessage
                                        else -> null
                                    }
                                    if (validationError != null) {
                                        exportError = validationError
                                    } else {
                                        viewModel.exportCredential(exportPassword) { bytes, fileName ->
                                            if (bytes == null) {
                                                ToastManager.show(exportFailedMessage)
                                            } else {
                                                showExportDialog = false
                                                exportPassword = ""
                                                exportConfirm = ""
                                                fileManager.exportFile(fileName, bytes)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                enabled = !transferring,
                            )
                        }
                    }
                }
            }

            if (showImportDialog) {
                WindowDialog(
                    show = true,
                    title = stringResource(Res.string.portal_import_dialog_title),
                    onDismissRequest = { showImportDialog = false },
                    insideMargin = DpSize(16.dp, 16.dp),
                ) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(Res.string.portal_import_dialog_message),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        TextField(
                            value = importPassword,
                            onValueChange = { importPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.portal_password_label),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                text = stringResource(Res.string.action_cancel),
                                onClick = { showImportDialog = false; importPassword = "" },
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                text = stringResource(Res.string.action_confirm),
                                onClick = {
                                    showImportDialog = false
                                    fileManager.importFile(listOf("json"))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                enabled = !transferring,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixCredentialTransferCard(
    transferring: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(Res.string.portal_transfer_title),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(Res.string.portal_transfer_summary),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onExport,
                    enabled = !transferring,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.portal_export_action))
                }
                TextButton(
                    text = stringResource(Res.string.portal_import_action),
                    onClick = onImport,
                    modifier = Modifier.weight(1f),
                    enabled = !transferring,
                )
            }
        }
    }
}

@Composable
private fun MiuixCredentialDetailsCard(state: PortalBindingState.Bound) {
    val boundAt = Instant.fromEpochMilliseconds(state.bundle.createdAt)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val boundAtText = "${boundAt.date} ${boundAt.hour.toString().padStart(2, '0')}:${boundAt.minute.toString().padStart(2, '0')}"
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MiuixDetailRow(
                label = stringResource(Res.string.portal_credential_device),
                value = state.bundle.deviceName.ifBlank { "拾光" },
            )
            MiuixDetailRow(
                label = stringResource(Res.string.portal_bound_at),
                value = boundAtText,
            )
        }
    }
}

@Composable
private fun MiuixDetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Spacer(Modifier.weight(1f))
        Text(value, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MiuixLoadingCard() {
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(Modifier.size(24.dp))
            Text(stringResource(Res.string.portal_status_loading))
        }
    }
}

@Composable
private fun MiuixEmptyCard(title: String, summary: String, action: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(vectorResource(Res.drawable.account_circle_24px), null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Text(title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
            Text(summary, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(action) }
        }
    }
}

@Composable
private fun MiuixProfileHero(state: PortalBindingState.Bound, profile: PortalUserProfile?) {
    val studentId = profile?.studentId ?: state.bundle.studentId.orEmpty()
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.primaryContainer)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(MiuixTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    profile?.initial ?: "?",
                    style = MiuixTheme.textStyles.title1,
                    color = MiuixTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(profile?.name?.ifBlank { null } ?: "—", style = MiuixTheme.textStyles.title1, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onPrimaryContainer)
                Text(studentId.ifBlank { "****" }, style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onPrimaryContainer)
                if (!profile?.organizationLine.isNullOrBlank()) {
                    Text(profile.organizationLine, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun MiuixPortalActions(
    verifying: Boolean,
    onVerify: () -> Unit,
    onRebind: () -> Unit,
) {
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.portal_bound_description), style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Button(onClick = onVerify, enabled = !verifying, modifier = Modifier.fillMaxWidth()) {
                if (verifying) CircularProgressIndicator(Modifier.size(18.dp))
                else Text(stringResource(Res.string.portal_verify_action))
            }
            TextButton(stringResource(Res.string.portal_rebind_action), onRebind, Modifier.fillMaxWidth())
        }
    }
}

private fun PortalLoginResult.toUserMessage(): String = when (this) {
    is PortalLoginResult.Success -> "统一门户登录成功"
    is PortalLoginResult.CredentialInvalid -> message
    is PortalLoginResult.NetworkError -> message
    is PortalLoginResult.LoginError -> message
}
