package com.wild0408.nanxinshiguang.widget.electricity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.wild0408.nanxinshiguang.MainActivity
import com.wild0408.nanxinshiguang.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

object ElectricityWidgetRenderer {

    const val ACTION_REFRESH_ELECTRICITY = "com.wild0408.nanxinshiguang.ACTION_REFRESH_ELECTRICITY_WIDGET"
    const val EXTRA_DESTINATION = "extra_destination"
    const val DESTINATION_ELECTRICITY = "electricity_center"

    private data class StatusInfo(
        val text: String,
        val textColorRes: Int,
        val pillBgRes: Int
    )

    /**
     * 渲染精简版 (2x2) 电费小组件
     */
    fun renderCompact(context: Context, snapshot: ElectricityWidgetSnapshot): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_electricity_compact)

        // 设置卡片整体点击事件：跳转 App 电费中心
        val pendingIntent = createLaunchPendingIntent(context)
        rv.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // 刷新按钮点击事件
        val refreshIntent = Intent(context, ElectricityWidgetProvider.Compact::class.java).apply {
            action = ACTION_REFRESH_ELECTRICITY
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent)

        if (!snapshot.isConfigured) {
            rv.setViewVisibility(R.id.container_balance, View.GONE)
            rv.setViewVisibility(R.id.container_unconfigured, View.VISIBLE)
            rv.setTextViewText(
                R.id.tv_unconfigured_tip,
                context.getString(R.string.widget_electricity_unconfigured)
            )
            rv.setTextViewText(R.id.tv_room_name, context.getString(R.string.widget_electricity_title))
            rv.setTextViewText(R.id.tv_updated_time, "")
            return rv
        }

        rv.setViewVisibility(R.id.container_unconfigured, View.GONE)
        rv.setViewVisibility(R.id.container_balance, View.VISIBLE)

        val roomDisplay = snapshot.roomName.ifBlank { context.getString(R.string.widget_electricity_title) }
        rv.setTextViewText(R.id.tv_room_name, roomDisplay)
        rv.setTextViewText(R.id.tv_updated_time, formatRelativeTime(context, snapshot.lastUpdated))

        val balance = snapshot.balance
        if (balance != null) {
            rv.setTextViewText(R.id.tv_balance, String.format("%.2f", balance))
            val statusInfo = getStatusInfo(context, snapshot, balance)
            rv.setTextViewText(R.id.tv_status_tag, statusInfo.text)
            rv.setTextColor(
                R.id.tv_status_tag,
                ContextCompat.getColor(context, statusInfo.textColorRes)
            )
            rv.setInt(R.id.tv_status_tag, "setBackgroundResource", statusInfo.pillBgRes)

            // 计算较昨日变化量
            val yBalance = snapshot.yesterdayBalance
            if (yBalance != null) {
                val diff = balance - yBalance
                val diffStr = if (abs(diff) < 0.01) {
                    "较昨日持平"
                } else {
                    val sign = if (diff > 0) "+" else ""
                    context.getString(R.string.widget_electricity_change_format, String.format("%s%.2f", sign, diff))
                }
                rv.setTextViewText(R.id.tv_yesterday_change, diffStr)
                rv.setViewVisibility(R.id.tv_yesterday_change, View.VISIBLE)
            } else {
                rv.setViewVisibility(R.id.tv_yesterday_change, View.GONE)
            }

        } else {
            rv.setTextViewText(R.id.tv_balance, "--")
            rv.setTextViewText(
                R.id.tv_status_tag,
                snapshot.errorMessage ?: context.getString(R.string.widget_electricity_unconfigured)
            )
            rv.setTextColor(
                R.id.tv_status_tag,
                ContextCompat.getColor(context, R.color.widget_text_secondary)
            )
            rv.setViewVisibility(R.id.tv_yesterday_change, View.GONE)
        }

        return rv
    }

    /**
     * 渲染详细版 (4x2) 电费小组件
     */
    fun renderDetailed(context: Context, snapshot: ElectricityWidgetSnapshot): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_electricity_detailed)

        // 主卡片点击跳转
        val launchIntent = createLaunchPendingIntent(context)
        rv.setOnClickPendingIntent(R.id.widget_root, launchIntent)

        // 手动刷新按钮点击 Intent
        val refreshIntent = Intent(context, ElectricityWidgetProvider.Detailed::class.java).apply {
            action = ACTION_REFRESH_ELECTRICITY
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent)

        if (!snapshot.isConfigured) {
            rv.setViewVisibility(R.id.container_balance, View.GONE)
            rv.setViewVisibility(R.id.container_unconfigured, View.VISIBLE)
            rv.setTextViewText(
                R.id.tv_unconfigured_tip,
                context.getString(R.string.widget_electricity_unconfigured)
            )
            rv.setTextViewText(R.id.tv_room_name, context.getString(R.string.widget_electricity_title))
            rv.setTextViewText(R.id.tv_updated_time, "")
            return rv
        }

        rv.setViewVisibility(R.id.container_unconfigured, View.GONE)
        rv.setViewVisibility(R.id.container_balance, View.VISIBLE)

        val roomDisplay = snapshot.roomName.ifBlank { context.getString(R.string.widget_electricity_title) }
        rv.setTextViewText(R.id.tv_room_name, roomDisplay)

        val updatedStr = formatRelativeTime(context, snapshot.lastUpdated)
        rv.setTextViewText(
            R.id.tv_updated_time,
            if (updatedStr.isNotBlank()) context.getString(R.string.widget_electricity_updated_format, updatedStr) else ""
        )

        val balance = snapshot.balance
        if (balance != null) {
            rv.setTextViewText(R.id.tv_balance, String.format("%.2f", balance))
            val statusInfo = getStatusInfo(context, snapshot, balance)
            rv.setTextViewText(R.id.tv_status_tag, statusInfo.text)
            rv.setTextColor(
                R.id.tv_status_tag,
                ContextCompat.getColor(context, statusInfo.textColorRes)
            )
            rv.setInt(R.id.tv_status_tag, "setBackgroundResource", statusInfo.pillBgRes)

            // 计算较昨日变化量
            val yBalance = snapshot.yesterdayBalance
            if (yBalance != null) {
                val diff = balance - yBalance
                val diffStr = if (abs(diff) < 0.01) {
                    "较昨日持平"
                } else {
                    val sign = if (diff > 0) "+" else ""
                    context.getString(R.string.widget_electricity_change_format, String.format("%s%.2f", sign, diff))
                }
                rv.setTextViewText(R.id.tv_yesterday_change, diffStr)
                rv.setViewVisibility(R.id.container_trend, View.VISIBLE)
            } else {
                rv.setViewVisibility(R.id.container_trend, View.GONE)
            }
        } else {
            rv.setTextViewText(R.id.tv_balance, "--")
            rv.setTextViewText(
                R.id.tv_status_tag,
                snapshot.errorMessage ?: context.getString(R.string.widget_electricity_unconfigured)
            )
            rv.setTextColor(
                R.id.tv_status_tag,
                ContextCompat.getColor(context, R.color.widget_text_secondary)
            )
            rv.setViewVisibility(R.id.container_trend, View.GONE)
        }

        return rv
    }

    private fun getStatusInfo(
        context: Context,
        snapshot: ElectricityWidgetSnapshot,
        balance: Double
    ): StatusInfo {
        if (snapshot.isTokenExpired) {
            return StatusInfo(
                text = context.getString(R.string.widget_electricity_token_expired),
                textColorRes = R.color.electricity_status_warning,
                pillBgRes = R.drawable.widget_status_pill_warning
            )
        }
        return when {
            balance >= 30.0 -> StatusInfo(
                text = context.getString(R.string.widget_electricity_normal),
                textColorRes = R.color.electricity_status_normal,
                pillBgRes = R.drawable.widget_status_pill_normal
            )
            balance >= 10.0 -> StatusInfo(
                text = context.getString(R.string.widget_electricity_warning),
                textColorRes = R.color.electricity_status_warning,
                pillBgRes = R.drawable.widget_status_pill_warning
            )
            else -> StatusInfo(
                text = context.getString(R.string.widget_electricity_danger),
                textColorRes = R.color.electricity_status_danger,
                pillBgRes = R.drawable.widget_status_pill_danger
            )
        }
    }

    private fun createLaunchPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, DESTINATION_ELECTRICITY)
        }
        return PendingIntent.getActivity(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatRelativeTime(context: Context, timestampMs: Long?): String {
        if (timestampMs == null || timestampMs <= 0) return ""
        val now = System.currentTimeMillis()
        val diffSeconds = (now - timestampMs) / 1000
        return when {
            diffSeconds < 60 -> context.getString(R.string.widget_electricity_updated_just_now)
            diffSeconds < 3600 -> "${diffSeconds / 60}m前"
            diffSeconds < 86400 -> "${diffSeconds / 3600}h前"
            else -> {
                val instant = Instant.ofEpochMilli(timestampMs)
                instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
            }
        }
    }
}
