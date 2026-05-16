package com.yslee.subwaywhen.core.logger

import android.util.Log

enum class LogLevel { ERROR, INFO, DEBUG }

class AppLogger(
    private val categoryName: String,
    private val totalLogEnabled: Boolean,
) {
    fun log(level: LogLevel, message: String, enableLog: Boolean = true) {
        if (!totalLogEnabled || !enableLog) return
        val tag = "com.yslee.subwaywhen"
        val msg = "$categoryName: $message"
        when (level) {
            LogLevel.ERROR -> Log.e(tag, msg)
            LogLevel.INFO -> Log.i(tag, msg)
            LogLevel.DEBUG -> Log.d(tag, msg)
        }
    }

    companion object {
        val Network = AppLogger(categoryName = "🛜 Network", totalLogEnabled = true)
        val Core = AppLogger(categoryName = "💪 Core", totalLogEnabled = true)
        val View = AppLogger(categoryName = "💬 View", totalLogEnabled = true)
    }
}
