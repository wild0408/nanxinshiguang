package com.wild0408.nanxinshiguang.ui.components

import androidx.compose.runtime.Composable
import com.wild0408.nanxinshiguang.data.model.LaborOfficialResult
import com.wild0408.nanxinshiguang.data.model.LaborScore
import com.wild0408.nanxinshiguang.data.repository.LaborScoreState
import com.wild0408.nanxinshiguang.data.repository.LaborScoreStatus
import org.jetbrains.compose.resources.stringResource
import nanxinshiguang.generated.resources.*

@Composable
fun laborOfficialFields(result: LaborOfficialResult): List<Pair<String, String?>> = listOf(
    stringResource(Res.string.labor_score_theory) to result.theory,
    stringResource(Res.string.labor_score_life) to result.life,
    stringResource(Res.string.labor_score_service) to result.service,
    stringResource(Res.string.labor_score_major_total) to result.majorTotal,
    stringResource(Res.string.labor_score_major_course) to result.majorCourse,
    stringResource(Res.string.labor_score_contest) to result.majorContest,
)

@Composable
fun laborLiveFields(score: LaborScore): List<Pair<String, String?>> = listOf(
    stringResource(Res.string.labor_score_life) to score.liveLife,
    stringResource(Res.string.labor_score_service) to score.liveService,
    stringResource(Res.string.labor_score_major_course) to score.liveMajor?.course,
    stringResource(Res.string.labor_score_contest) to score.liveMajor?.contest,
    stringResource(Res.string.labor_score_major_total) to score.liveMajor?.total,
)

@Composable
fun laborOverviewFields(score: LaborScore): List<Pair<String, String?>> =
    if (score.hasOfficialData) laborOfficialFields(requireNotNull(score.official)).take(4)
    else laborLiveFields(score)

@Composable
fun laborScoreSource(score: LaborScore): String = stringResource(
    if (score.hasOfficialData) Res.string.labor_score_official else Res.string.labor_score_live,
)

@Composable
fun laborScoreEmptyTitle(state: LaborScoreState): String = stringResource(when (state.status) {
    LaborScoreStatus.NotLoggedIn -> Res.string.labor_score_not_bound_title
    LaborScoreStatus.Empty -> Res.string.labor_score_empty_title
    LaborScoreStatus.Error -> Res.string.labor_score_error_title
    else -> Res.string.title_labor_score
})

@Composable
fun laborScoreEmptyMessage(state: LaborScoreState): String = when {
    state.loading -> stringResource(Res.string.labor_score_loading)
    state.status == LaborScoreStatus.NotLoggedIn -> state.error ?: stringResource(Res.string.labor_score_not_bound)
    state.status == LaborScoreStatus.Empty -> stringResource(Res.string.labor_score_empty_detail)
    state.status == LaborScoreStatus.Error -> state.error ?: stringResource(Res.string.labor_score_refresh_failed)
    else -> stringResource(Res.string.labor_score_query_hint)
}

@Composable
fun laborConfirmationText(value: String): String = when (value.trim()) {
    "是" -> stringResource(Res.string.labor_score_confirmed_yes)
    "否" -> stringResource(Res.string.labor_score_confirmed_no)
    else -> value.trim().ifBlank { "--" }
}

@Composable
fun laborFiledText(value: String): String = when (value.trim()) {
    "是" -> stringResource(Res.string.labor_score_filed_yes)
    "否" -> stringResource(Res.string.labor_score_filed_no)
    else -> value.trim().ifBlank { "--" }
}
