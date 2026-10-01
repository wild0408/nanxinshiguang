package com.wild0408.nanxinshiguang.ui.material.portal

import com.wild0408.nanxinshiguang.ui.viewmodel.portal.PortalAccountViewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.data.portal.PORTAL_BACKUP_MIN_PASSWORD_LENGTH
import com.wild0408.nanxinshiguang.data.portal.PortalCredentialImportResult
import com.wild0408.nanxinshiguang.data.portal.PortalLoginResult
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.tool.FileManagerCallbacks
import com.wild0408.nanxinshiguang.tool.rememberFileManager
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.account_circle_24px
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.portal_account_title
import nanxinshiguang.generated.resources.portal_bind_action
import nanxinshiguang.generated.resources.portal_bind_success
import nanxinshiguang.generated.resources.portal_bound_at
import nanxinshiguang.generated.resources.portal_bound_description
import nanxinshiguang.generated.resources.portal_credential_device
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
import nanxinshiguang.generated.resources.portal_invalid
import nanxinshiguang.generated.resources.portal_password_confirm_label
import nanxinshiguang.generated.resources.portal_password_label
import nanxinshiguang.generated.resources.portal_password_mismatch
import nanxinshiguang.generated.resources.portal_password_too_short
import nanxinshiguang.generated.resources.portal_profile_error
import nanxinshiguang.generated.resources.portal_profile_loading
import nanxinshiguang.generated.resources.portal_rebind_action
import nanxinshiguang.generated.resources.portal_status_loading
import nanxinshiguang.generated.resources.portal_student_id
import nanxinshiguang.generated.resources.portal_transfer_summary
import nanxinshiguang.generated.resources.portal_transfer_title
import nanxinshiguang.generated.resources.portal_unbind_action
import nanxinshiguang.generated.resources.portal_unbind_confirm_message
import nanxinshiguang.generated.resources.portal_unbind_confirm_title
import nanxinshiguang.generated.resources.portal_unbound
import nanxinshiguang.generated.resources.portal_unbound_description
import nanxinshiguang.generated.resources.portal_verify_action

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPortalAccountScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: PortalAccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val verifying by viewModel.verifying.collectAsState()
    val transferring by viewModel.transferring.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirm by remember { mutableStateOf("") }
    var exportError by remember { mutableStateOf<String?>(null) }
    var importPassword by remember { mutableStateOf("") }

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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.portal_account_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.portal_account_title),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val current = state) {
                PortalBindingState.Loading -> item { LoadingCard() }
                PortalBindingState.Unbound -> item {
                    EmptyStateCard(
                        title = stringResource(Res.string.portal_unbound),
                        description = stringResource(Res.string.portal_unbound_description),
                        action = stringResource(Res.string.portal_bind_action),
                        onAction = { onNavigate(Destination.PortalBind) },
                    )
                }
                is PortalBindingState.Invalid -> item {
                    EmptyStateCard(
                        title = stringResource(Res.string.portal_invalid),
                        description = current.message,
                        action = stringResource(Res.string.portal_rebind_action),
                        onAction = { onNavigate(Destination.PortalBind) },
                    )
                }
                is PortalBindingState.Error -> item {
                    EmptyStateCard(
                        title = stringResource(Res.string.portal_profile_error),
                        description = current.message,
                        action = stringResource(Res.string.portal_rebind_action),
                        onAction = { onNavigate(Destination.PortalBind) },
                    )
                }
                is PortalBindingState.Bound -> {
                    item { ProfileHeroCard(current, profile) }
                    item { CredentialDetailsCard(current) }
                    item {
                        ActionsCard(
                            verifying = verifying,
                            onVerify = {
                                viewModel.verify { result -> ToastManager.show(result.toUserMessage()) }
                            },
                            onRebind = { onNavigate(Destination.PortalBind) },
                        )
                    }
                    item {
                        TransferCard(
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
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(vectorResource(Res.drawable.delete_24px), contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(Res.string.portal_unbind_action))
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(Res.string.portal_unbind_confirm_title)) },
            text = { Text(stringResource(Res.string.portal_unbind_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clear()
                    showClearConfirm = false
                }) { Text(stringResource(Res.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(Res.string.portal_export_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(Res.string.portal_export_dialog_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it; exportError = null },
                        label = { Text(stringResource(Res.string.portal_password_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = exportConfirm,
                        onValueChange = { exportConfirm = it; exportError = null },
                        label = { Text(stringResource(Res.string.portal_password_confirm_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    exportError?.let { message ->
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !transferring,
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
                ) { Text(stringResource(Res.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(Res.string.portal_import_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(Res.string.portal_import_dialog_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = { importPassword = it },
                        label = { Text(stringResource(Res.string.portal_password_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !transferring,
                    onClick = {
                        showImportDialog = false
                        fileManager.importFile(listOf("json"))
                    },
                ) { Text(stringResource(Res.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false; importPassword = "" }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun TransferCard(
    transferring: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(Res.string.portal_transfer_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(Res.string.portal_transfer_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onExport,
                    enabled = !transferring,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.portal_export_action))
                }
                OutlinedButton(
                    onClick = onImport,
                    enabled = !transferring,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.portal_import_action))
                }
            }
        }
    }
}

@Composable
private fun CredentialDetailsCard(state: PortalBindingState.Bound) {
    val boundAt = Instant.fromEpochMilliseconds(state.bundle.createdAt)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val boundAtText = "${boundAt.date} ${boundAt.hour.toString().padStart(2, '0')}:${boundAt.minute.toString().padStart(2, '0')}"
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRow(
                label = stringResource(Res.string.portal_credential_device),
                value = state.bundle.deviceName.ifBlank { "拾光" },
            )
            DetailRow(
                label = stringResource(Res.string.portal_bound_at),
                value = boundAtText,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun LoadingCard() {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(Modifier.size(24.dp))
            Text(stringResource(Res.string.portal_status_loading))
        }
    }
}

@Composable
private fun EmptyStateCard(title: String, description: String, action: String, onAction: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(vectorResource(Res.drawable.account_circle_24px), contentDescription = null, modifier = Modifier.size(40.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(action) }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    state: PortalBindingState.Bound,
    profile: PortalUserProfile?,
) {
    val bundle = state.bundle
    val studentId = profile?.studentId ?: bundle.studentId.orEmpty()
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                vectorResource(Res.drawable.account_circle_24px),
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = profile?.name?.takeIf(String::isNotBlank) ?: "—",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(studentId.ifBlank { "****" }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (!profile?.organizationLine.isNullOrBlank()) {
                    Text(profile!!.organizationLine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun ActionsCard(
    verifying: Boolean,
    onVerify: () -> Unit,
    onRebind: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.portal_bound_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onVerify, enabled = !verifying, modifier = Modifier.fillMaxWidth()) {
                if (verifying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.portal_verify_action))
            }
            TextButton(onClick = onRebind, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.portal_rebind_action))
            }
        }
    }
}

private fun PortalLoginResult.toUserMessage(): String = when (this) {
    is PortalLoginResult.Success -> "统一门户登录成功"
    is PortalLoginResult.CredentialInvalid -> message
    is PortalLoginResult.NetworkError -> message
    is PortalLoginResult.LoginError -> message
}
