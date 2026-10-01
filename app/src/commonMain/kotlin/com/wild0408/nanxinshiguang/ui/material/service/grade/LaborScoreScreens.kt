package com.wild0408.nanxinshiguang.ui.material.service.grade

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.LaborOfficialResult
import com.wild0408.nanxinshiguang.data.model.LaborScore
import com.wild0408.nanxinshiguang.data.model.updatedAtText
import com.wild0408.nanxinshiguang.data.repository.LaborScoreState
import com.wild0408.nanxinshiguang.data.repository.LaborScoreStatus
import com.wild0408.nanxinshiguang.ui.viewmodel.service.labor.LaborScoreViewModel
import com.wild0408.nanxinshiguang.ui.components.laborConfirmationText
import com.wild0408.nanxinshiguang.ui.components.laborFiledText
import com.wild0408.nanxinshiguang.ui.components.laborLiveFields
import com.wild0408.nanxinshiguang.ui.components.laborOfficialFields
import com.wild0408.nanxinshiguang.ui.components.laborOverviewFields
import com.wild0408.nanxinshiguang.ui.components.laborScoreEmptyMessage
import com.wild0408.nanxinshiguang.ui.components.laborScoreEmptyTitle
import com.wild0408.nanxinshiguang.ui.components.laborScoreSource
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.labor_score_bind
import nanxinshiguang.generated.resources.labor_score_confirmed
import nanxinshiguang.generated.resources.labor_score_confirmed_no
import nanxinshiguang.generated.resources.labor_score_confirmed_yes
import nanxinshiguang.generated.resources.labor_score_contest
import nanxinshiguang.generated.resources.labor_score_empty_detail
import nanxinshiguang.generated.resources.labor_score_filed
import nanxinshiguang.generated.resources.labor_score_filed_no
import nanxinshiguang.generated.resources.labor_score_filed_yes
import nanxinshiguang.generated.resources.labor_score_live
import nanxinshiguang.generated.resources.labor_score_loading
import nanxinshiguang.generated.resources.labor_score_major_course
import nanxinshiguang.generated.resources.labor_score_major_total
import nanxinshiguang.generated.resources.labor_score_no_data
import nanxinshiguang.generated.resources.labor_score_not_bound
import nanxinshiguang.generated.resources.labor_score_official
import nanxinshiguang.generated.resources.labor_score_query_hint
import nanxinshiguang.generated.resources.labor_score_refresh_failed
import nanxinshiguang.generated.resources.labor_score_retry
import nanxinshiguang.generated.resources.labor_score_service
import nanxinshiguang.generated.resources.labor_score_summary
import nanxinshiguang.generated.resources.labor_score_theory
import nanxinshiguang.generated.resources.labor_score_total
import nanxinshiguang.generated.resources.labor_score_updated_format
import nanxinshiguang.generated.resources.labor_score_life
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.title_labor_score
import nanxinshiguang.generated.resources.labor_score_pending
import nanxinshiguang.generated.resources.labor_score_refresh
import nanxinshiguang.generated.resources.labor_score_official_source
import nanxinshiguang.generated.resources.labor_score_official_empty
import nanxinshiguang.generated.resources.labor_score_live_source
import nanxinshiguang.generated.resources.labor_score_updated_date
import nanxinshiguang.generated.resources.labor_score_filed_date
import nanxinshiguang.generated.resources.list_alt_24px

@Composable
fun LaborScoreOverviewCard(
    onNavigate: (Destination) -> Unit,
    onBind: () -> Unit,
    viewModel: LaborScoreViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val score = state.score
    Card(
        onClick = { onNavigate(Destination.LaborScoreDetails) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.title_labor_score), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(state.score?.let { laborScoreSource(it) } ?: laborScoreEmptyTitle(state), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LaborRefreshButton(state.loading, viewModel::refresh)
            }
            when {
                score != null -> LaborOverviewData(score)
                else -> LaborOverviewEmpty(state, onBind, viewModel::refresh)
            }
            if (state.error != null && score != null) LaborErrorNote(state, onBind)
        }
    }
}

