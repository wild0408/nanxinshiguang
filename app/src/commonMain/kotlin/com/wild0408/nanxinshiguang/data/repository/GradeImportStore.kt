package com.wild0408.nanxinshiguang.data.repository

import com.wild0408.nanxinshiguang.data.model.GradeRecord
import com.wild0408.nanxinshiguang.data.model.latestGradeSemester
import com.wild0408.nanxinshiguang.data.model.orderedGradeSemesters
import com.wild0408.nanxinshiguang.data.model.summarizeGradeRecords
import com.wild0408.nanxinshiguang.ui.service.GradeCenterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 临时成绩导入状态；后续由 GradeRepository 持久化替换。 */
object GradeImportStore {
    private val _state = MutableStateFlow<GradeCenterUiState?>(null)
    val state: StateFlow<GradeCenterUiState?> = _state.asStateFlow()

    fun replace(records: List<GradeRecord>) {
        val semesters = orderedGradeSemesters(records.map { it.semester })
        val summaries = semesters.associateWith { semester -> summarizeGradeRecords(records, semester) }
        _state.value = GradeCenterUiState(
            semesters = semesters,
            selectedSemester = latestGradeSemester(semesters),
            summary = semesters.firstOrNull()?.let { summaries[it] },
            records = records,
            semesterSummaries = summaries
        )
    }

}
