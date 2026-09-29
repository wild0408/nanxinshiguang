package com.wild0408.nanxinshiguang.ui.service

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.wild0408.nanxinshiguang.data.model.GradeRecord
import com.wild0408.nanxinshiguang.data.model.GradeSemesterSummary
import com.wild0408.nanxinshiguang.data.model.ALL_GRADE_SEMESTERS
import com.wild0408.nanxinshiguang.data.model.latestGradeSemester
import com.wild0408.nanxinshiguang.data.model.orderedGradeSemesters
import com.wild0408.nanxinshiguang.data.model.summarizeAllGradeRecords
import com.wild0408.nanxinshiguang.data.model.summarizeGradeRecords
import com.wild0408.nanxinshiguang.data.model.isPassed
import com.wild0408.nanxinshiguang.data.model.AcademicSummary
import com.wild0408.nanxinshiguang.data.model.updatedAtText
import com.wild0408.nanxinshiguang.data.repository.GradeImportStore
import com.wild0408.nanxinshiguang.data.repository.GradeRepository
import com.wild0408.nanxinshiguang.ui.components.AdaptiveNavigationScaffold
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_import_grade
import nanxinshiguang.generated.resources.action_refresh_grades
import nanxinshiguang.generated.resources.action_view_grade_details
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.desc_grade_empty
import nanxinshiguang.generated.resources.label_course_count
import nanxinshiguang.generated.resources.label_credits
import nanxinshiguang.generated.resources.label_current_semester
import nanxinshiguang.generated.resources.label_all_semesters
import nanxinshiguang.generated.resources.label_exam_type
import nanxinshiguang.generated.resources.label_gpa
import nanxinshiguang.generated.resources.label_grade_point
import nanxinshiguang.generated.resources.label_passed_count
import nanxinshiguang.generated.resources.label_score
import nanxinshiguang.generated.resources.label_score_composition
import nanxinshiguang.generated.resources.label_teacher
import nanxinshiguang.generated.resources.list_alt_24px
import nanxinshiguang.generated.resources.status_no_data
import nanxinshiguang.generated.resources.title_grade_center
import nanxinshiguang.generated.resources.title_grade_details
import nanxinshiguang.generated.resources.title_grade_empty
import nanxinshiguang.generated.resources.title_academic_summary
import nanxinshiguang.generated.resources.action_refresh_academic_summary
import nanxinshiguang.generated.resources.status_academic_summary_loading
import nanxinshiguang.generated.resources.status_academic_summary_no_data
import nanxinshiguang.generated.resources.status_academic_summary_not_bound
import nanxinshiguang.generated.resources.status_academic_summary_updated
import nanxinshiguang.generated.resources.status_grade_service_unavailable
import nanxinshiguang.generated.resources.status_grade_loading
import nanxinshiguang.generated.resources.status_grade_no_data
import nanxinshiguang.generated.resources.status_grade_fetch_failed
import nanxinshiguang.generated.resources.label_average_grade_point
import nanxinshiguang.generated.resources.label_average_score
import nanxinshiguang.generated.resources.label_grade_average_score
import nanxinshiguang.generated.resources.label_credit_progress
import nanxinshiguang.generated.resources.label_earned_required_credits
import nanxinshiguang.generated.resources.label_class_rank
import nanxinshiguang.generated.resources.label_major_rank
import nanxinshiguang.generated.resources.label_pass_rate
import nanxinshiguang.generated.resources.label_weighted_average_score
import nanxinshiguang.generated.resources.portal_bind_action
import nanxinshiguang.generated.resources.refresh_24px

