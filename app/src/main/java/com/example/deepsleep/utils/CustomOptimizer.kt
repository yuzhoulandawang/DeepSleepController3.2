package com.example.deepsleep.utils

import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 自定义优化方案示例
 * 演示如何组合不同的优化命令
 */
class CustomOptimizer {
    
    /**
     * 方案1：轻度省电（日常使用）
     * 优化内容：
     * - 深度 Doze（启用运动检测）
     * - CPU 日常模式
     * - 后台应用优化
     */
    suspend fun applyLightPowerSaving(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()
            
            // 深度 Doze（保留运动检测）
            commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
            
            // CPU 日常模式
            commands.addAll(OptimizationCommands.CpuScheduler.applyDefaultMode())
            
            // 后台应用优化（保留白名单）
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))
            
            // 执行命令
            val result = RootCommander.execBatch(commands)
            result.isSuccess
        }
    }
    
    /**
     * 方案2：中度省电（通勤/休息）
     * 优化内容：
     * - 深度 Doze（禁用运动检测）
     * - CPU 待机模式
     * - 后台应用优化
     * - 降低屏幕亮度
     */
    suspend fun applyMediumPowerSaving(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()
            
            // 深度 Doze（禁用运动检测）
            commands.addAll(OptimizationCommands.DeepDoze.disableMotion())
            commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
            
            // CPU 待机模式
            commands.addAll(OptimizationCommands.CpuScheduler.applyStandbyMode())
            
            // 后台应用优化
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))
            
            // 降低屏幕亮度
            commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(100))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))
            
            // 执行命令
            val result = RootCommander.execBatch(commands)
            result.isSuccess
        }
    }
    
    /**
     * 方案3：重度省电（夜间待机）
     * 优化内容：
     * - 深度 Doze（禁用运动检测）
     * - CPU 待机模式
     * - 后台应用优化
     * - 系统省电模式
     * - 关闭蓝牙
     * - 降低屏幕亮度
     */
    suspend fun applyHeavyPowerSaving(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()
            
            // 深度 Doze（禁用运动检测）
            commands.addAll(OptimizationCommands.DeepDoze.disableMotion())
            commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
            
            // CPU 待机模式
            commands.addAll(OptimizationCommands.CpuScheduler.applyStandbyMode())
            
            // 后台应用优化
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))
            
            // 系统省电模式
            commands.addAll(OptimizationCommands.PowerSaver.enable())
            
            // 关闭蓝牙
            commands.addAll(OptimizationCommands.NetworkOptimizer.disableBluetooth())
            
            // 降低屏幕亮度
            commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(50))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))
            
            // 执行命令
            val result = RootCommander.execBatch(commands)
            result.isSuccess
        }
    }
    
    /**
     * 方案4：游戏模式（最高性能）
     * 优化内容：
     * - 退出 Doze
     * - CPU 性能模式
     * - 后台应用优化（减少干扰）
     * - 关闭系统省电模式
     */
    suspend fun applyGamingMode(): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()
            
            // 退出 Doze
            commands.addAll(OptimizationCommands.DeepDoze.unforceIdle())
            commands.addAll(OptimizationCommands.DeepDoze.enableMotion())
            
            // CPU 性能模式
            commands.addAll(OptimizationCommands.CpuScheduler.applyPerformanceMode())
            
            // 后台应用优化（减少后台干扰）
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(emptyList()))
            
            // 关闭系统省电模式
            commands.addAll(OptimizationCommands.PowerSaver.disable())
            
            // 执行命令
            val result = RootCommander.execBatch(commands)
            result.isSuccess
        }
    }
    
    /**
     * 方案5：办公模式（平衡性能与续航）
     * 优化内容：
     * - CPU 日常模式
     * - 后台应用优化
     * - 适中的屏幕亮度
     */
    suspend fun applyOfficeMode(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()
            
            // CPU 日常模式
            commands.addAll(OptimizationCommands.CpuScheduler.applyDefaultMode())
            
            // 后台应用优化
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))
            
            // 适中的屏幕亮度
            commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(180))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))
            
            // 执行命令
            val result = RootCommander.execBatch(commands)
            result.isSuccess
        }
    }
    
    /**
     * 恢复默认设置
     */
    suspend fun restoreDefaults(): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()
            
            // 恢复 Doze
            commands.addAll(OptimizationCommands.DeepDoze.unforceIdle())
            commands.addAll(OptimizationCommands.DeepDoze.enableMotion())
            
            // 恢复 CPU
            commands.addAll(OptimizationCommands.CpuScheduler.applyDefaultMode())
            
            // 恢复后台应用
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchRestoreApps())
            
            // 恢复省电模式
            commands.addAll(OptimizationCommands.PowerSaver.disable())
            
            // 恢复屏幕设置
            commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(true))
            
            // 恢复网络
            commands.addAll(OptimizationCommands.NetworkOptimizer.enableMobileData())
            commands.addAll(OptimizationCommands.NetworkOptimizer.enableWifi())
            
            // 执行命令
            val result = RootCommander.execBatch(commands)
            result.isSuccess
        }
    }
    
    /**
     * 智能温度保护
     * 当 CPU 温度超过阈值时自动降低频率
     */
    suspend fun applyThermalProtection(threshold: Int = 70): Boolean {
        return withContext(Dispatchers.IO) {
            // 获取温度
            val tempCommands = OptimizationCommands.ThermalOptimizer.getCpuTemperature()
            val result = RootCommander.exec(tempCommands)
            
            val temp = result.out.firstOrNull()?.toIntOrNull() ?: 0
            val tempCelsius = temp / 1000.0 // 转换为摄氏度
            
            if (tempCelsius > threshold) {
                // 温度过高，限制大核频率
                val commands = OptimizationCommands.ThermalOptimizer.limitMaxFreq(2, 1200000)
                RootCommander.exec(commands).isSuccess
            } else {
                // 温度正常，无需操作
                true
            }
        }
    }
}
    
    /**
     * 应用自定义优化方案
     * @param mode 优化模式: "light", "medium", "heavy", "gaming", "office"
     * @param whitelist 白名单
     */
    suspend fun apply(mode: String, whitelist: List<String> = emptyList()): Boolean {
        return when (mode.lowercase()) {
            "light" -> applyLightPowerSaving(whitelist)
            "medium" -> applyMediumPowerSaving(whitelist)
            "heavy" -> applyHeavyPowerSaving(whitelist)
            "gaming" -> applyGamingMode()
            "office" -> applyOfficeMode(whitelist)
            else -> false
        }
    }
