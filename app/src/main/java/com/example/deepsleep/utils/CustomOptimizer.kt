package com.example.deepsleep.utils

import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomOptimizer {

    suspend fun applyLightPowerSaving(whitelist: List<String> = emptyList()): Boolean = withContext(Dispatchers.IO) {
        val commands = mutableListOf<String>()
        commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
        commands.addAll(OptimizationCommands.CpuScheduler.applyDefaultMode())
        whitelist.forEach { app -> commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(app)) }
        RootCommander.execBatch(commands).isSuccess
    }

    suspend fun applyMediumPowerSaving(whitelist: List<String> = emptyList()): Boolean = withContext(Dispatchers.IO) {
        val commands = mutableListOf<String>()
        commands.addAll(OptimizationCommands.DeepDoze.disableMotion())
        commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
        commands.addAll(OptimizationCommands.CpuScheduler.applyStandbyMode())
        whitelist.forEach { app -> commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(app)) }
        commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(100))
        commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))
        RootCommander.execBatch(commands).isSuccess
    }

    suspend fun applyHeavyPowerSaving(whitelist: List<String> = emptyList()): Boolean = withContext(Dispatchers.IO) {
        val commands = mutableListOf<String>()
        commands.addAll(OptimizationCommands.DeepDoze.disableMotion())
        commands.addAll(OptimizationCommands.DeepDoze.forceIdle())
        commands.addAll(OptimizationCommands.CpuScheduler.applyStandbyMode())
        whitelist.forEach { app -> commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(app)) }
        commands.addAll(OptimizationCommands.PowerSaver.enable())
        commands.addAll(OptimizationCommands.NetworkOptimizer.disableBluetooth())
        commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(50))
        commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))
        RootCommander.execBatch(commands).isSuccess
    }

    suspend fun applyGamingMode(): Boolean = withContext(Dispatchers.IO) {
        val commands = mutableListOf<String>()
        commands.addAll(OptimizationCommands.DeepDoze.unforceIdle())
        commands.addAll(OptimizationCommands.DeepDoze.enableMotion())
        commands.addAll(OptimizationCommands.CpuScheduler.applyPerformanceMode())
        commands.addAll(OptimizationCommands.PowerSaver.disable())
        RootCommander.execBatch(commands).isSuccess
    }

    suspend fun applyOfficeMode(whitelist: List<String> = emptyList()): Boolean = withContext(Dispatchers.IO) {
        val commands = mutableListOf<String>()
        commands.addAll(OptimizationCommands.CpuScheduler.applyDefaultMode())
        whitelist.forEach { app -> commands.addAll(OptimizationCommands.BackgroundOptimizer.batchOptimizeApps(app)) }
        commands.addAll(OptimizationCommands.ScreenOptimizer.setBrightness(180))
        commands.addAll(OptimizationCommands.ScreenOptimizer.setAutoBrightness(false))
        RootCommander.execBatch(commands).isSuccess
    }

    suspend fun applyThermalProtection(threshold: Int = 70): Boolean = withContext(Dispatchers.IO) {
        val tempResult = RootCommander.exec(OptimizationCommands.ThermalOptimizer.getCpuTemperature())
        val temp = tempResult.out.firstOrNull()?.toIntOrNull() ?: 0
        val tempCelsius = temp / 1000.0
        if (tempCelsius > threshold) {
            RootCommander.exec(OptimizationCommands.ThermalOptimizer.limitMaxFreq(2, 1200000)).isSuccess
        } else true
    }

    suspend fun apply(mode: String, whitelist: List<String> = emptyList()): Boolean = when (mode.lowercase()) {
        "light" -> applyLightPowerSaving(whitelist)
        "medium" -> applyMediumPowerSaving(whitelist)
        "heavy" -> applyHeavyPowerSaving(whitelist)
        "gaming" -> applyGamingMode()
        "office" -> applyOfficeMode(whitelist)
        else -> false
    }
}