package com.wild0408.nanxinshiguang.data.repository

import com.wild0408.nanxinshiguang.data.model.LaborScore
import com.wild0408.nanxinshiguang.data.portal.LaborScoreResult
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

enum class LaborScoreStatus { Idle, Loading, Success, Empty, NotLoggedIn, Error }

data class LaborScoreState(
    val score: LaborScore? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val status: LaborScoreStatus = LaborScoreStatus.Idle,
)

@Single
class LaborScoreRepository(
    private val session: PortalSession,
    private val credentials: PortalCredentialRepository,
    private val cache: LaborScoreCache,
) {
    private val _state = MutableStateFlow(LaborScoreState(loading = true, status = LaborScoreStatus.Loading))
    val state = _state.asStateFlow()
    private val mutex = Mutex()
    private var initialized = false
    private var account: String? = null

    suspend fun initialize(studentId: String?) = mutex.withLock {
        if (initialized && account == studentId) return@withLock
        initialized = true
        account = studentId
        _state.value = LaborScoreState(loading = true, status = LaborScoreStatus.Loading)
        if (studentId == null) {
            _state.value = LaborScoreState(status = LaborScoreStatus.NotLoggedIn)
            return@withLock
        }
        try {
            val saved = cache.load(studentId)
            if (currentAccount() != studentId) {
                _state.value = LaborScoreState()
                return@withLock
            }
            if (saved != null) {
                _state.value = saved.toState()
            } else {
                // Only a missing cache triggers the first automatic query; empty results are cached too.
                refreshAccount(studentId)
            }
        } catch (e: CancellationException) {
            initialized = false
            throw e
        } catch (e: Exception) {
            _state.value = LaborScoreState(error = "劳动积分缓存读取失败，请手动刷新", status = LaborScoreStatus.Error)
        } finally {
            _state.value = _state.value.copy(loading = false)
        }
    }

    suspend fun refresh() {
        if (!mutex.tryLock()) return
        try {
            val studentId = currentAccount()
            if (studentId == null) {
                _state.value = LaborScoreState(status = LaborScoreStatus.NotLoggedIn)
                return
            }
            if (account != studentId) {
                account = studentId
                initialized = true
                _state.value = LaborScoreState()
            }
            refreshAccount(studentId)
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun refreshAccount(studentId: String) {
        val previous = _state.value.copy(loading = false)
        _state.value = _state.value.copy(loading = true, error = null, status = LaborScoreStatus.Loading)
        try {
            val result = session.fetchLaborScore()
            // Do not expose or save a response after the user switches or unbinds the account.
            if (currentAccount() != studentId) {
                _state.value = LaborScoreState()
                return
            }
            _state.value = when (result) {
                is LaborScoreResult.Success -> {
                    cache.save(studentId, result.score)
                    result.score.toState()
                }
                is LaborScoreResult.NotLoggedIn -> _state.value.copy(error = result.message, status = LaborScoreStatus.NotLoggedIn)
                is LaborScoreResult.NetworkError -> _state.value.copy(error = result.message, status = LaborScoreStatus.Error)
                is LaborScoreResult.Error -> _state.value.copy(error = result.message, status = LaborScoreStatus.Error)
            }
        } catch (e: CancellationException) {
            _state.value = previous
            throw e
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "劳动积分获取或保存失败，请重试", status = LaborScoreStatus.Error)
        } finally {
            _state.value = _state.value.copy(loading = false)
        }
    }

    private fun currentAccount(): String? =
        (credentials.state.value as? PortalBindingState.Bound)?.bundle?.studentId

    private fun LaborScore.toState() = LaborScoreState(
        score = takeIf(LaborScore::hasData),
        status = if (hasData) LaborScoreStatus.Success else LaborScoreStatus.Empty,
    )
}
