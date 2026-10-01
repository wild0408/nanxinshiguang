package com.wild0408.nanxinshiguang.ui.viewmodel.service.grade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.model.AcademicSummary
import com.wild0408.nanxinshiguang.data.portal.AcademicSummaryResult
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import com.wild0408.nanxinshiguang.data.repository.AcademicSummaryRepository
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class AcademicSummaryUiState(
    val summary: AcademicSummary? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val portalBound: Boolean = false,
)

@KoinViewModel
class AcademicSummaryViewModel(
    private val repository: AcademicSummaryRepository,
    private val credentials: PortalCredentialRepository,
    private val session: PortalSession,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AcademicSummaryUiState())
    val uiState: StateFlow<AcademicSummaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            credentials.load()
            repository.load()
            val binding = credentials.state.value
            val cached = repository.summary.value.forAccount(binding)
            _uiState.value = AcademicSummaryUiState(
                summary = cached,
                loading = false,
                portalBound = binding is PortalBindingState.Bound,
            )
            if (binding is PortalBindingState.Bound && cached == null) {
                refresh()
            }

            combine(credentials.state, repository.summary) { binding, summary ->
                binding to summary.forAccount(binding)
            }.collect { (binding, summary) ->
                _uiState.value = _uiState.value.copy(
                    portalBound = binding is PortalBindingState.Bound,
                    summary = summary,
                    errorMessage = if (binding is PortalBindingState.Bound) _uiState.value.errorMessage else null,
                )
            }
        }
    }

    fun refresh() {
        if (_uiState.value.loading) return
        val bound = credentials.state.value as? PortalBindingState.Bound ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
            try {
                val state = when (val result = session.fetchAcademicSummary()) {
                    is AcademicSummaryResult.Success -> {
                        val current = credentials.state.value as? PortalBindingState.Bound
                        if (current == null || current.bundle.studentId != result.summary.studentId ||
                            bound.bundle.studentId != result.summary.studentId
                        ) {
                            _uiState.value.copy(errorMessage = "门户返回的学号与绑定账号不一致")
                        } else {
                            repository.save(result.summary)
                            _uiState.value.copy(summary = result.summary)
                        }
                    }
                    is AcademicSummaryResult.NotLoggedIn -> _uiState.value.copy(errorMessage = result.message)
                    is AcademicSummaryResult.NetworkError -> _uiState.value.copy(errorMessage = result.message)
                    is AcademicSummaryResult.Error -> _uiState.value.copy(errorMessage = result.message)
                }
                _uiState.value = state
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "学业概览保存失败，请重试")
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}

private fun AcademicSummary?.forAccount(binding: PortalBindingState): AcademicSummary? {
    val id = (binding as? PortalBindingState.Bound)?.bundle?.studentId
    return takeIf { id != null && it?.studentId == id }
}
