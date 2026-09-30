package com.wild0408.nanxinshiguang.ui.viewmodel.service.grade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.portal.GradeQueryResult
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import com.wild0408.nanxinshiguang.data.repository.GradeImportStore
import com.wild0408.nanxinshiguang.data.repository.GradeRepository
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class GradeSyncUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

@KoinViewModel
class GradeSyncViewModel(
    private val repository: GradeRepository,
    private val credentials: PortalCredentialRepository,
    private val session: PortalSession,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GradeSyncUiState())
    val uiState: StateFlow<GradeSyncUiState> = _uiState.asStateFlow()

    fun refresh() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.value = GradeSyncUiState(loading = true)
            try {
                credentials.load()
                if (credentials.state.value !is PortalBindingState.Bound) {
                    _uiState.value = GradeSyncUiState(errorMessage = "请先绑定统一门户")
                    return@launch
                }
                when (val result = session.fetchGrades()) {
                    is GradeQueryResult.Success -> {
                        repository.replaceAll(result.records)
                        GradeImportStore.replace(result.records)
                        _uiState.value = GradeSyncUiState()
                    }
                    is GradeQueryResult.NotLoggedIn -> {
                        _uiState.value = GradeSyncUiState(errorMessage = result.message)
                    }
                    is GradeQueryResult.NetworkError -> {
                        _uiState.value = GradeSyncUiState(errorMessage = result.message)
                    }
                    is GradeQueryResult.Error -> {
                        _uiState.value = GradeSyncUiState(errorMessage = result.message)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = GradeSyncUiState(errorMessage = "成绩保存失败，请重试")
            } finally {
                if (_uiState.value.loading) {
                    _uiState.value = _uiState.value.copy(loading = false)
                }
            }
        }
    }
}
