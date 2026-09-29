package com.wild0408.nanxinshiguang.widget.electricity

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.wild0408.nanxinshiguang.data.db.main.ElectricityHistoryDao
import com.wild0408.nanxinshiguang.data.repository.ElectricityRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

private object ElectricityDependencyContainer : KoinComponent {
    val electricityRepository: ElectricityRepository by inject()
    val electricityHistoryDao: ElectricityHistoryDao by inject()
}

object ElectricityWidgetUpdateHelper {

    private const val TAG = "ElectricityWidgetUpdate"

    suspend fun updateWidgets(context: Context) {
        try {
            val repository = ElectricityDependencyContainer.electricityRepository
            val historyDao = ElectricityDependencyContainer.electricityHistoryDao

            val config = withTimeoutOrNull(2.seconds) {
                repository.configFlow.first()
            }

            val snapshot = if (config == null) {
                ElectricityWidgetSnapshot(isConfigured = false)
            } else {
                val historyList = try {
                    withTimeoutOrNull(2.seconds) {
                        historyDao.observe(config.studentId, config.room.value).first()
                    } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                val now = System.currentTimeMillis()
                val oneDayAgo = now - 24 * 3600 * 1000
                val yesterdayBalance = historyList.firstOrNull { it.recordedAt <= oneDayAgo }?.balance
                    ?: historyList.getOrNull(1)?.balance

                ElectricityWidgetSnapshot(
                    isConfigured = true,
                    roomName = config.room.name.ifBlank { "${config.building.name} ${config.room.value}" },
                    balance = config.cachedBalance,
                    lastUpdated = config.lastUpdated,
                    yesterdayBalance = yesterdayBalance,
                    isTokenExpired = false
                )
            }

            renderAllWidgets(context, snapshot)

        } catch (e: Exception) {
            Log.e(TAG, "更新电费小组件异常: ${e.stackTraceToString()}")
        }
    }

    suspend fun forceRefreshElectricity(context: Context) {
        try {
            val repository = ElectricityDependencyContainer.electricityRepository
            val config = repository.configFlow.first()
            if (config == null) {
                Toast.makeText(context, "未配置宿舍，请先在应用内设置", Toast.LENGTH_SHORT).show()
                updateWidgets(context)
                return
            }

            val auth = repository.authenticateWithPortal(force = false)
            val result = repository.query(config, auth.token)

            Toast.makeText(
                context,
                "刷新成功：最新电量 ${String.format("%.2f", result.balance)} 度",
                Toast.LENGTH_SHORT
            ).show()

            updateWidgets(context)

        } catch (e: Exception) {
            Log.e(TAG, "手动刷新电费异常", e)
            val msg = e.message ?: "刷新失败"
            Toast.makeText(context, "刷新失败: $msg", Toast.LENGTH_SHORT).show()

            // 更新带有错误标识的快照
            try {
                val repository = ElectricityDependencyContainer.electricityRepository
                val config = repository.configFlow.first()
                if (config != null) {
                    val snapshot = ElectricityWidgetSnapshot(
                        isConfigured = true,
                        roomName = config.room.name,
                        balance = config.cachedBalance,
                        lastUpdated = config.lastUpdated,
                        isTokenExpired = msg.contains("凭据") || msg.contains("登录"),
                        errorMessage = msg
                    )
                    renderAllWidgets(context, snapshot)
                }
            } catch (_: Exception) {}
        }
    }

    private fun renderAllWidgets(context: Context, snapshot: ElectricityWidgetSnapshot) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // 精简版 (2x2)
        val compactComponent = ComponentName(context, ElectricityWidgetProvider.Compact::class.java)
        val compactIds = appWidgetManager.getAppWidgetIds(compactComponent)
        if (compactIds.isNotEmpty()) {
            val compactViews = ElectricityWidgetRenderer.renderCompact(context, snapshot)
            appWidgetManager.updateAppWidget(compactComponent, compactViews)
        }

        // 详细版 (4x2)
        val detailedComponent = ComponentName(context, ElectricityWidgetProvider.Detailed::class.java)
        val detailedIds = appWidgetManager.getAppWidgetIds(detailedComponent)
        if (detailedIds.isNotEmpty()) {
            val detailedViews = ElectricityWidgetRenderer.renderDetailed(context, snapshot)
            appWidgetManager.updateAppWidget(detailedComponent, detailedViews)
        }
    }
}
