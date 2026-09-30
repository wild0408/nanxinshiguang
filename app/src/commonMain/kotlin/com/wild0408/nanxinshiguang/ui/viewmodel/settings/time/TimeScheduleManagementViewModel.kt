package com.wild0408.nanxinshiguang.ui.viewmodel.settings.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.db.main.CourseTimeBinding.TargetType
import com.wild0408.nanxinshiguang.data.repository.AppSettingsRepository
import com.wild0408.nanxinshiguang.data.repository.TimeScheduleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

// UI 数据模型定义

enum class ScheduleType {
    EXCLUSIVE, // 课表专属作息
    PUBLIC,    // 公共作息
    COMBO      // 组合作息
}

data class TimeScheduleItemUiModel(
    val id: String,
    val name: String?,
    val type: ScheduleType,
    val isSelected: Boolean,
    val isEditable: Boolean = true,
    val createdAtFormatted: String? = null
)

// 作息方案管理主页 ViewModel

data class ScheduleManagementUiState(
    val currentCourseTableId: String = "",
    val items: List<TimeScheduleItemUiModel> = emptyList(),
    val isDataLoaded: Boolean = false,
    val errorMessage: String? = null
)

private fun formatTimestamp(timestamp: Long?): String? {
    if (timestamp == null || timestamp <= 0) return null
    return runCatching {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val year = localDateTime.year
        val month = localDateTime.month.number.toString().padStart(2, '0')
        val day = localDateTime.day.toString().padStart(2, '0')
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        "$year-$month-$day $hour:$minute"
    }.getOrNull()
}

@KoinViewModel
class TimeScheduleManagementViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val timeScheduleRepository: TimeScheduleRepository
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScheduleManagementUiState> = appSettingsRepository.getAppSettings()
        .flatMapLatest { settings ->
            val tableId = settings.currentCourseTableId

            combine(
                refreshTrigger,
                timeScheduleRepository.getAllPublicTimeTables(),
                timeScheduleRepository.getAllCombos()
            ) { _, publicTables, combos ->
                val binding = runCatching {
                    timeScheduleRepository.getBinding(tableId)
                }.getOrNull()

                val items = mutableListOf<TimeScheduleItemUiModel>()

                val isExclusiveSelected = binding == null ||
                        (binding.targetType == TargetType.SINGLE && binding.targetId == tableId)

                items.add(
                    TimeScheduleItemUiModel(
                        id = tableId,
                        name = null,
                        type = ScheduleType.EXCLUSIVE,
                        isSelected = isExclusiveSelected,
                        isEditable = true,
                        createdAtFormatted = null
                    )
                )

                publicTables.forEach { publicTable ->
                    val isSelected = binding?.targetType == TargetType.SINGLE && binding.targetId == publicTable.id
                    items.add(
                        TimeScheduleItemUiModel(
                            id = publicTable.id,
                            name = publicTable.name,
                            type = ScheduleType.PUBLIC,
                            isSelected = isSelected,
                            isEditable = true,
                            createdAtFormatted = formatTimestamp(publicTable.createdAt)
                        )
                    )
                }

                combos.forEach { combo ->
                    val isSelected = binding?.targetType == TargetType.COMBO && binding.targetId == combo.id
                    items.add(
                        TimeScheduleItemUiModel(
                            id = combo.id,
                            name = combo.name,
                            type = ScheduleType.COMBO,
                            isSelected = isSelected,
                            isEditable = true,
                            createdAtFormatted = formatTimestamp(combo.createdAt)
                        )
                    )
                }

                ScheduleManagementUiState(
                    currentCourseTableId = tableId,
                    items = items,
                    isDataLoaded = true
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScheduleManagementUiState()
        )

    fun bindTimeSchedule(item: TimeScheduleItemUiModel) {
        viewModelScope.launch {
            val tableId = uiState.value.currentCourseTableId
            if (tableId.isBlank()) return@launch

            val targetType = if (item.type == ScheduleType.COMBO) TargetType.COMBO else TargetType.SINGLE
            val targetId = if (item.type == ScheduleType.EXCLUSIVE) tableId else item.id

            runCatching {
                timeScheduleRepository.bindCourseTableToTimeSchedule(tableId, targetType, targetId)
            }.onSuccess {
                refreshTrigger.value += 1
            }
        }
    }

    fun deleteSchedules(selectedKeys: Set<String>) {
        viewModelScope.launch {
            if (selectedKeys.isEmpty()) return@launch

            val currentState = uiState.value
            val itemsToDelete = currentState.items.filter { item ->
                val key = "${item.type.name}_${item.id}"
                key in selectedKeys && item.type != ScheduleType.EXCLUSIVE
            }

            runCatching {
                itemsToDelete.forEach { item ->
                    when (item.type) {
                        ScheduleType.PUBLIC -> timeScheduleRepository.deletePublicTimeTable(item.id)
                        ScheduleType.COMBO -> timeScheduleRepository.deleteCombo(item.id)
                        ScheduleType.EXCLUSIVE -> {}
                    }
                }
            }.onSuccess {
                refreshTrigger.value += 1
            }
        }
    }
}
