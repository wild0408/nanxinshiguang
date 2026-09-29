package com.wild0408.nanxinshiguang.ui.portal

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
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.data.portal.PortalLoginResult
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
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
    var showClearConfirm by remember { mutableStateOf(false) }

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
