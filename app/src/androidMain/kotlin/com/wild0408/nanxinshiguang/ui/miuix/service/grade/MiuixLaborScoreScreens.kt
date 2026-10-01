package com.wild0408.nanxinshiguang.ui.miuix.service.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.data.model.LaborOfficialResult
import com.wild0408.nanxinshiguang.data.model.LaborScore
import com.wild0408.nanxinshiguang.data.model.updatedAtText
import com.wild0408.nanxinshiguang.data.repository.LaborScoreState
import com.wild0408.nanxinshiguang.data.repository.LaborScoreStatus
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.components.laborConfirmationText
import com.wild0408.nanxinshiguang.ui.components.laborFiledText
import com.wild0408.nanxinshiguang.ui.components.laborLiveFields
import com.wild0408.nanxinshiguang.ui.components.laborOfficialFields
import com.wild0408.nanxinshiguang.ui.components.laborOverviewFields
import com.wild0408.nanxinshiguang.ui.components.laborScoreEmptyMessage
import com.wild0408.nanxinshiguang.ui.components.laborScoreEmptyTitle
import com.wild0408.nanxinshiguang.ui.components.laborScoreSource
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.viewmodel.service.labor.LaborScoreViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.labor_score_bind
import nanxinshiguang.generated.resources.labor_score_confirmed
import nanxinshiguang.generated.resources.labor_score_filed
import nanxinshiguang.generated.resources.labor_score_live
import nanxinshiguang.generated.resources.labor_score_live_source
import nanxinshiguang.generated.resources.labor_score_loading
import nanxinshiguang.generated.resources.labor_score_official
import nanxinshiguang.generated.resources.labor_score_official_empty
import nanxinshiguang.generated.resources.labor_score_official_source
import nanxinshiguang.generated.resources.labor_score_pending
import nanxinshiguang.generated.resources.labor_score_refresh
import nanxinshiguang.generated.resources.labor_score_retry
import nanxinshiguang.generated.resources.labor_score_total
import nanxinshiguang.generated.resources.labor_score_updated_date
import nanxinshiguang.generated.resources.labor_score_updated_format
import nanxinshiguang.generated.resources.labor_score_filed_date
import nanxinshiguang.generated.resources.list_alt_24px
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.title_labor_score
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixLaborScoreOverviewCard(onNavigate: () -> Unit, onBind: () -> Unit, viewModel: LaborScoreViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val score = state.score
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onNavigate,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.title_labor_score), style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
                    Text(state.score?.let { laborScoreSource(it) } ?: laborScoreEmptyTitle(state), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                if (state.loading) CircularProgressIndicator(Modifier.size(22.dp))
                else IconButton(onClick = viewModel::refresh) { Icon(vectorResource(Res.drawable.refresh_24px), stringResource(Res.string.labor_score_refresh), tint = MiuixTheme.colorScheme.primary) }
            }
            if (score != null) MiuixLaborOverviewData(score)
            else MiuixLaborOverviewEmpty(state, onBind, viewModel::refresh)
            if (state.error != null && score != null) MiuixLaborError(state, onBind)
        }
    }
}

