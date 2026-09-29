package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.data.model.RepoType
import com.wild0408.nanxinshiguang.data.model.RepositoryInfo
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.settings.update.UpdateRepoViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_update
import nanxinshiguang.generated.resources.action_updating
import nanxinshiguang.generated.resources.label_password_or_token_value
import nanxinshiguang.generated.resources.label_private_repo_credentials
import nanxinshiguang.generated.resources.label_repo_branch
import nanxinshiguang.generated.resources.label_repo_url
import nanxinshiguang.generated.resources.label_select_repo
import nanxinshiguang.generated.resources.label_username_or_token_key
import nanxinshiguang.generated.resources.text_select_repo_hint
import nanxinshiguang.generated.resources.title_update_log
import nanxinshiguang.generated.resources.title_update_repo_screen
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixUpdateRepoScreen(
    onBack: () -> Unit,
    viewModel: UpdateRepoViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val visibleRepos = state.repoList.filter { state.isDeveloperModeEnabled || (it.repoType != RepoType.CUSTOM && it.repoType != RepoType.PRIVATE_REPO) }
    val title = stringResource(Res.string.title_update_repo_screen)
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }
    val hostPadding = LocalNavigationHostPadding.current
    val layoutDirection = LocalLayoutDirection.current

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
                bottom = scaffoldPadding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallTitle(stringResource(Res.string.label_select_repo))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                    ) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            WindowDropdownPreference(
                                items = visibleRepos.map(RepositoryInfo::name),
                                selectedIndex = visibleRepos.indexOfFirst { it == state.selectedRepo }.coerceAtLeast(0),
                                title = stringResource(Res.string.label_select_repo),
                                summary = state.selectedRepo?.name ?: stringResource(Res.string.text_select_repo_hint),
                                onSelectedIndexChange = { index -> visibleRepos.getOrNull(index)?.let(viewModel::selectRepository) },
                            )
                            if (state.selectedRepo?.editable == true) {
                                TextField(state.currentEditableUrl, viewModel::updateCurrentUrl, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_repo_url), singleLine = true)
                                TextField(state.currentEditableBranch, viewModel::updateCurrentBranch, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_repo_branch), singleLine = true)
                                if (state.selectedRepo?.repoType == RepoType.PRIVATE_REPO) {
                                    Text(stringResource(Res.string.label_private_repo_credentials), style = MiuixTheme.textStyles.title3)
                                    TextField(state.currentEditableUsername, viewModel::updateCurrentUsername, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_username_or_token_key), singleLine = true)
                                    TextField(state.currentEditablePassword, viewModel::updateCurrentPassword, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_password_or_token_value), singleLine = true, visualTransformation = PasswordVisualTransformation())
                                }
                            }
                            Button(
                                onClick = viewModel::startUpdate,
                                enabled = !state.isUpdating && state.selectedRepo != null,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(stringResource(if (state.isUpdating) Res.string.action_updating else Res.string.action_update)) }
                            if (state.isUpdating) LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            item {
                MiuixRepoLog(state.logs)
            }
        }
        }
    }
}

@Composable
private fun MiuixRepoLog(logs: String) {
    val scrollState = rememberScrollState()
    LaunchedEffect(logs) { if (logs.isNotEmpty()) scrollState.scrollTo(scrollState.maxValue) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallTitle(stringResource(Res.string.title_update_log))
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            SelectionContainer {
                Text(
                    text = logs.ifBlank { " " },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 320.dp).verticalScroll(scrollState),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.footnote1.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
    }
}