@Composable
private fun LaborOverviewData(score: LaborScore) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.labor_score_total), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(score.officialTotalDisplay, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        if (!score.hasOfficialData) Text(stringResource(Res.string.labor_score_pending), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    LaborValueGrid(laborOverviewFields(score))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(Res.string.labor_score_updated_format, score.updatedAtText()), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(vectorResource(Res.drawable.chevron_right_24px), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LaborOverviewEmpty(state: LaborScoreState, onBind: () -> Unit, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(laborScoreEmptyMessage(state), style = MaterialTheme.typography.bodyMedium, color = state.errorColor())
        when (state.status) {
            LaborScoreStatus.NotLoggedIn -> TextButton(onClick = onBind) { Text(stringResource(Res.string.labor_score_bind)) }
            LaborScoreStatus.Empty, LaborScoreStatus.Error -> TextButton(onClick = onRetry) { Text(stringResource(Res.string.labor_score_retry)) }
            else -> Unit
        }
    }
}

@Composable
private fun LaborRefreshButton(loading: Boolean, onRefresh: () -> Unit) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        else IconButton(onClick = onRefresh) { Icon(vectorResource(Res.drawable.refresh_24px), stringResource(Res.string.labor_score_refresh)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborScoreDetailsScreen(onBack: () -> Unit, onBind: () -> Unit, viewModel: LaborScoreViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val score = state.score
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.title_labor_score)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(vectorResource(Res.drawable.arrow_back_24px), stringResource(Res.string.a11y_back)) } },
                actions = { LaborRefreshButton(state.loading, viewModel::refresh) },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (score == null) item(key = "empty") { LaborEmptyState(state, onBind, viewModel::refresh) }
            else {
                item(key = "summary") { LaborSummaryCard(score) }
                if (state.error != null) item(key = "error") { LaborErrorNote(state, onBind) }
                item(key = "official") {
                    if (score.hasOfficialData) LaborOfficialCard(requireNotNull(score.official))
                    else LaborSectionCard(stringResource(Res.string.labor_score_official), stringResource(Res.string.labor_score_official_source)) {
                        Text(stringResource(Res.string.labor_score_official_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (score.hasLiveData) item(key = "live") { LaborLiveCard(score) }
            }
        }
    }
}

@Composable
private fun LaborSummaryCard(score: LaborScore) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.labor_score_total), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(score.officialTotalDisplay, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        if (!score.hasOfficialData) Text(stringResource(Res.string.labor_score_pending), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(Res.string.labor_score_updated_format, score.updatedAtText()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LaborOfficialCard(result: LaborOfficialResult) {
    LaborSectionCard(stringResource(Res.string.labor_score_official), stringResource(Res.string.labor_score_official_source)) {
        LaborValueGrid(laborOfficialFields(result))
        HorizontalDivider()
        LaborStatusRow(stringResource(Res.string.labor_score_confirmed), laborConfirmationText(result.confirmed), result.isConfirmed)
        LaborStatusRow(stringResource(Res.string.labor_score_filed), laborFiledText(result.filed), result.isFiled)
        if (result.updatedAt.isNotBlank()) LaborDetailRow(stringResource(Res.string.labor_score_updated_date), result.updatedAt)
        if (result.filedAt.isNotBlank()) LaborDetailRow(stringResource(Res.string.labor_score_filed_date), result.filedAt)
    }
}

@Composable
private fun LaborLiveCard(score: LaborScore) {
    LaborSectionCard(stringResource(Res.string.labor_score_live), stringResource(Res.string.labor_score_live_source)) {
        LaborValueGrid(laborLiveFields(score))
    }
}

@Composable
private fun LaborSectionCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalDivider()
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
}

@Composable
private fun LaborValueGrid(values: List<Pair<String, String?>>) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 280.dp || LocalDensity.current.fontScale > 1.3f) 1 else if (maxWidth >= 600.dp) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        values.chunked(columns).forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { (label, value) -> LaborValueCell(label, value, Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        }
    }
}

@Composable
private fun LaborValueCell(label: String, value: String?, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value?.ifBlank { "--" } ?: "--", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LaborStatusRow(label: String, value: String, positive: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun LaborDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

@Composable private fun LaborErrorNote(state: LaborScoreState, onBind: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(state.error.orEmpty(), Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        if (state.status == LaborScoreStatus.NotLoggedIn) TextButton(onClick = onBind) { Text(stringResource(Res.string.labor_score_bind)) }
    }
}

@Composable
private fun LaborEmptyState(state: LaborScoreState, onBind: () -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 52.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.loading) CircularProgressIndicator(Modifier.size(28.dp))
        else Icon(vectorResource(Res.drawable.list_alt_24px), contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(laborScoreEmptyTitle(state), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(laborScoreEmptyMessage(state), Modifier.widthIn(max = 420.dp), color = state.errorColor(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        when (state.status) {
            LaborScoreStatus.NotLoggedIn -> TextButton(onClick = onBind) { Text(stringResource(Res.string.labor_score_bind)) }
            LaborScoreStatus.Empty, LaborScoreStatus.Error -> TextButton(onClick = onRetry) { Text(stringResource(Res.string.labor_score_retry)) }
            else -> Unit
        }
    }
}

@Composable private fun LaborScoreState.errorColor() = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
