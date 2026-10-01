package com.wild0408.nanxinshiguang.ui.components.bus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.wild0408.nanxinshiguang.data.api.bus.BusRepository
import com.wild0408.nanxinshiguang.data.api.bus.BusSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** 轮询间隔：车辆位置刷新频率与校园平台页面一致。 */
private const val BUS_POLL_INTERVAL_MILLIS = 5_000L

/** 公交页共享状态：快照、加载、错误与上次更新时间。Material 与 Miuix 两侧共用。 */
@Stable
class BusMapState internal constructor(
    private val repository: BusRepository,
    private val scope: CoroutineScope,
) {
    var snapshot by mutableStateOf<BusSnapshot?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val lastUpdatedMillis: Long? get() = snapshot?.fetchedAtMillis

    suspend fun refreshNow() {
        loading = true
        runCatching { repository.fetchSnapshot() }
            .onSuccess {
                snapshot = it
                error = null
            }
            .onFailure {
                error = it.message
            }
        loading = false
    }

    /** 手动刷新入口（页面按钮 / 顶栏刷新）。 */
    fun refresh() {
        scope.launch { refreshNow() }
    }

    internal fun startPolling(): Job = scope.launch {
        while (isActive) {
            refreshNow()
            delay(BUS_POLL_INTERVAL_MILLIS)
        }
    }
}

/**
 * 生命周期感知的公交轮询：页面回到前台才轮询，切到后台立即停止，
 * 避免应用不可见时仍然每 5 秒请求一次接口。
 */
@Composable
fun rememberBusMapState(repository: BusRepository, refreshRequest: Int = 0): BusMapState {
    val scope = rememberCoroutineScope()
    val state = remember(repository) { BusMapState(repository, scope) }

    LifecycleResumeEffect(repository) {
        val job = state.startPolling()
        onPauseOrDispose { job.cancel() }
    }

    LaunchedEffect(refreshRequest) {
        if (refreshRequest > 0) state.refreshNow()
    }

    return state
}
