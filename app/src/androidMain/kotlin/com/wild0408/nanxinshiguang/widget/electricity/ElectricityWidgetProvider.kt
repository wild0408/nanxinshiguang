package com.wild0408.nanxinshiguang.widget.electricity

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class ElectricityWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.Main).launch {
            ElectricityWidgetUpdateHelper.updateWidgets(context)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ElectricityWidgetRenderer.ACTION_REFRESH_ELECTRICITY) {
            Toast.makeText(context, "正在更新电费数据...", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.Main).launch {
                ElectricityWidgetUpdateHelper.forceRefreshElectricity(context)
            }
        }
    }

    /** 精简版 2x2 AppWidgetReceiver */
    class Compact : ElectricityWidgetProvider()

    /** 详细版 4x2 AppWidgetReceiver */
    class Detailed : ElectricityWidgetProvider()
}
