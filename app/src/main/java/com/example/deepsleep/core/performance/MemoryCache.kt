package com.example.deepsleep.core.performance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

/**
 * 内存缓存管理器
 * 用于减少磁盘 I/O 和网络请求
 */
class MemoryCache<K, V>(
    private val maxSize: Int = 100,
    private val ttl: Long = 5 * 60 * 1000 // 5分钟过期
) {
    private val cache = LinkedHashMap<K, CacheEntry<V>>(maxSize, 0.75f, true)
    private val mutex = Mutex()

    // 修复：改为 internal 避免暴露私有类型
    internal data class CacheEntry<V>(
        val value: V,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttl: Long): Boolean {
            return System.currentTimeMillis() - timestamp > ttl
        }
    }

    /**
     * 获取缓存值
     */
    suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key]
        if (entry != null && !entry.isExpired(ttl)) {
            return@withLock entry.value
        }
        cache.remove(key)
        return@withLock null
    }

    /**
     * 设置缓存值
     */
    suspend fun put(key: K, value: V) = mutex.withLock {
        cache[key] = CacheEntry(value)

        // 如果超过最大容量，移除最旧的项
        if (cache.size > maxSize) {
            cache.remove(cache.keys.first())
        }
    }

    /**
     * 获取或计算缓存值
     */
    suspend fun getOrPut(key: K, compute: suspend () -> V): V {
        get(key)?.let { return it }
        val value = compute()
        put(key, value)
        return value
    }

    /**
     * 移除缓存值
     */
    suspend fun remove(key: K) = mutex.withLock {
        cache.remove(key)
    }

    /**
     * 清空所有缓存
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    /**
     * 清理过期缓存
     */
    suspend fun cleanup() = mutex.withLock {
        val expiredKeys = cache.filter { it.value.isExpired(ttl) }.keys
        expiredKeys.forEach { cache.remove(it) }
    }

    /**
     * 获取缓存大小
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }

    companion object {
        /**
         * 创建设置缓存
         */
        fun createSettingsCache(): MemoryCache<String, String> {
            return MemoryCache(maxSize = 50, ttl = 10 * 60 * 1000) // 10分钟
        }

        /**
         * 创建 Root 命令结果缓存
         */
        fun createRootCommandCache(): MemoryCache<String, String> {
            return MemoryCache(maxSize = 20, ttl = 1 * 60 * 1000) // 1分钟
        }

        /**
         * 创建统计数据缓存
         */
        fun createStatsCache(): MemoryCache<String, Int> {
            return MemoryCache(maxSize = 30, ttl = 2 * 60 * 1000) // 2分钟
        }
    }
}