@Composable
private fun MiuixLaborOverviewData(score: LaborScore) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.labor_score_total), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Text(score.officialTotalDisplay, style = MiuixTheme.textStyles.title1, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.primary)
        }
        if (!score.hasOfficialData) Text(stringResource(Res.string.labor_score_pending), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
    MiuixLaborValueGrid(laborOverviewFields(score))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("同步于 ${score.updatedAtText()}", Modifier.weight(1f), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Icon(vectorResource(Res.drawable.chevron_right_24px), contentDescription = null, tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

@Composable
private fun MiuixLaborOverviewEmpty(state: LaborScoreState, onBind: () -> Unit, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(laborScoreEmptyMessage(state), style = MiuixTheme.textStyles.body2, color = if (state.error == null) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.error)
        when (state.status) {
            LaborScoreStatus.NotLoggedIn -> TextButton(stringResource(Res.string.labor_score_bind), onBind)
            LaborScoreStatus.Empty, LaborScoreStatus.Error -> TextButton(stringResource(Res.string.labor_score_retry), onRetry)
            else -> Unit
        }
    }
}

@Composable
fun MiuixLaborScoreDetailsScreen(onBack: () -> Unit, onBind: () -> Unit, viewModel: LaborScoreViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val score = state.score
    val title = stringResource(Res.string.title_labor_score)
    val scrollBehavior = rememberSharedScrollBehavior()
    val background = MiuixTheme.colorScheme.surface
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
                endAction = { backdropAlpha, shadowAlpha ->
                    if (state.loading) {
                        CircularProgressIndicator(Modifier.size(22.dp))
                    } else {
                        HyperLiquidTopBarButton(
                            onClick = viewModel::refresh,
                            backdrop = backdrop,
                            icon = vectorResource(Res.drawable.refresh_24px),
                            contentDescription = stringResource(Res.string.labor_score_refresh),
                            backdropAlpha = backdropAlpha,
                            shadowAlpha = shadowAlpha,
                        )
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        val contentPadding = PaddingValues(
            start = scaffoldPadding.calculateLeftPadding(layoutDirection) + 20.dp,
            top = scaffoldPadding.calculateTopPadding() + 12.dp,
            end = scaffoldPadding.calculateRightPadding(layoutDirection) + 20.dp,
            bottom = scaffoldPadding.calculateBottomPadding() +
                hostPadding.calculateBottomPadding() + 20.dp,
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (score == null) item(key = "empty") { MiuixLaborEmptyState(state, onBind, viewModel::refresh) }
                else {
                    item(key = "summary") { MiuixLaborSummary(score) }
                    if (state.error != null) item(key = "error") { MiuixLaborError(state, onBind) }
                    item(key = "official") {
                        if (score.hasOfficialData) MiuixLaborOfficialSection(requireNotNull(score.official))
                        else MiuixLaborSection(stringResource(Res.string.labor_score_official), stringResource(Res.string.labor_score_official_source)) { Text(stringResource(Res.string.labor_score_official_empty), color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
                    }
                    if (score.hasLiveData) item(key = "live") { MiuixLaborLiveSection(score) }
                }
            }
        }
    }
}

@Composable
private fun MiuixLaborSummary(score: LaborScore) {
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.primaryContainer)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(Res.string.labor_score_total),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                score.officialTotalDisplay,
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onPrimaryContainer,
            )
            if (!score.hasOfficialData) {
                Text(
                    stringResource(Res.string.labor_score_pending),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                stringResource(Res.string.labor_score_updated_format, score.updatedAtText()),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable private fun MiuixLaborOfficialSection(result: LaborOfficialResult) {
    MiuixLaborSection(stringResource(Res.string.labor_score_official), stringResource(Res.string.labor_score_official_source)) {
        MiuixLaborValueGrid(laborOfficialFields(result))
        MiuixLaborStatusRow(stringResource(Res.string.labor_score_confirmed), laborConfirmationText(result.confirmed), result.isConfirmed)
        MiuixLaborStatusRow(stringResource(Res.string.labor_score_filed), laborFiledText(result.filed), result.isFiled)
        if (result.updatedAt.isNotBlank()) MiuixLaborDetailRow(stringResource(Res.string.labor_score_updated_date), result.updatedAt)
        if (result.filedAt.isNotBlank()) MiuixLaborDetailRow(stringResource(Res.string.labor_score_filed_date), result.filedAt)
    }
}

@Composable private fun MiuixLaborLiveSection(score: LaborScore) = MiuixLaborSection(stringResource(Res.string.labor_score_live), stringResource(Res.string.labor_score_live_source)) { MiuixLaborValueGrid(laborLiveFields(score)) }

@Composable
private fun MiuixLaborSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            content()
        }
    }
}

@Composable
private fun MiuixLaborValueGrid(values: List<Pair<String, String?>>) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 280.dp || LocalDensity.current.fontScale > 1.3f) 1 else if (maxWidth >= 600.dp) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            values.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { (label, value) -> MiuixLaborValueCell(label, value, Modifier.weight(1f)) }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable private fun MiuixLaborValueCell(label: String, value: String?, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Text(value?.ifBlank { "--" } ?: "--", style = MiuixTheme.textStyles.body2, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun MiuixLaborStatusRow(label: String, value: String, positive: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        MiuixLaborStatusChip(value, positive)
    }
}

/** 状态用圆角标签表达：已确认/已归档用主色容器，待确认/未归档用中性容器。 */
@Composable private fun MiuixLaborStatusChip(text: String, positive: Boolean) {
    val containerColor = if (positive) {
        MiuixTheme.colorScheme.primaryContainer
    } else {
        MiuixTheme.colorScheme.secondaryContainer
    }
    val labelColor = if (positive) {
        MiuixTheme.colorScheme.onPrimaryContainer
    } else {
        MiuixTheme.colorScheme.onSecondaryContainer
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MiuixTheme.textStyles.footnote1, color = labelColor, fontWeight = FontWeight.Medium)
    }
}

@Composable private fun MiuixLaborDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Text(value, Modifier.weight(1f), style = MiuixTheme.textStyles.body2, textAlign = TextAlign.End)
    }
}

@Composable private fun MiuixLaborError(state: LaborScoreState, onBind: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(state.error.orEmpty(), Modifier.fillMaxWidth(), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.error)
        if (state.status == LaborScoreStatus.NotLoggedIn) TextButton(stringResource(Res.string.labor_score_bind), onBind)
    }
}

@Composable
private fun MiuixLaborEmptyState(state: LaborScoreState, onBind: () -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 52.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.loading) CircularProgressIndicator(Modifier.size(28.dp))
        else Icon(vectorResource(Res.drawable.list_alt_24px), contentDescription = null, modifier = Modifier.size(32.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Text(laborScoreEmptyTitle(state), style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(laborScoreEmptyMessage(state), Modifier.widthIn(max = 420.dp), style = MiuixTheme.textStyles.body2, color = if (state.error == null) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.error, textAlign = TextAlign.Center)
        when (state.status) {
            LaborScoreStatus.NotLoggedIn -> TextButton(stringResource(Res.string.labor_score_bind), onBind)
            LaborScoreStatus.Empty, LaborScoreStatus.Error -> TextButton(stringResource(Res.string.labor_score_retry), onRetry)
            else -> Unit
        }
    }
}
