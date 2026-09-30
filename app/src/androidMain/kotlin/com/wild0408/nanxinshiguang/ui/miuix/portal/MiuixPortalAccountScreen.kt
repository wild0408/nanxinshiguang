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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.data.portal.PortalLoginResult
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperPageScaffold
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.hyperPageScroll
import com.wild0408.nanxinshiguang.ui.viewmodel.portal.PortalAccountViewModel
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
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixPortalAccountScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: PortalAccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val verifying by viewModel.verifying.collectAsState()
    var showClear by remember { mutableStateOf(false) }
    val direction = LocalLayoutDirection.current

    HyperPageScaffold(
        title = stringResource(Res.string.portal_account_title),
        onBack = onBack,
        modifier = Modifier.fillMaxSize(),
    ) { padding, scrollBehavior, _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hyperPageScroll(scrollBehavior),
            contentPadding = PaddingValues(
                start = padding.calculateLeftPadding(direction) + 20.dp,
                top = padding.calculateTopPadding() + 12.dp,
                end = padding.calculateRightPadding(direction) + 20.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
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
                        BasicComponent(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(Res.string.portal_unbind_action),
                            summary = stringResource(Res.string.portal_unbind_confirm_message),
                            startAction = { Icon(vectorResource(Res.drawable.delete_24px), null, tint = MiuixTheme.colorScheme.error) },
                            onClick = { showClear = true },
                        )
                    }
                }
            }
        }
    }

    if (showClear) {
        OverlayDialog(
            show = true,
            title = stringResource(Res.string.portal_unbind_confirm_title),
            onDismissRequest = { showClear = false },
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.portal_unbind_confirm_message), color = MiuixTheme.colorScheme.onSurface)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(stringResource(Res.string.action_cancel), { showClear = false }, Modifier.weight(1f))
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
                    Text(profile!!.organizationLine, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onPrimaryContainer)
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
