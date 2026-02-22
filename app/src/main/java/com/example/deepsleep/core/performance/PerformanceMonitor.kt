package com.example.deepsleep.core.performance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

/**
 * 性能监控器
 * 用于监控操作耗时和性能指标
 */
object PerformanceMonitor {
    
    private val metrics = mutableMapOf<String, MetricData>()
    
    private data class MetricData(
        val totalTime: Long,
        val count: Int,
        val minTime: Long,
        val maxTime: Long
    ) {
        fun addTime(time: Long): MetricData {
            return copy(
                totalTime = totalTime + time,
                count = count + 1,
                minTime = minOf(minTime, time),
                maxTime = maxOf(maxTime, time)
            )
        }
        
        fun averageTime(): Long = if (count > 0) totalTime / count else 0
    }
    
    /**
     * 监控操作执行时间
     */
    suspend fun <T> monitor(
        name: String,
        block: suspend () -> T
    ): T {
        val startTime = System.nanoTime()
        return try {
            val result = block()
            val endTime = System.nanoTime()
            val duration = endTime - startTime
            
            synchronized(metrics) {
                metrics[name] = metrics[name]?.addTime(duration) ?: 
                    MetricData(duration, 1, duration, duration)
            }
            
            result
        } catch (e: Exception) {
            val endTime = System.nanoTime()
            val duration = endTime - startTime
            
            synchronized(metrics) {
                metrics[name] = metrics[name]?.addTime(duration) ?: 
                    MetricData(duration, 1, duration, duration)
            }
            
            throw e
        }
    }
    
    /**
     * 监控操作执行时间（带超时）
     */
    suspend fun <T> monitorWithTimeout(
        name: String,
        timeoutMs: Long,
        block: suspend () -> T
    ): Result<T> {
        return try {
            val result = withTimeout(timeoutMs) {
                monitor(name) { block() }
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取性能指标
     */
    fun getMetrics(name: String): PerformanceMetric? {
        val data = synchronized(metrics) { metrics[name] } ?: return null
        return PerformanceMetric(
            name = name,
            averageTime = TimeUnit.NANOSECONDS.toMillis(data.averageTime()),
            minTime = TimeUnit.NANOSECONDS.toMillis(data.minTime),
            maxTime = TimeUnit.NANOSECONDS.toMillis(data.maxTime),
            count = data.count
        )
    }
    
    /**
     * 获取所有性能指标
     */
    fun getAllMetrics(): List<PerformanceMetric> {
        return synchronized(metrics) {
            metrics.map { (name, data) ->
                PerformanceMetric(
                    name = name,
                    averageTime = TimeUnit.NANOSECONDS.toMillis(data.averageTime()),
                    minTime = TimeUnit.NANOSECONDS.toMillis(data.minTime),
                    maxTime = TimeUnit.NANOSECONDS.toMillis(data.maxTime),
                    count = data.count
                )
            }
        }.sortedByDescending { it.count }
    }
    
    /**
     * 清除性能指标
     */
    fun clearMetrics() {
        synchronized(metrics) {
            metrics.clear()
        }
    }
    
    /**
     * 清除指定指标
     */
    fun clearMetric(name: String) {
        synchronized(metrics) {
            metrics.remove(name)
        }
    }
}

/**
 * 性能指标数据类
 */
data class PerformanceMetric(
    val name: String,
    val averageTime: Long, // 毫秒
    val minTime: Long,     // 毫秒
    val maxTime: Long,     // 毫秒
    val count: Int
) {
    override fun toString(): String {
        return "$name: 平均 ${averageTime}ms, 最小 ${minTime}ms, 最大 ${maxTime}ms, 执行 $count 次"
    }
}

/**
 * 监控操作执行时间的扩展函数
 */
suspend fun <T> monitorPerformance(name: String, block: suspend () -> T): T {
    return PerformanceMonitor.monitor(name, block)
}
