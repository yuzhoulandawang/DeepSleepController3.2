package com.example.deepsleep.data

import android.content.Context
import com.example.deepsleep.model.LogEntry
import com.example.deepsleep.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

object LogRepository {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    /**
     * 添加日志条目（内部方法）
     */
    fun addLog(level: LogLevel, tag: String, message: String, throwable: String? = null) {
        val timestamp = System.currentTimeMillis()
        val logEntry = LogEntry(
            timestamp = timestamp,
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )
        _logs.value = listOf(logEntry) + _logs.value.take(500) // 保留最近500条
    }
    
    /**
     * 记录 INFO 级别日志
     */
    fun info(tag: String, message: String) {
        addLog(LogLevel.INFO, tag, message)
    }
    
    /**
     * 记录 DEBUG 级别日志
     */
    fun debug(tag: String, message: String) {
        addLog(LogLevel.DEBUG, tag, message)
    }
    
    /**
     * 记录 ERROR 级别日志
     */
    fun error(tag: String, message: String) {
        addLog(LogLevel.ERROR, tag, message)
    }
    
    /**
     * 记录 SUCCESS 级别日志
     */
    fun success(tag: String, message: String) {
        addLog(LogLevel.SUCCESS, tag, message)
    }
    
    /**
     * 记录 WARNING 级别日志
     */
    fun warning(tag: String, message: String) {
        addLog(LogLevel.WARNING, tag, message)
    }
    
    /**
     * 记录 FATAL 级别日志
     */
    fun fatal(tag: String, message: String, throwable: String? = null) {
        addLog(LogLevel.FATAL, tag, message, throwable)
    }
    
    /**
     * 追加日志（兼容方法）
     */
    fun appendLog(message: String) {
        addLog(LogLevel.INFO, "System", message)
    }
    
    /**
     * 清空日志
     */
    fun clearLogs() {
        _logs.value = emptyList()
    }
    
    /**
     * 获取日志数量
     */
    fun getLogCount(): Int = _logs.value.size
    
    /**
     * 读取所有日志
     */
    fun readLogs(): List<LogEntry> = _logs.value
    
    /**
     * 创建可分享文件
     */
    suspend fun createShareableFile(context: Context) {
        // 简化实现，仅记录日志
        addLog(LogLevel.INFO, "System", "日志导出功能已调用")
    }
    
    /**
     * 获取日志大小
     */
    suspend fun getLogSize(): String = "${_logs.value.size} 条日志"
}