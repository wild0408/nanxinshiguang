package com.wild0408.nanxinshiguang.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.api.electricity.ElectricityLocation
import com.wild0408.nanxinshiguang.data.repository.ElectricityConfig
import com.wild0408.nanxinshiguang.data.repository.ElectricityPortalAuth
import com.wild0408.nanxinshiguang.data.repository.ElectricityRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Clock

data class ElectricityUiState(
    val isInitializing: Boolean = true,
    val isConfigured: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingLocations: Boolean = false,
    val studentId: String = "",
    val campuses: List<ElectricityLocation> = emptyList(),
    val buildings: List<ElectricityLocation> = emptyList(),
    val rooms: List<ElectricityLocation> = emptyList(),
    val selectedCampus: ElectricityLocation? = null,
    val selectedBuilding: ElectricityLocation? = null,
    val selectedRoom: ElectricityLocation? = null,
    val balance: Double? = null,
    val lastUpdated: Long? = null,
    val history: List<Pair<Long, Double>> = emptyList(),
    val errorMessage: String? = null,
    val editing: Boolean = false,
)

@KoinViewModel
class ElectricityViewModel(
    private val repository: ElectricityRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ElectricityUiState())
    val state: StateFlow<ElectricityUiState> = _state.asStateFlow()

    private var config: ElectricityConfig? = null
    private var sessionToken: String = ""
    private var historyJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching {
                val portalStudentId = repository.currentPortalStudentId()
                val storedConfig = repository.configFlow.first()
                    ?.takeIf { portalStudentId != null && it.studentId == portalStudentId }
                val campuses = repository.storedCampuses()
                if (storedConfig != null) {
                    config = storedConfig
                    sessionToken = storedConfig.token
                    val buildings = repository.storedBuildings(storedConfig.campus.value)
                    val rooms = repository.storedRooms("${storedConfig.campus.value}|${storedConfig.building.value}")
                    _state.value = _state.value.copy(
                        isInitializing = false,
                        isConfigured = true,
                        isLoggedIn = true,
                        isLoading = true,
                        studentId = storedConfig.studentId,
                        campuses = campuses,
                        buildings = buildings,
                        rooms = rooms,
                        selectedCampus = storedConfig.campus,
                        selectedBuilding = storedConfig.building,
                        selectedRoom = storedConfig.room,
                        balance = storedConfig.cachedBalance,
                        lastUpdated = storedConfig.lastUpdated,
                    )
                    observeHistory(storedConfig)
                    refreshNow(storedConfig)
                } else {
                    _state.value = _state.value.copy(
                        isInitializing = false,
                        studentId = portalStudentId.orEmpty(),
                        campuses = campuses,
                    )
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isInitializing = false,
                    errorMessage = error.message ?: "电费配置加载失败",
                )
            }
        }
    }

    fun loginAndLoadCampuses() {
        if (_state.value.isLoadingLocations) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingLocations = true)
            runCatching {
                val auth = authenticate()
                val campuses = repository.storedCampuses().ifEmpty { repository.campuses(auth.token) }
                auth to campuses
            }.onSuccess { (auth, campuses) ->
                if (campuses.isNotEmpty()) repository.saveLocations(0, campuses)
                _state.value = _state.value.copy(
                    isLoggedIn = true,
                    isLoadingLocations = false,
                    studentId = auth.studentId,
                    campuses = campuses,
                    errorMessage = null,
                )
            }.onFailure { error ->
                sessionToken = ""
                _state.value = _state.value.copy(
                    isLoggedIn = false,
                    isLoadingLocations = false,
                    errorMessage = error.message ?: "统一认证失败",
                )
            }
        }
    }

    fun loadCampuses() {
        if (!_state.value.isLoggedIn || sessionToken.isBlank()) {
            loginAndLoadCampuses()
            return
        }
        if (_state.value.campuses.isNotEmpty() || _state.value.isLoadingLocations) return
        viewModelScope.launch {
            loadLocations("校区列表加载失败") { token -> repository.campuses(token) }
                ?.let { campuses ->
                    repository.saveLocations(0, campuses)
                    _state.value = _state.value.copy(campuses = campuses)
                }
        }
    }

    fun selectCampus(campus: ElectricityLocation) {
        _state.value = _state.value.copy(
            selectedCampus = campus,
            selectedBuilding = null,
            selectedRoom = null,
            buildings = emptyList(),
            rooms = emptyList(),
        )
        viewModelScope.launch {
            val stored = repository.storedBuildings(campus.value)
            if (stored.isNotEmpty()) {
                _state.value = _state.value.copy(buildings = stored)
                return@launch
            }
            loadLocations("楼栋列表加载失败") { token -> repository.buildings(token, campus.value) }
                ?.let { buildings ->
                    repository.saveLocations(1, buildings, campus.value)
                    _state.value = _state.value.copy(buildings = buildings)
                }
        }
    }

    fun selectBuilding(building: ElectricityLocation) {
        val campus = _state.value.selectedCampus ?: return
        _state.value = _state.value.copy(
            selectedBuilding = building,
            selectedRoom = null,
            rooms = emptyList(),
        )
        viewModelScope.launch {
            val parent = "${campus.value}|${building.value}"
            val stored = repository.storedRooms(parent)
            if (stored.isNotEmpty()) {
                _state.value = _state.value.copy(rooms = stored)
                return@launch
            }
            loadLocations("房号列表加载失败") { token ->
                repository.rooms(token, campus.value, building.value)
            }?.let { rooms ->
                repository.saveLocations(2, rooms, parent)
                _state.value = _state.value.copy(rooms = rooms)
            }
        }
    }

    fun selectRoom(room: ElectricityLocation) {
        _state.value = _state.value.copy(selectedRoom = room)
    }

    fun saveAndQuery() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            val state = _state.value
            val campus = state.selectedCampus ?: return@launch
            val building = state.selectedBuilding ?: return@launch
            val room = state.selectedRoom ?: return@launch
            _state.value = state.copy(isLoading = true)
            runCatching {
                var auth = ensureAuth()
                var activeConfig = ElectricityConfig(auth.studentId, campus, building, room, auth.token)
                val result = runCatching { repository.query(activeConfig, auth.token) }.getOrElse {
                    auth = authenticate(force = true)
                    activeConfig = activeConfig.copy(studentId = auth.studentId, token = auth.token)
                    repository.query(activeConfig, auth.token)
                }
                activeConfig = activeConfig.copy(
                    cachedBalance = result.balance,
                    lastUpdated = result.queriedAt,
                )
                repository.save(activeConfig)
                config = activeConfig
                activeConfig to result
            }.onSuccess { (activeConfig, result) ->
                _state.value = _state.value.copy(
                    isConfigured = true,
                    isLoggedIn = true,
                    isLoading = false,
                    studentId = activeConfig.studentId,
                    balance = result.balance,
                    lastUpdated = result.queriedAt,
                    editing = false,
                    errorMessage = null,
                )
                observeHistory(activeConfig)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "电量查询失败",
                )
            }
        }
    }

    fun refresh(target: ElectricityConfig? = config) {
        val initialConfig = target ?: return
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            refreshNow(initialConfig)
        }
    }

    fun editConfiguration() {
        _state.value = _state.value.copy(editing = true)
    }

    fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun clearConfiguration() {
        viewModelScope.launch {
            repository.clear(config)
            historyJob?.cancel()
            config = null
            sessionToken = ""
            _state.value = ElectricityUiState(
                isInitializing = false,
                studentId = repository.currentPortalStudentId().orEmpty(),
            )
        }
    }

    private suspend fun refreshNow(initialConfig: ElectricityConfig) {
        runCatching {
            var activeConfig = initialConfig
            val result = runCatching {
                val token = activeConfig.token.ifBlank { ensureAuth().token }
                activeConfig = activeConfig.copy(token = token)
                repository.query(activeConfig, token)
            }.getOrElse {
                val auth = authenticate(force = true)
                activeConfig = activeConfig.copy(studentId = auth.studentId, token = auth.token)
                repository.query(activeConfig, auth.token)
            }
            activeConfig = activeConfig.copy(
                cachedBalance = result.balance,
                lastUpdated = result.queriedAt,
            )
            repository.save(activeConfig)
            config = activeConfig
            activeConfig to result
        }.onSuccess { (activeConfig, result) ->
            _state.value = _state.value.copy(
                isLoggedIn = true,
                isLoading = false,
                studentId = activeConfig.studentId,
                balance = result.balance,
                lastUpdated = result.queriedAt,
                errorMessage = null,
            )
            observeHistory(activeConfig)
        }.onFailure { error ->
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = error.message ?: "电量查询失败",
            )
        }
    }

    private suspend fun authenticate(force: Boolean = false): ElectricityPortalAuth =
        repository.authenticateWithPortal(force).also { auth ->
            sessionToken = auth.token
            _state.value = _state.value.copy(studentId = auth.studentId)
        }

    private suspend fun ensureAuth(): ElectricityPortalAuth = if (sessionToken.isBlank()) {
        authenticate()
    } else {
        ElectricityPortalAuth(_state.value.studentId, sessionToken)
    }

    private suspend fun <T> loadLocations(
        fallbackMessage: String,
        block: suspend (String) -> T,
    ): T? {
        _state.value = _state.value.copy(isLoadingLocations = true)
        return runCatching {
            val auth = ensureAuth()
            runCatching { block(auth.token) }.getOrElse {
                block(authenticate(force = true).token)
            }
        }.onSuccess {
            _state.value = _state.value.copy(isLoggedIn = true, isLoadingLocations = false, errorMessage = null)
        }.onFailure { error ->
            _state.value = _state.value.copy(
                isLoadingLocations = false,
                errorMessage = error.message ?: fallbackMessage,
            )
        }.getOrNull()
    }

    private fun observeHistory(activeConfig: ElectricityConfig) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            repository.history(activeConfig).collect { history ->
                _state.value = _state.value.copy(
                    history = history.map { it.recordedAt to it.balance },
                )
            }
        }
    }
}