data class GradeCenterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val semesters: List<String> = emptyList(),
    val selectedSemester: String? = null,
    val summary: GradeSemesterSummary? = null,
    val records: List<GradeRecord> = emptyList(),
    val semesterSummaries: Map<String, GradeSemesterSummary> = emptyMap()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCenterScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    uiState: GradeCenterUiState? = null,
    details: Boolean = false,
) {
    val academicViewModel: AcademicSummaryViewModel = koinViewModel()
    val academicState by academicViewModel.uiState.collectAsState()
    val gradeSyncViewModel: GradeSyncViewModel = koinViewModel()
    val gradeSyncState by gradeSyncViewModel.uiState.collectAsState()
    val importedState by GradeImportStore.state.collectAsState()
    val gradeRepository: GradeRepository = koinInject()
    val savedRecords by gradeRepository.observeAll().collectAsState(initial = emptyList())
    val savedState = if (savedRecords.isEmpty()) null else GradeCenterUiState(
        semesters = orderedGradeSemesters(savedRecords.map { it.semester }),
        selectedSemester = latestGradeSemester(savedRecords.map { it.semester }),
        records = savedRecords,
        semesterSummaries = orderedGradeSemesters(savedRecords.map { it.semester }).associateWith { semester ->
            summarizeGradeRecords(savedRecords, semester)
        }
    )
    val effectiveUiState = savedState ?: importedState ?: uiState ?: GradeCenterUiState()
    var semesterMenuExpanded by remember { mutableStateOf(false) }
    var selectedSemester by remember {
        mutableStateOf(latestGradeSemester(effectiveUiState.semesters) ?: effectiveUiState.selectedSemester)
    }
    LaunchedEffect(effectiveUiState.semesters, effectiveUiState.selectedSemester) {
        if (selectedSemester == null || selectedSemester !in effectiveUiState.semesters && selectedSemester != ALL_SEMESTERS) {
            selectedSemester = latestGradeSemester(effectiveUiState.semesters) ?: effectiveUiState.selectedSemester
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    AdaptiveNavigationScaffold(
        currentDestination = Destination.GradeCenter,
        onTabSelected = onNavigate,
        showNavigation = false
    ) { navigationPadding ->
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(
                                if (details) Res.string.title_grade_details
                                else Res.string.title_grade_center,
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.arrow_back_24px),
                                contentDescription = stringResource(Res.string.a11y_back)
                            )
                        }
                    },
                    actions = {
                        if (details) {
                            IconButton(
                                onClick = gradeSyncViewModel::refresh,
                                enabled = !gradeSyncState.loading,
                            ) {
                                if (gradeSyncState.loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.refresh_24px),
                                        contentDescription = stringResource(Res.string.action_refresh_grades),
                                    )
                                }
                            }
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { contentPadding ->
            when {
                effectiveUiState.isLoading -> LoadingContent(
                    Modifier.padding(navigationPadding).padding(contentPadding)
                )
                else -> GradeContent(
                    uiState = if (details) effectiveUiState.forSemester(selectedSemester) else effectiveUiState,
                    academicState = academicState,
                    onRefreshAcademic = academicViewModel::refresh,
                    onBindPortal = { onNavigate(Destination.PortalAccount) },
                    gradeSyncState = gradeSyncState,
                    onRefreshGrades = gradeSyncViewModel::refresh,
                    showDetails = details,
                    onOpenDetails = { onNavigate(Destination.GradeDetails) },
                    semesterMenuExpanded = semesterMenuExpanded,
                    onSemesterMenuChange = { semesterMenuExpanded = it },
                    onSemesterSelected = { selectedSemester = it },
                    modifier = Modifier.padding(navigationPadding).padding(contentPadding)
                )
            }
        }
    }
}

@Composable
private fun GradeContent(
    uiState: GradeCenterUiState,
    academicState: AcademicSummaryUiState,
    onRefreshAcademic: () -> Unit,
    onBindPortal: () -> Unit,
    gradeSyncState: GradeSyncUiState,
    onRefreshGrades: () -> Unit,
    showDetails: Boolean,
    onOpenDetails: () -> Unit,
    semesterMenuExpanded: Boolean,
    onSemesterMenuChange: (Boolean) -> Unit,
    onSemesterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!showDetails) {
            item {
                AcademicSummaryCard(
                    state = academicState,
                    onRefresh = onRefreshAcademic,
                    onBind = onBindPortal,
                )
            }
        }
        if (!showDetails) {
            item {
                GradeOverviewCard(
                    uiState = uiState,
                    syncState = gradeSyncState,
                    onRefresh = onRefreshGrades,
                    onOpenDetails = onOpenDetails,
                )
            }
        } else {
            if (uiState.records.isEmpty()) {
                item {
                    EmptyGradeCard(
                        syncState = gradeSyncState,
                        onRefresh = onRefreshGrades,
                    )
                }
            } else {
                item {
                    SemesterSelector(
                        semesters = uiState.semesters,
                        selectedSemester = uiState.selectedSemester,
                        expanded = semesterMenuExpanded,
                        onExpandedChange = onSemesterMenuChange,
                        onSemesterSelected = onSemesterSelected,
                    )
                }
                item { SummaryGrid(uiState.summary) }
                items(uiState.records, key = { it.id }) { record -> GradeRecordCard(record) }
            }
        }
    }
}

