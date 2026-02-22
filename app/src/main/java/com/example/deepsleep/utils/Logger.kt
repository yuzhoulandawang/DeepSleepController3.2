package com.example.deepsleep.utils

import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 日志工具类 - 提供便捷的日志记录方法
 */
object Logger {
    
    private val logRepository = LogRepository
    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * DEBUG 级别日志
     */
    fun d(tag: String, message: String) {
        logScope.launch {
            logRepository.addLog(LogLevel.DEBUG, tag, message)
        }
    }
    
    /**
     * INFO 级别日志
     */
    fun i(tag: String, message: String) {
        logScope.launch {
            logRepository.addLog(LogLevel.INFO, tag, message)
        }
    }
    
    /**
     * SUCCESS 级别日志
     */
    fun s(tag: String, message: String) {
        logScope.launch {
            logRepository.addLog(LogLevel.SUCCESS, tag, message)
        }
    }
    
    /**
     * WARNING 级别日志
     */
    fun w(tag: String, message: String) {
        logScope.launch {
            logRepository.addLog(LogLevel.WARNING, tag, message)
        }
    }
    
    /**
     * ERROR 级别日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        logScope.launch {
            val throwableStr = throwable?.stackTraceToString()
            logRepository.addLog(LogLevel.ERROR, tag, message, throwableStr)
        }
    }
    
    /**
     * FATAL 级别日志
     */
    fun f(tag: String, message: String, throwable: Throwable? = null) {
        logScope.launch {
            val throwableStr = throwable?.stackTraceToString()
            logRepository.addLog(LogLevel.FATAL, tag, message, throwableStr)
        }
    }
    
    /**
     * 清除所有日志
     */
    fun clear() {
        logScope.launch {
            logRepository.clearLogs()
        }
    }
}