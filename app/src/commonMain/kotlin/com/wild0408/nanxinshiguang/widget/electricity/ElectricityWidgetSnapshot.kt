package com.wild0408.nanxinshiguang.widget.electricity

/**
 * 宿舍电费小组件的数据快照类
 */
data class ElectricityWidgetSnapshot(
    val isConfigured: Boolean = false,
    val roomName: String = "",
    val balance: Double? = null,
    val lastUpdated: Long? = null,
    val yesterdayBalance: Double? = null,
    val isTokenExpired: Boolean = false,
    val errorMessage: String? = null
)