@Composable
private fun GradeOverviewCard(
    uiState: GradeCenterUiState,
    syncState: GradeSyncUiState,
    onRefresh: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    Card(
        onClick = onOpenDetails,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.title_grade_details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (uiState.records.isEmpty()) {
                            when {
                                syncState.loading -> stringResource(Res.string.status_grade_loading)
                                syncState.errorMessage != null -> syncState.errorMessage
                                else -> stringResource(Res.string.status_grade_no_data)
                            }
                        } else {
                            stringResource(Res.string.action_view_grade_details)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (syncState.errorMessage == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
                if (syncState.loading) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.refresh_24px),
                            contentDescription = stringResource(Res.string.action_refresh_grades),
                        )
                    }
                }
            }
            if (uiState.records.isNotEmpty()) {
                val semester = latestGradeSemester(uiState.semesters)
                    ?: uiState.selectedSemester
                val summary = semester?.let { uiState.forSemester(it).summary }
                    ?: uiState.summary
                Text(
                    summary?.semester ?: stringResource(Res.string.status_no_data),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewMetric(stringResource(Res.string.label_grade_average_score), summary?.averageScore ?: "--", Modifier.weight(1f))
                    OverviewMetric(stringResource(Res.string.label_credits), summary?.totalCredits ?: "--", Modifier.weight(1f))
                    OverviewMetric(stringResource(Res.string.label_course_count), summary?.courseCount?.toString() ?: "--", Modifier.weight(1f))
                    OverviewMetric(stringResource(Res.string.label_passed_count), summary?.passedCount?.toString() ?: "--", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AcademicSummaryCard(
    state: AcademicSummaryUiState,
    onRefresh: () -> Unit,
    onBind: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.title_academic_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (state.portalBound) IconButton(onClick = onRefresh, enabled = !state.loading) {
                    if (state.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = vectorResource(Res.drawable.refresh_24px),
                            contentDescription = stringResource(Res.string.action_refresh_academic_summary),
                        )
                    }
                }
            }
            when {
                state.loading && !state.portalBound -> {
                    Text(
                        stringResource(Res.string.status_academic_summary_loading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                !state.portalBound -> {
                    Text(
                        stringResource(Res.string.status_academic_summary_not_bound),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onBind) { Text(stringResource(Res.string.portal_bind_action)) }
                }
                state.summary == null && state.loading -> {
                    Text(
                        stringResource(Res.string.status_academic_summary_loading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                state.summary == null -> {
                    Text(
                        state.errorMessage ?: stringResource(Res.string.status_academic_summary_no_data),
                        color = if (state.errorMessage == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
                else -> {
                    Text(
                        stringResource(Res.string.status_academic_summary_updated, state.summary.updatedAtText()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AcademicSummaryContent(state.summary)
                    state.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun AcademicSummaryContent(summary: AcademicSummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AcademicMetric(stringResource(Res.string.label_average_grade_point), summary.averageGradePoint, Modifier.weight(1f))
        AcademicMetric(stringResource(Res.string.label_gpa), summary.gpa, Modifier.weight(1f))
        AcademicMetric(stringResource(Res.string.label_average_score), summary.averageScore, Modifier.weight(1f))
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(Res.string.label_credit_progress),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(
                Res.string.label_earned_required_credits,
                summary.earnedCredits.ifBlank { "--" },
                summary.requiredCredits.ifBlank { "--" },
            ) + "  ${summary.creditProgress.ifBlank { "--" }}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    LinearProgressIndicator(
        progress = { summary.creditProgressFraction ?: 0f },
        modifier = Modifier.fillMaxWidth().height(6.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
    )
    HorizontalDivider()
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
private fun AcademicMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            value.ifBlank { "--" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AcademicStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "--" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SemesterSelector(
    semesters: List<String>,
    selectedSemester: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSemesterSelected: (String) -> Unit
) {
    Box {
        Card(
            onClick = { if (semesters.isNotEmpty()) onExpandedChange(true) },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.label_current_semester),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        when (selectedSemester) {
                            ALL_SEMESTERS -> stringResource(Res.string.label_all_semesters)
                            else -> selectedSemester ?: stringResource(Res.string.status_no_data)
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Icon(
                    imageVector = vectorResource(Res.drawable.chevron_right_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.label_all_semesters)) },
                onClick = {
                    onSemesterSelected(ALL_SEMESTERS)
                    onExpandedChange(false)
                }
            )
            semesters.forEach { semester ->
                DropdownMenuItem(
                    text = { Text(semester) },
                    onClick = {
                        onSemesterSelected(semester)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

private fun GradeCenterUiState.forSemester(semester: String?): GradeCenterUiState {
    if (semester.isNullOrBlank()) return this
    if (semester == ALL_SEMESTERS) {
        return copy(selectedSemester = semester, summary = summarizeAll(), records = records)
    }
    return copy(
        selectedSemester = semester,
        summary = semesterSummaries[semester] ?: summarizeGradeRecords(records, semester),
        records = records.filter { it.semester == semester }
    )
}

private const val ALL_SEMESTERS = ALL_GRADE_SEMESTERS

private fun GradeCenterUiState.summarizeAll(): GradeSemesterSummary? {
    return summarizeAllGradeRecords(records)
}

@Composable
private fun SummaryGrid(summary: GradeSemesterSummary?) {
    BoxWithConstraints {
        val values = listOf(
            stringResource(Res.string.label_grade_average_score) to (summary?.averageScore ?: "--"),
            stringResource(Res.string.label_credits) to (summary?.totalCredits ?: "--"),
            stringResource(Res.string.label_course_count) to (summary?.courseCount?.toString() ?: "--"),
            stringResource(Res.string.label_passed_count) to (summary?.passedCount?.toString() ?: "--")
        )
        if (maxWidth >= 840.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                values.forEach { (label, value) -> SummaryItem(label, value, Modifier.weight(1f)) }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                values.chunked(2).forEach { rowValues ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowValues.forEach { (label, value) -> SummaryItem(label, value, Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun EmptyGradeCard(
    syncState: GradeSyncUiState,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.list_alt_24px),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(Res.string.title_grade_empty), style = MaterialTheme.typography.titleMedium)
            Text(
                when {
                    syncState.loading -> stringResource(Res.string.status_grade_loading)
                    syncState.errorMessage != null -> syncState.errorMessage
                    else -> stringResource(Res.string.desc_grade_empty)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (syncState.errorMessage == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            TextButton(onClick = onRefresh, enabled = !syncState.loading) {
                Text(stringResource(Res.string.action_import_grade))
            }
        }
    }
}

@Composable
private fun GradeRecordCard(record: GradeRecord) {
    var expanded by remember(record.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(record.courseType, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(record.score, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text("${stringResource(Res.string.label_credits)} ${record.credits}  ·  ${stringResource(Res.string.label_grade_point)} ${record.gradePoint}")
            record.status?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary) }
            if (expanded) {
                HorizontalDivider()
                DetailLine(stringResource(Res.string.label_teacher), record.teacher)
                DetailLine(stringResource(Res.string.label_exam_type), record.examType)
                DetailLine(stringResource(Res.string.label_score_composition), record.scoreComposition)
                DetailLine(stringResource(Res.string.label_score), record.score)
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value)
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
