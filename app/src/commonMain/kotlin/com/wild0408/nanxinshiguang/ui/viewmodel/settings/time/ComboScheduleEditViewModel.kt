package com.wild0408.nanxinshiguang.ui.viewmodel.settings.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.db.main.CourseTimeBinding.TargetType
import com.wild0408.nanxinshiguang.data.db.main.TimeTable
import com.wild0408.nanxinshiguang.data.db.main.TimeTableCombo
import com.wild0408.nanxinshiguang.data.db.main.TimeTableComboRule
import com.wild0408.nanxinshiguang.data.repository.TimeScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.wild0408.nanxinshiguang.tool.AppLog

private const val TAG = "ComboScheduleEditViewModel"

data class ComboScheduleEditUiState(
    val name: String = "",
    val baseTimeTableId: String? = null,
    val rules: List<TimeTableComboRule> = emptyList(),
    val availablePublicTables: List<TimeTable> = emptyList(),
    val isDataLoaded: Boolean = false,
    val isSaved: Boolean = false,
    val errorMsg: String? = null
)

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class ComboScheduleEditViewModel(
    @InjectedParam private val rawComboId: String?,
    @InjectedParam private val rawCopyFromId: String? = null,
    private val timeScheduleRepository: TimeScheduleRepository
) : ViewModel() {

    private val comboId: String? = rawComboId?.takeIf { it.isNotBlank() && it != "null" }
    private val copyFromId: String? = rawCopyFromId?.takeIf { it.isNotBlank() && it != "null" }

    private val currentComboId: String = if (copyFromId != null) Uuid.random().toString() else (comboId ?: Uuid.random().toString())

    private val _uiState = MutableStateFlow(ComboScheduleEditUiState())
    val uiState: StateFlow<ComboScheduleEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val publicTables = timeScheduleRepository.getAllPublicTimeTables().first()

                val sourceComboId = copyFromId ?: comboId

                if (sourceComboId != null) {
                    val combos = timeScheduleRepository.getAllCombos().first()
                    val sourceCombo = combos.find { it.id == sourceComboId }
                    val dbRules = timeScheduleRepository.getComboRules(sourceComboId).first()

                    val rulesToPreFill = dbRules.map { rule ->
                        rule.copy(
                            id = if (copyFromId != null) Uuid.random().toString() else rule.id,
                            comboId = currentComboId
                        )
                    }

                    val baseName = sourceCombo?.name.orEmpty()
                    val initialName = if (copyFromId != null) {
                        if (baseName.isNotBlank()) "$baseName.1" else ""
                    } else {
                        baseName
                    }

                    _uiState.update {
                        it.copy(
                            name = initialName,
                            baseTimeTableId = sourceCombo?.baseTimeTableId,
                            rules = rulesToPreFill,
                            availablePublicTables = publicTables,
                            isDataLoaded = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            baseTimeTableId = null,
                            availablePublicTables = publicTables,
                            isDataLoaded = true
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMsg = e.message, isDataLoaded = true) }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onBaseTableChange(baseId: String?) {
        _uiState.update { it.copy(baseTimeTableId = baseId) }
    }

    fun addRule() {
        val defaultTable = uiState.value.availablePublicTables.firstOrNull() ?: return
        val currentDateStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        _uiState.update { state ->
            val newRule = TimeTableComboRule(
                id = Uuid.random().toString(),
                comboId = currentComboId,
                targetTimeTableId = defaultTable.id,
                startDate = currentDateStr,
                endDate = currentDateStr
            )
            state.copy(rules = state.rules + newRule)
        }
    }

    fun updateRule(index: Int, updatedRule: TimeTableComboRule) {
        _uiState.update { state ->
            val list = state.rules.toMutableList()
            if (index in list.indices) {
                list[index] = updatedRule
            }
            state.copy(rules = list)
        }
    }

    fun removeRule(index: Int) {
        _uiState.update { state ->
            val list = state.rules.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            state.copy(rules = list)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun save(bindCourseTableId: String? = null) {
        val state = uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMsg = "请输入组合名称") }
            return
        }
        if (state.baseTimeTableId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMsg = "请选择基准作息") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val combo = TimeTableCombo(
                    id = currentComboId,
                    name = state.name.trim(),
                    baseTimeTableId = state.baseTimeTableId,
                    createdAt = Clock.System.now().toEpochMilliseconds()
                )

                timeScheduleRepository.saveTimeTableCombo(combo, state.rules)

                if (!bindCourseTableId.isNullOrBlank()) {
                    timeScheduleRepository.bindCourseTableToTimeSchedule(
                        courseTableId = bindCourseTableId,
                        targetType = TargetType.COMBO,
                        targetId = currentComboId
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaved = true) }
            }.onFailure { e ->
                AppLog.e(TAG, "保存组合作息方案失败", e)
                _uiState.update { it.copy(errorMsg = e.message ?: "保存失败") }
            }
        }
    }
}
