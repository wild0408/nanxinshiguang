package com.wild0408.nanxinshiguang.ui.miuix.service.grade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.GradeRecord
import com.wild0408.nanxinshiguang.data.model.GradeSemesterSummary
import com.wild0408.nanxinshiguang.data.model.ALL_GRADE_SEMESTERS
import com.wild0408.nanxinshiguang.data.model.latestGradeSemester
import com.wild0408.nanxinshiguang.data.model.orderedGradeSemesters
import com.wild0408.nanxinshiguang.data.model.summarizeAllGradeRecords
import com.wild0408.nanxinshiguang.data.model.summarizeGradeRecords
import com.wild0408.nanxinshiguang.data.model.isPassed
import com.wild0408.nanxinshiguang.data.repository.GradeImportStore
import com.wild0408.nanxinshiguang.data.repository.GradeRepository
import com.wild0408.nanxinshiguang.ui.material.service.grade.GradeCenterUiState
import com.wild0408.nanxinshiguang.ui.service.AcademicSummaryViewModel
import com.wild0408.nanxinshiguang.ui.service.GradeSyncUiState
import com.wild0408.nanxinshiguang.ui.service.GradeSyncViewModel
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.SharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_import_grade
import nanxinshiguang.generated.resources.action_refresh_grades
import nanxinshiguang.generated.resources.action_view_grade_details
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.check_24px
import nanxinshiguang.generated.resources.desc_grade_empty
import nanxinshiguang.generated.resources.label_course_count
import nanxinshiguang.generated.resources.label_credits
import nanxinshiguang.generated.resources.label_current_semester
import nanxinshiguang.generated.resources.label_all_semesters
import nanxinshiguang.generated.resources.label_exam_type
import nanxinshiguang.generated.resources.label_gpa
import nanxinshiguang.generated.resources.label_grade_average_score
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
import nanxinshiguang.generated.resources.status_grade_service_unavailable
import nanxinshiguang.generated.resources.status_grade_loading
import nanxinshiguang.generated.resources.status_grade_no_data
import nanxinshiguang.generated.resources.refresh_24px
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixGradeCenterScreen(
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
    // The database is the source of truth across process restarts; the store only
    // provides an immediate in-process update while Room emits its new snapshot.
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
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = stringResource(
                    if (details) Res.string.title_grade_details else Res.string.title_grade_center
                ),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(Res.string.a11y_back),
                        backdropAlpha = a,
                        shadowAlpha = s,
                    )
                },
                endAction = if (details) {
                    { a, s ->
                        if (gradeSyncState.loading) {
                            top.yukonga.miuix.kmp.basic.CircularProgressIndicator(Modifier.size(22.dp))
                        } else {
                            HyperLiquidTopBarButton(
                                onClick = gradeSyncViewModel::refresh,
                                backdrop = backdrop,
                                icon = vectorResource(Res.drawable.refresh_24px),
                                contentDescription = stringResource(Res.string.action_refresh_grades),
                                backdropAlpha = a,
                                shadowAlpha = s,
                            )
                        }
                    }
                } else null,
            )
        },
    ) { padding ->
        val contentPadding = PaddingValues(
            start = padding.calculateLeftPadding(direction) + 20.dp,
            top = padding.calculateTopPadding() + 12.dp,
            end = padding.calculateRightPadding(direction) + 20.dp,
            bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
        )
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            when {
                effectiveUiState.isLoading -> LoadingContent(Modifier.padding(contentPadding))
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
                    contentPadding = contentPadding,
                    scrollBehavior = scrollBehavior,
                )
            }
        }
    }
}

