package com.example.deepsleep

import android.app.Application
import com.example.deepsleep.data.database.AppDatabase
import com.example.deepsleep.core.performance.PerformanceMonitor
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用程序入口
 * 使用 Hilt 进行依赖注入
 */
@HiltAndroidApp
class DeepSleepApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Root 权限
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
        
        // 初始化数据库
        initDatabase()
        
        // 初始化性能监控
        PerformanceMonitor
        
        // 初始化缓存清理（定期清理过期缓存）
        scheduleCacheCleanup()
    }
    
    /**
     * 初始化数据库
     */
    private fun initDatabase() {
        // 通过依赖注入获取数据库实例
        // 这里会延迟初始化，避免影响应用启动速度
    }
    
    /**
     * 定期清理缓存
     */
    private fun scheduleCacheCleanup() {
        // 每小时清理一次过期缓存
        // 可以使用 WorkManager 或 AlarmManager 实现
    }
}
