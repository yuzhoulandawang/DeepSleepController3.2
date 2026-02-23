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
     */
    suspend fun applyLightPowerSaving(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()

            commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
            commands.addAll(OptimizationCommands.CpuScheduler.applyDefaultMode())
            // 直接传递整个列表
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))

            RootCommander.execBatch(commands).isSuccess
        }
    }

    /**
     * 方案2：中度省电（通勤/休息）
     */
    suspend fun applyMediumPowerSaving(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()

            commands.addAll(OptimizationCommands.DeepDoze.disableMotion())
            commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
            commands.addAll(OptimizationCommands.CpuScheduler.applyStandbyMode())
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(100))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))

            RootCommander.execBatch(commands).isSuccess
        }
    }

    /**
     * 方案3：重度省电（夜间待机）
     */
    suspend fun applyHeavyPowerSaving(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()

            commands.addAll(OptimizationCommands.DeepDoze.disableMotion())
            commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
            commands.addAll(OptimizationCommands.CpuScheduler.applyStandbyMode())
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))
            commands.addAll(OptimizationCommands.PowerSaver.enable())
            commands.addAll(OptimizationCommands.NetworkOptimizer.disableBluetooth())
            commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(50))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))

            RootCommander.execBatch(commands).isSuccess
        }
    }

    /**
     * 方案4：游戏模式（最高性能）
     */
    suspend fun applyGamingMode(): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()

            commands.addAll(OptimizationCommands.DeepDoze.unforceIdle())
            commands.addAll(OptimizationCommands.DeepDoze.enableMotion())
            commands.addAll(OptimizationCommands.CpuScheduler.applyPerformanceMode())
            commands.addAll(OptimizationCommands.PowerSaver.disable())

            RootCommander.execBatch(commands).isSuccess
        }
    }

    /**
     * 方案5：办公模式（平衡性能与续航）
     */
    suspend fun applyOfficeMode(whitelist: List<String> = emptyList()): Boolean {
        return withContext(Dispatchers.IO) {
            val commands = mutableListOf<String>()

            commands.addAll(OptimizationCommands.CpuScheduler.applyDefaultMode())
            commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(whitelist))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(180))
            commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))

            RootCommander.execBatch(commands).isSuccess
        }
    }

    /**
     * 智能温度保护
     */
    suspend fun applyThermalProtection(threshold: Int = 70): Boolean {
        return withContext(Dispatchers.IO) {
            val tempCommands = OptimizationCommands.ThermalOptimizer.getCpuTemperature()
            val result = RootCommander.exec(tempCommands)
            val temp = result.out.firstOrNull()?.toIntOrNull() ?: 0
            val tempCelsius = temp / 1000.0

            if (tempCelsius > threshold) {
                val commands = OptimizationCommands.ThermalOptimizer.limitMaxFreq(2, 1200000)
                RootCommander.exec(commands).isSuccess
            } else {
                true
            }
        }
    }

    /**
     * 应用自定义优化方案
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
}