@Composable
private fun GradeContent(
    uiState: GradeCenterUiState,
    academicState: com.wild0408.nanxinshiguang.ui.service.AcademicSummaryUiState,
    onRefreshAcademic: () -> Unit,
    onBindPortal: () -> Unit,
    gradeSyncState: GradeSyncUiState,
    onRefreshGrades: () -> Unit,
    showDetails: Boolean,
    onOpenDetails: () -> Unit,
    semesterMenuExpanded: Boolean,
    onSemesterMenuChange: (Boolean) -> Unit,
    onSemesterSelected: (String) -> Unit,
    contentPadding: PaddingValues,
    scrollBehavior: SharedScrollBehavior,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = contentPadding.calculateLeftPadding(LocalLayoutDirection.current),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateRightPadding(LocalLayoutDirection.current),
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!showDetails) {
            item {
                MiuixAcademicSummaryCard(
                    state = academicState,
                    onRefresh = onRefreshAcademic,
                    onBind = onBindPortal,
                )
            }
        }
        if (!showDetails) {
            item {
                MiuixGradeOverviewCard(
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
private fun MiuixGradeOverviewCard(
    uiState: GradeCenterUiState,
    syncState: GradeSyncUiState,
    onRefresh: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    top.yukonga.miuix.kmp.basic.Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenDetails,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        ),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    top.yukonga.miuix.kmp.basic.Text(
                        stringResource(Res.string.title_grade_details),
                        style = MiuixTheme.textStyles.title3,
                    )
                    top.yukonga.miuix.kmp.basic.Text(
                        when {
                            uiState.records.isNotEmpty() -> stringResource(Res.string.action_view_grade_details)
                            syncState.loading -> stringResource(Res.string.status_grade_loading)
                            syncState.errorMessage != null -> syncState.errorMessage
                            else -> stringResource(Res.string.status_grade_no_data)
                        },
                        style = MiuixTheme.textStyles.body2,
                        color = if (syncState.errorMessage == null) {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        } else {
                            MiuixTheme.colorScheme.error
                        },
                    )
                }
                if (syncState.loading) {
                    top.yukonga.miuix.kmp.basic.CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    top.yukonga.miuix.kmp.basic.IconButton(onClick = onRefresh) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            vectorResource(Res.drawable.refresh_24px),
                            stringResource(Res.string.action_refresh_grades),
                            tint = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
            }
            if (uiState.records.isNotEmpty()) {
                val semester = latestGradeSemester(uiState.semesters)
                    ?: uiState.selectedSemester
                val summary = semester?.let { uiState.forSemester(it).summary }
                    ?: uiState.summary
                top.yukonga.miuix.kmp.basic.Text(
                    summary?.semester ?: stringResource(Res.string.status_no_data),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiuixOverviewMetric(stringResource(Res.string.label_grade_average_score), summary?.averageScore ?: "--", Modifier.weight(1f))
                    MiuixOverviewMetric(stringResource(Res.string.label_credits), summary?.totalCredits ?: "--", Modifier.weight(1f))
                    MiuixOverviewMetric(stringResource(Res.string.label_course_count), summary?.courseCount?.toString() ?: "--", Modifier.weight(1f))
                    MiuixOverviewMetric(stringResource(Res.string.label_passed_count), summary?.passedCount?.toString() ?: "--", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiuixOverviewMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        top.yukonga.miuix.kmp.basic.Text(value, style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.SemiBold)
        top.yukonga.miuix.kmp.basic.Text(label, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
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
            top.yukonga.miuix.kmp.basic.Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (semesters.isNotEmpty()) onExpandedChange(true) },
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainer)
            ) {
                top.yukonga.miuix.kmp.basic.BasicComponent(
                    title = stringResource(Res.string.label_current_semester),
                    summary = when (selectedSemester) { ALL_SEMESTERS -> stringResource(Res.string.label_all_semesters); else -> selectedSemester ?: stringResource(Res.string.status_no_data) },
                    onClick = { if (semesters.isNotEmpty()) onExpandedChange(true) },
                    endActions = { top.yukonga.miuix.kmp.basic.Icon(vectorResource(Res.drawable.chevron_right_24px), null, tint = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantActions) }
                )
            }
            if (expanded) {
                top.yukonga.miuix.kmp.window.WindowDialog(show = true, title = stringResource(Res.string.label_current_semester), onDismissRequest = { onExpandedChange(false) }, insideMargin = androidx.compose.ui.unit.DpSize(16.dp, 16.dp)) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val options = listOf(ALL_SEMESTERS) + semesters
                        options.forEach { semester ->
                            top.yukonga.miuix.kmp.basic.BasicComponent(
                                title = if (semester == ALL_SEMESTERS) stringResource(Res.string.label_all_semesters) else semester,
                                onClick = { onSemesterSelected(semester); onExpandedChange(false) },
                                endActions = { if (semester == selectedSemester) top.yukonga.miuix.kmp.basic.Icon(vectorResource(Res.drawable.check_24px), null, tint = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary) }
                            )
                        }
                    }
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
    top.yukonga.miuix.kmp.basic.Card(
        modifier = modifier,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            top.yukonga.miuix.kmp.basic.Text(label, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote1, color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary)
            top.yukonga.miuix.kmp.basic.Text(value, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.title2)
        }
    }
}

@Composable
private fun EmptyGradeCard(
    syncState: GradeSyncUiState,
    onRefresh: () -> Unit,
) {
    top.yukonga.miuix.kmp.basic.Card(
            modifier = Modifier.fillMaxWidth(),
            colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                top.yukonga.miuix.kmp.basic.Icon(vectorResource(Res.drawable.list_alt_24px), null, modifier = Modifier.size(48.dp), tint = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary)
                top.yukonga.miuix.kmp.basic.Text(stringResource(Res.string.title_grade_empty), style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.title3)
                top.yukonga.miuix.kmp.basic.Text(
                    when {
                        syncState.loading -> stringResource(Res.string.status_grade_loading)
                        syncState.errorMessage != null -> syncState.errorMessage
                        else -> stringResource(Res.string.desc_grade_empty)
                    },
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body2,
                    color = if (syncState.errorMessage == null) {
                        top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary
                    } else {
                        top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.error
                    },
                )
                top.yukonga.miuix.kmp.basic.Button(
                    onClick = onRefresh,
                    enabled = !syncState.loading,
                ) {
                    top.yukonga.miuix.kmp.basic.Text(stringResource(Res.string.action_import_grade))
                }
            }
    }
}

