package com.wild0408.nanxinshiguang.ui.viewmodel.settings.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.db.main.CourseTimeBinding.TargetType
import com.wild0408.nanxinshiguang.data.db.main.TimeSlot
import com.wild0408.nanxinshiguang.data.db.main.TimeTable
import com.wild0408.nanxinshiguang.data.repository.AppSettingsRepository
import com.wild0408.nanxinshiguang.data.repository.TimeScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class SingleScheduleEditUiState(
    val isPublic: Boolean = false,
    val name: String = "",
    val slots: List<TimeSlot> = emptyList(),
    val defaultClassDuration: Int = 45,
    val defaultBreakDuration: Int = 10,
    val isDataLoaded: Boolean = false,
    val isSaved: Boolean = false,
    val errorMsg: String? = null
)

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class SingleScheduleEditViewModel(
    @InjectedParam private val rawTableId: String?,
    @InjectedParam val isPublic: Boolean,
    @InjectedParam private val rawCopyFromId: String? = null,
    private val appSettingsRepository: AppSettingsRepository,
    private val timeScheduleRepository: TimeScheduleRepository
) : ViewModel() {

    private val parsedTableId: String? = rawTableId?.takeIf { it.isNotBlank() && it != "null" }
    private val copyFromId: String? = rawCopyFromId?.takeIf { it.isNotBlank() && it != "null" }

    private val _uiState = MutableStateFlow(SingleScheduleEditUiState(isPublic = isPublic))
    val uiState: StateFlow<SingleScheduleEditUiState> = _uiState.asStateFlow()

    private var currentTargetId: String = ""

    init {
        viewModelScope.launch {
            runCatching {
                val currentCourseTableId = appSettingsRepository.getAppSettings().first().currentCourseTableId

                currentTargetId = if (isPublic) {
                    if (copyFromId != null) {
                        Uuid.random().toString()
                    } else parsedTableId ?: Uuid.random().toString()
                } else {
                    parsedTableId ?: currentCourseTableId
                }

                check(currentTargetId.isNotBlank()) { "目标作息表ID不能为空" }

                val sourceTimeTableId = copyFromId ?: parsedTableId ?: currentCourseTableId

                val rawSlots = timeScheduleRepository.getTimeSlotsByTimeTableId(sourceTimeTableId).first()
                val slots = rawSlots.map { it.copy(timeTableId = currentTargetId) }

                val timeTable = timeScheduleRepository.getTimeTableById(sourceTimeTableId).first()
                val classDuration = timeTable?.defaultClassDuration ?: 45
                val breakDuration = timeTable?.defaultBreakDuration ?: 10

                val tableName = if (copyFromId != null) {
                    val baseName = timeTable?.name.orEmpty()
                    if (baseName.isNotBlank()) "$baseName.1" else ""
                } else {
                    timeTable?.name.orEmpty()
                }

                _uiState.update {
                    it.copy(
                        name = tableName,
                        slots = slots,
                        defaultClassDuration = classDuration,
                        defaultBreakDuration = breakDuration,
                        isDataLoaded = true
                    )
                }
            }.onFailure { e ->
                e.printStackTrace()
                _uiState.update { it.copy(errorMsg = e.message, isDataLoaded = true) }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onDefaultDurationChange(classDuration: Int, breakDuration: Int) {
        _uiState.update {
            it.copy(
                defaultClassDuration = classDuration,
                defaultBreakDuration = breakDuration
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun saveWithData(
        newName: String,
        classDuration: Int,
        breakDuration: Int,
        slots: List<TimeSlot>
    ) {
        viewModelScope.launch {
            val currentCourseTableId = appSettingsRepository.getAppSettings().first().currentCourseTableId
            val slotsToSave = slots.map { it.copy(timeTableId = currentTargetId) }

            val timeTable = TimeTable(
                id = currentTargetId,
                name = if (isPublic) newName.trim().ifBlank { null } else null,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                defaultClassDuration = classDuration,
                defaultBreakDuration = breakDuration
            )

            runCatching {
                if (isPublic) {
                    timeScheduleRepository.savePublicTimeTable(timeTable, slotsToSave)
                } else {
                    timeScheduleRepository.saveExclusiveTimeTable(timeTable, slotsToSave)

                    timeScheduleRepository.bindCourseTableToTimeSchedule(
                        courseTableId = currentCourseTableId,
                        targetType = TargetType.SINGLE,
                        targetId = currentCourseTableId
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaved = true) }
            }.onFailure { e ->
                e.printStackTrace()
                _uiState.update { it.copy(errorMsg = e.message ?: "保存失败，请重试") }
            }
        }
    }
}
