package com.wild0408.nanxinshiguang.tool

import android.util.Log

actual fun platformLog(level: AppLogLevel, tag: String, message: String, throwable: Throwable?) {
    when (level) {
        AppLogLevel.DEBUG -> Log.d(tag, message, throwable)
        AppLogLevel.WARN -> Log.w(tag, message, throwable)
        AppLogLevel.ERROR -> Log.e(tag, message, throwable)
    }
}
