package com.yslee.subwaywhen.core.logger

import android.util.Log
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify

class AppLoggerTest : FunSpec({

    beforeEach {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
    }

    afterEach {
        unmockkStatic(Log::class)
    }

    test("totalLogEnabled=true, enableLog=true 일 때 레벨별로 Log.*가 정확히 1회 호출된다") {
        val logger = AppLogger(categoryName = "Test", totalLogEnabled = true)

        logger.log(LogLevel.INFO, "info message")
        logger.log(LogLevel.ERROR, "error message")
        logger.log(LogLevel.DEBUG, "debug message")

        verify(exactly = 1) { Log.i(any(), any()) }
        verify(exactly = 1) { Log.e(any(), any()) }
        verify(exactly = 1) { Log.d(any(), any()) }
    }

    test("totalLogEnabled=false 일 때 Log.*가 호출되지 않는다") {
        val logger = AppLogger(categoryName = "Test", totalLogEnabled = false)

        logger.log(LogLevel.INFO, "info message")
        logger.log(LogLevel.ERROR, "error message")
        logger.log(LogLevel.DEBUG, "debug message")

        verify(exactly = 0) { Log.i(any(), any()) }
        verify(exactly = 0) { Log.e(any(), any()) }
        verify(exactly = 0) { Log.d(any(), any()) }
    }

    test("totalLogEnabled=true, enableLog=false 일 때 Log.*가 호출되지 않는다") {
        val logger = AppLogger(categoryName = "Test", totalLogEnabled = true)

        logger.log(LogLevel.INFO, "info message", enableLog = false)
        logger.log(LogLevel.ERROR, "error message", enableLog = false)
        logger.log(LogLevel.DEBUG, "debug message", enableLog = false)

        verify(exactly = 0) { Log.i(any(), any()) }
        verify(exactly = 0) { Log.e(any(), any()) }
        verify(exactly = 0) { Log.d(any(), any()) }
    }

    test("LogLevel별로 올바른 Log 메서드에 매핑된다") {
        val logger = AppLogger(categoryName = "Test", totalLogEnabled = true)
        val tag = "com.yslee.subwaywhen"

        logger.log(LogLevel.ERROR, "error message")
        verify(exactly = 1) { Log.e(tag, "Test: error message") }

        logger.log(LogLevel.INFO, "info message")
        verify(exactly = 1) { Log.i(tag, "Test: info message") }

        logger.log(LogLevel.DEBUG, "debug message")
        verify(exactly = 1) { Log.d(tag, "Test: debug message") }
    }
})
