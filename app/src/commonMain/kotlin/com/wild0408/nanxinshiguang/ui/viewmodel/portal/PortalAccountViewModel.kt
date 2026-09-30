package com.wild0408.nanxinshiguang.ui.viewmodel.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import com.wild0408.nanxinshiguang.data.repository.AcademicSummaryRepository
import com.wild0408.nanxinshiguang.data.portal.PortalLoginResult
import com.wild0408.nanxinshiguang.data.portal.PortalProfileResult
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PortalAccountViewModel(
    private val repository: PortalCredentialRepository,
    private val academicSummaryRepository: AcademicSummaryRepository,
    private val portalSession: PortalSession,
) : ViewModel() {
    val state: StateFlow<PortalBindingState> = repository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortalBindingState.Loading)
    val profile = repository.profile
    private val _verifying = MutableStateFlow(false)
    val verifying = _verifying.asStateFlow()

    init {
        viewModelScope.launch {
            repository.load()
            // 兼容升级前已经绑定的账号：只在本地没有资料时补拉一次，
            // 成功后由 Repository 持久化，后续打开页面只读本地。
            if (repository.state.value is PortalBindingState.Bound && repository.profile.value == null) {
                when (val result = portalSession.fetchUserProfile(force = false)) {
                    is PortalProfileResult.Success -> repository.saveProfile(result.profile)
                    else -> Unit
                }
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.clear()
            academicSummaryRepository.clear()
        }
    }

    fun verify(onResult: (PortalLoginResult) -> Unit) {
        if (_verifying.value) return
        viewModelScope.launch {
            _verifying.value = true
            val result = portalSession.verifyCredential()
            _verifying.value = false
            onResult(result)
        }
    }
}
