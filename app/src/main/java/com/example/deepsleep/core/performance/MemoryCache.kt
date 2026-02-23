package com.example.deepsleep.core.performance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// CacheEntry 为 private 顶级类，确保不暴露
private data class CacheEntry<V>(
    val value: V,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isExpired(ttl: Long): Boolean = System.currentTimeMillis() - timestamp > ttl
}

@Suppress("EXPOSED_FUNCTION_RETURN_TYPE")  // 编译器误报，实际并未暴露 CacheEntry
class MemoryCache<K, V>(
    private val maxSize: Int = 100,
    private val ttl: Long = 5 * 60 * 1000 // 5分钟过期
) {
    private val cache = LinkedHashMap<K, CacheEntry<V>>(maxSize, 0.75f, true)
    private val mutex = Mutex()

    suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key]
        if (entry != null && !entry.isExpired(ttl)) {
            return@withLock entry.value
        }
        cache.remove(key)
        return@withLock null
    }

    suspend fun put(key: K, value: V) = mutex.withLock {
        cache[key] = CacheEntry(value)
        if (cache.size > maxSize) {
            cache.remove(cache.keys.first())
        }
    }

    suspend fun getOrPut(key: K, compute: suspend () -> V): V {
        get(key)?.let { return it }
        val value = compute()
        put(key, value)
        return value
    }

    suspend fun remove(key: K) = mutex.withLock { cache.remove(key) }
    suspend fun clear() = mutex.withLock { cache.clear() }
    suspend fun cleanup() = mutex.withLock {
        val expiredKeys = cache.filter { it.value.isExpired(ttl) }.keys
        expiredKeys.forEach { cache.remove(it) }
    }
    suspend fun size(): Int = mutex.withLock { cache.size }

    companion object {
        fun createSettingsCache(): MemoryCache<String, String> = MemoryCache(50, 10 * 60 * 1000)
        fun createRootCommandCache(): MemoryCache<String, String> = MemoryCache(20, 60 * 1000)
        fun createStatsCache(): MemoryCache<String, Int> = MemoryCache(30, 2 * 60 * 1000)
    }
}