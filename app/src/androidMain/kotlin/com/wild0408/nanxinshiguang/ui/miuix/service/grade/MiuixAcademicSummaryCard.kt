package com.wild0408.nanxinshiguang.ui.miuix.service.grade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.data.model.AcademicSummary
import com.wild0408.nanxinshiguang.data.model.updatedAtText
import com.wild0408.nanxinshiguang.ui.viewmodel.service.grade.AcademicSummaryUiState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.action_refresh_academic_summary
import nanxinshiguang.generated.resources.label_average_grade_point
import nanxinshiguang.generated.resources.label_average_score
import nanxinshiguang.generated.resources.label_class_rank
import nanxinshiguang.generated.resources.label_credit_progress
import nanxinshiguang.generated.resources.label_earned_required_credits
import nanxinshiguang.generated.resources.label_gpa
import nanxinshiguang.generated.resources.label_major_rank
import nanxinshiguang.generated.resources.label_pass_rate
import nanxinshiguang.generated.resources.label_weighted_average_score
import nanxinshiguang.generated.resources.portal_bind_action
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.status_academic_summary_loading
import nanxinshiguang.generated.resources.status_academic_summary_no_data
import nanxinshiguang.generated.resources.status_academic_summary_not_bound
import nanxinshiguang.generated.resources.status_academic_summary_updated
import nanxinshiguang.generated.resources.title_academic_summary
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixAcademicSummaryCard(
    state: AcademicSummaryUiState,
    onRefresh: () -> Unit,
    onBind: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.title_academic_summary),
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.title3,
                )
                if (state.portalBound) {
                    if (state.loading) CircularProgressIndicator(Modifier.size(24.dp))
                    else IconButton(onClick = onRefresh) {
                        Icon(
                            vectorResource(Res.drawable.refresh_24px),
                            stringResource(Res.string.action_refresh_academic_summary),
                            tint = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
            }
            when {
                state.loading && !state.portalBound -> Text(
                    stringResource(Res.string.status_academic_summary_loading),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                !state.portalBound -> {
                    Text(
                        stringResource(Res.string.status_academic_summary_not_bound),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    top.yukonga.miuix.kmp.basic.Button(onClick = onBind) {
                        Text(stringResource(Res.string.portal_bind_action))
                    }
                }
                state.summary == null -> {
                    Text(
                        if (state.loading) stringResource(Res.string.status_academic_summary_loading)
                        else state.errorMessage ?: stringResource(Res.string.status_academic_summary_no_data),
                        color = if (state.errorMessage == null) MiuixTheme.colorScheme.onSurfaceVariantSummary
                        else MiuixTheme.colorScheme.error,
                    )
                }
                else -> {
                    Text(
                        stringResource(Res.string.status_academic_summary_updated, state.summary.updatedAtText()),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    MiuixAcademicSummaryContent(state.summary)
                    state.errorMessage?.let {
                        Text(it, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixAcademicSummaryContent(summary: AcademicSummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AcademicMetric(stringResource(Res.string.label_average_grade_point), summary.averageGradePoint, Modifier.weight(1f))
        AcademicMetric(stringResource(Res.string.label_gpa), summary.gpa, Modifier.weight(1f))
        AcademicMetric(stringResource(Res.string.label_average_score), summary.averageScore, Modifier.weight(1f))
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(Res.string.label_credit_progress),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            stringResource(
                Res.string.label_earned_required_credits,
                summary.earnedCredits.ifBlank { "--" },
                summary.requiredCredits.ifBlank { "--" },
            ) + "  ${summary.creditProgress.ifBlank { "--" }}",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
    LinearProgressIndicator(
        progress = { summary.creditProgressFraction ?: 0f },
        modifier = Modifier.fillMaxWidth().height(6.dp),
        color = MiuixTheme.colorScheme.primary,
        trackColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.12f),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AcademicStat(stringResource(Res.string.label_class_rank), summary.classRank, Modifier.weight(1f))
        AcademicStat(stringResource(Res.string.label_major_rank), summary.majorRank, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AcademicStat(stringResource(Res.string.label_pass_rate), summary.passRateDisplay, Modifier.weight(1f))
        AcademicStat(stringResource(Res.string.label_weighted_average_score), summary.weightedAverageScore, Modifier.weight(1f))
    }
}

@Composable
private fun AcademicMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(value.ifBlank { "--" }, style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.SemiBold)
        Text(label, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

@Composable
private fun AcademicStat(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Text(value.ifBlank { "--" }, style = MiuixTheme.textStyles.body1, fontWeight = FontWeight.SemiBold)
    }
}
