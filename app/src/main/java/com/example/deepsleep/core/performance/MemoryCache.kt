package com.example.deepsleep.core.performance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// 将 CacheEntry 移到外部并设为 private，避免任何暴露可能
private data class CacheEntry<V>(
    val value: V,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isExpired(ttl: Long): Boolean = System.currentTimeMillis() - timestamp > ttl
}

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
    suspend fun remove(key: K) = mutex.withLock { cache.remove(key) }

    /**
     * 清空所有缓存
     */
    suspend fun clear() = mutex.withLock { cache.clear() }

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
    suspend fun size(): Int = mutex.withLock { cache.size }

    companion object {
        fun createSettingsCache(): MemoryCache<String, String> = MemoryCache(50, 10 * 60 * 1000)
        fun createRootCommandCache(): MemoryCache<String, String> = MemoryCache(20, 60 * 1000)
        fun createStatsCache(): MemoryCache<String, Int> = MemoryCache(30, 2 * 60 * 1000)
    }
}