@Composable
private fun GradeRecordCard(record: GradeRecord) {
    var expanded by remember(record.id) { mutableStateOf(false) }
    top.yukonga.miuix.kmp.basic.Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = !expanded },
            colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        top.yukonga.miuix.kmp.basic.Text(record.courseName, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body1, fontWeight = FontWeight.SemiBold)
                        top.yukonga.miuix.kmp.basic.Text(record.courseType, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body2, color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                    top.yukonga.miuix.kmp.basic.Text(record.score, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.title2, color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary)
                }
                top.yukonga.miuix.kmp.basic.Text("${stringResource(Res.string.label_credits)} ${record.credits}  ·  ${stringResource(Res.string.label_grade_point)} ${record.gradePoint}", style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body2)
                record.status?.let { top.yukonga.miuix.kmp.basic.Text(it, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote1, color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.secondary) }
                if (expanded) {
                    DetailLineMiuix(stringResource(Res.string.label_teacher), record.teacher)
                    DetailLineMiuix(stringResource(Res.string.label_exam_type), record.examType)
                    DetailLineMiuix(stringResource(Res.string.label_score_composition), record.scoreComposition)
                    DetailLineMiuix(stringResource(Res.string.label_score), record.score)
                }
            }
    }
}

@Composable
private fun DetailLineMiuix(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(Modifier.fillMaxWidth()) {
            top.yukonga.miuix.kmp.basic.Text(label, Modifier.weight(1f), color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary)
            top.yukonga.miuix.kmp.basic.Text(value)
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        top.yukonga.miuix.kmp.basic.CircularProgressIndicator()
    }
}
