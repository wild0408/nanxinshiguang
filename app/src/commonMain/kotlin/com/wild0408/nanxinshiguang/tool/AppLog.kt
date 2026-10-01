package com.wild0408.nanxinshiguang.tool

/**
 * 统一日志出口。
 *
 * 之前各处直接 `printStackTrace()` / `println()`：既不会进入 logcat 的结构化日志，
 * 也无法统一控制级别与脱敏。这里收敛为一个外观（facade），平台实现只提供最底层的
 * 一个 expect 函数（顶层函数不会触发 expect/actual 类的 Beta 警告）。
 *
 * 约定：日志内容不得包含账号、密码、Cookie、Token、Passkey 或学生个人信息。
 */
object AppLog {
    fun d(tag: String, message: String) {
        platformLog(AppLogLevel.DEBUG, tag, message, null)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        platformLog(AppLogLevel.ERROR, tag, message, throwable)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        platformLog(AppLogLevel.WARN, tag, message, throwable)
    }
}

enum class AppLogLevel { DEBUG, WARN, ERROR }

expect fun platformLog(level: AppLogLevel, tag: String, message: String, throwable: Throwable?)
