package com.wild0408.nanxinshiguang.ui.viewmodel.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import com.wild0408.nanxinshiguang.data.repository.AcademicSummaryRepository
import com.wild0408.nanxinshiguang.data.portal.PortalCredentialImportResult
import com.wild0408.nanxinshiguang.data.portal.PortalCredentialTransfer
import com.wild0408.nanxinshiguang.data.portal.PortalLoginResult
import com.wild0408.nanxinshiguang.data.portal.PortalProfileResult
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PortalAccountViewModel(
    private val repository: PortalCredentialRepository,
    private val academicSummaryRepository: AcademicSummaryRepository,
    private val portalSession: PortalSession,
    private val credentialTransfer: PortalCredentialTransfer,
) : ViewModel() {
    val state: StateFlow<PortalBindingState> = repository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortalBindingState.Loading)
    val profile = repository.profile
    private val _verifying = MutableStateFlow(false)
    val verifying = _verifying.asStateFlow()
    private val _transferring = MutableStateFlow(false)

    /** 导出/导入进行中；口令派生需要数百毫秒，期间禁用相关入口。 */
    val transferring = _transferring.asStateFlow()

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

    /**
     * 生成可迁移到其他设备的密钥文件。
     * 口令派生与加密放到 [Dispatchers.Default]，完成后回到主线程交出字节数据，
     * 由页面交给系统文件保存器写出。
     */
    fun exportCredential(password: String, onReady: (bytes: ByteArray?, fileName: String) -> Unit) {
        val current = repository.state.value
        if (current !is PortalBindingState.Bound || _transferring.value) {
            onReady(null, "")
            return
        }
        viewModelScope.launch {
            _transferring.value = true
            val fileName = credentialTransfer.defaultFileName()
            val bytes = withContext(Dispatchers.Default) {
                credentialTransfer.export(current.bundle, repository.profile.value, password)
            }
            _transferring.value = false
            onReady(bytes, fileName)
        }
    }

    /**
     * 导入其他设备导出的密钥文件：成功后写回本机（由设备密钥库重新加密），
     * 并清掉可能属于上一个账号的学业概览缓存。
     */
    fun importCredential(
        bytes: ByteArray,
        password: String,
        onResult: (PortalCredentialImportResult) -> Unit
    ) {
        if (_transferring.value) return
        viewModelScope.launch {
            _transferring.value = true
            val result = withContext(Dispatchers.Default) {
                credentialTransfer.import(bytes, password)
            }
            if (result is PortalCredentialImportResult.Success) {
                academicSummaryRepository.clear()
                repository.save(result.bundle)
                result.profile?.let { repository.saveProfile(it) }
            }
            _transferring.value = false
            onResult(result)
        }
    }
}
