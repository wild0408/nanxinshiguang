package com.wild0408.nanxinshiguang.ui.viewmodel.service.labor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.repository.LaborScoreRepository
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LaborScoreViewModel(
    private val repository: LaborScoreRepository,
    private val credentials: PortalCredentialRepository,
) : ViewModel() {
    val uiState = repository.state

    init {
        viewModelScope.launch {
            credentials.load()
            credentials.state.map { (it as? PortalBindingState.Bound)?.bundle?.studentId }
                .distinctUntilChanged()
                .collect { repository.initialize(it) }
        }
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }
}
