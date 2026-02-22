package com.example.deepsleep.root

import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.utils.Logger

object BackgroundOptimizer {
    private const val TAG = "BackgroundOptimizer"
    
    private val DEFAULT_BG_WHITELIST = listOf(
        "com.android.deskclock",
        "com.android.contacts",
        "com.android.dialer",
        "com.android.inputmethod.latin",
        "com.google.android.inputmethod.latin",
        "com.android.providers.downloads",
        "com.google.android.apps.messaging",
        "com.whatsapp",
        "com.tencent.mm",
        "com.tencent.mobileqq",
        "com.eg.android.AlipayGphone"
    )
    
    /**
     * 优化所有后台应用
     */
    suspend fun optimizeAll(customWhitelist: List<String> = emptyList()) {
        try {
            val whitelist = DEFAULT_BG_WHITELIST + customWhitelist
            
            val packages = RootCommander.exec(
                "pm list packages -3 | sed 's/package://g'"
            ).out
            
            val commands = mutableListOf<String>()
            
            for (packageName in packages) {
                if (whitelist.contains(packageName)) continue
                
                commands.add("appops set $packageName RUN_ANY_IN_BACKGROUND deny 2>/dev/null || true")
                commands.add("appops set $packageName WAKE_LOCK ignore 2>/dev/null || true")
                commands.add("pm set-app-standby-bucket $packageName rare 2>/dev/null || true")
            }
            
            commands.chunked(50).forEach { batch ->
                RootCommander.execBatch(batch)
            }
            
            Logger.i(TAG, "已优化 ${commands.size / 3} 个后台应用")
        } catch (e: Exception) {
            Logger.e(TAG, "优化后台应用失败: ${e.message}")
        }
    }
    
    /**
     * 恢复单个应用
     */
    suspend fun restoreApp(packageName: String) {
        try {
            RootCommander.exec(
                "appops set $packageName RUN_ANY_IN_BACKGROUND default 2>/dev/null || true",
                "appops set $packageName WAKE_LOCK default 2>/dev/null || true",
                "pm set-app-standby-bucket $packageName active 2>/dev/null || true"
            )
            Logger.i(TAG, "已恢复应用: $packageName")
        } catch (e: Exception) {
            Logger.e(TAG, "恢复应用失败: ${e.message}")
        }
    }
    
    /**
     * 恢复所有应用
     */
    suspend fun restoreAll() {
        try {
            val packages = RootCommander.exec(
                "pm list packages -3 | sed 's/package://g'"
            ).out
            
            val commands = packages.flatMap { packageName ->
                listOf(
                    "appops set $packageName RUN_ANY_IN_BACKGROUND default 2>/dev/null || true",
                    "appops set $packageName WAKE_LOCK default 2>/dev/null || true",
                    "pm set-app-standby-bucket $packageName active 2>/dev/null || true"
                )
            }
            
            commands.chunked(50).forEach { batch ->
                RootCommander.execBatch(batch)
            }
            
            Logger.i(TAG, "已恢复所有应用")
        } catch (e: Exception) {
            Logger.e(TAG, "恢复所有应用失败: ${e.message}")
        }
    }
    
    /**
     * 启用后台优化
     */
    suspend fun enableBackgroundOpt(): Boolean {
        return try {
            val commands = listOf(
                "settings put global background_throttle_enabled 1 2>/dev/null",
                "settings put global app_standby_enabled 1 2>/dev/null"
            )
            RootCommander.execBatch(commands).isSuccess
        } catch (e: Exception) {
            Logger.e(TAG, "启用后台优化失败: ${e.message}")
            false
        }
    }
    
    /**
     * 禁用后台优化
     */
    suspend fun disableBackgroundOpt(): Boolean {
        return try {
            val commands = listOf(
                "settings put global background_throttle_enabled 0 2>/dev/null",
                "settings put global app_standby_enabled 0 2>/dev/null"
            )
            RootCommander.execBatch(commands).isSuccess
        } catch (e: Exception) {
            Logger.e(TAG, "禁用后台优化失败: ${e.message}")
            false
        }
    }
    
    /**
     * 设置优化等级 (1-5)
     */
    suspend fun setOptimizationLevel(level: Int): Boolean {
        return try {
            val threshold = when (level) {
                1 -> "0.3"
                2 -> "0.5"
                3 -> "0.7"
                4 -> "0.85"
                5 -> "0.95"
                else -> return false
            }
            RootCommander.exec("settings put global background_throttle_threshold $threshold 2>/dev/null").isSuccess
        } catch (e: Exception) {
            Logger.e(TAG, "设置优化等级失败: ${e.message}")
            false
        }
    }
}