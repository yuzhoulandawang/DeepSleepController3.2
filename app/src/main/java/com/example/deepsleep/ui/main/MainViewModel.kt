package com.example.deepsleep.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.root.RootCommander
import com.example.deepsleep.root.DozeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val logRepository = LogRepository
    private val settingsRepository = SettingsRepository(context)
    private val statsRepository = StatsRepository
    
    // Root 权限状态
    private val _hasRoot = MutableStateFlow(false)
    val hasRoot: StateFlow<Boolean> = _hasRoot.asStateFlow()
    
    // 别名,用于UI组件兼容
    val rootAvailable: StateFlow<Boolean> = _hasRoot
    
    // Root 权限检查状态
    private val _isCheckingRoot = MutableStateFlow(false)
    val isCheckingRoot: StateFlow<Boolean> = _isCheckingRoot.asStateFlow()
    
    // UI 状态
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // CPU 参数状态
    private val _cpuParams = MutableStateFlow(CpuParamsState())
    val cpuParams: StateFlow<CpuParamsState> = _cpuParams.asStateFlow()
    
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, AppSettings())
    
    init {
        refreshRootStatus()
    }
    
    /**
     * 刷新 Root 权限状态
     */
    fun refreshRootStatus() {
        if (_isCheckingRoot.value) return
        
        viewModelScope.launch {
            _isCheckingRoot.value = true
            try {
                val hasRootAccess = RootCommander.checkRoot()
                _hasRoot.value = hasRootAccess
                
                if (hasRootAccess) {
                    logRepository.info("Root权限", "Root 权限已验证")
                } else {
                    logRepository.error("Root权限", "Root 权限未获取，请手动授权")
                }
            } catch (e: Exception) {
                _hasRoot.value = false
                logRepository.error("Root权限", "Root 权限检查失败: ${e.message}")
            } finally {
                _isCheckingRoot.value = false
            }
        }
    }
    
    /** 
     * 获取详细的 Root 权限信息
     */
    suspend fun getRootInfo() = RootCommander.getRootInfo()
    
    // ========== 深度 Doze ==========
    fun setDeepDozeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepDozeEnabled(enabled)
            logRepository.info("深度 Doze 已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setDeepDozeDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setDeepDozeDelaySeconds(seconds)
            logRepository.info("深度 Doze 延迟时间已设置为: $seconds 秒")
        }
    }
    
    fun setDeepDozeForceMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepDozeForceMode(enabled)
            logRepository.info("深度 Doze 强制模式已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    // ========== 深度睡眠 Hook ==========
    fun setDeepSleepHookEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepHookEnabled(enabled)
            _uiState.value = _uiState.value.copy(deepSleepHookEnabled = enabled)
            logRepository.info("深度睡眠 Hook 已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setDeepSleepDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepDelaySeconds(seconds)
            logRepository.info("深度睡眠延迟时间已设置为: $seconds 秒")
        }
    }
    
    fun setDeepSleepBlockExit(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepBlockExit(enabled)
            logRepository.info("深度睡眠阻止自动退出已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setDeepSleepCheckInterval(interval: Int) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepCheckInterval(interval)
            logRepository.info("深度睡眠状态检查间隔已设置为: $interval 秒")
        }
    }
    
    // ========== 系统省电模式联动 ==========
    fun setEnablePowerSaverOnSleep(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEnablePowerSaverOnSleep(enabled)
            logRepository.info("进入深度睡眠时开启省电模式已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setDisablePowerSaverOnWake(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDisablePowerSaverOnWake(enabled)
            logRepository.info("退出深度睡眠时关闭省电模式已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    // ========== CPU 调度优化 ==========
    fun setCpuOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCpuOptimizationEnabled(enabled)
            _uiState.value = _uiState.value.copy(cpuOptEnabled = enabled)
            logRepository.info("CPU 调度优化已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setCpuModeOnScreen(mode: String) {
        viewModelScope.launch {
            settingsRepository.setCpuModeOnScreen(mode)
            val modeName = getCpuModeName(mode)
            logRepository.info("亮屏 CPU 模式已设置为: $modeName")
        }
    }
    
    fun setCpuModeOnScreenOff(mode: String) {
        viewModelScope.launch {
            settingsRepository.setCpuModeOnScreenOff(mode)
            val modeName = getCpuModeName(mode)
            logRepository.info("息屏 CPU 模式已设置为: $modeName")
        }
    }
    
    fun setAutoSwitchCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSwitchCpuMode(enabled)
            logRepository.info("自动切换 CPU 模式已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setAllowManualCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowManualCpuMode(enabled)
            logRepository.info("手动切换 CPU 模式已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    // CPU 参数 - 日常模式
    fun setDailyUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyUpRateLimit(value)
            logRepository.info("日常模式上行限频: $value%")
        }
    }
    
    fun setDailyDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyDownRateLimit(value)
            logRepository.info("日常模式下行限频: $value%")
        }
    }
    
    fun setDailyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyHiSpeedLoad(value)
            logRepository.info("日常模式高频负载: $value%")
        }
    }
    
    fun setDailyTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyTargetLoads(value)
            logRepository.info("日常模式目标负载: $value%")
        }
    }
    
    // CPU 参数 - 待机模式
    fun setStandbyUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyUpRateLimit(value)
            logRepository.info("待机模式上行限频: $value%")
        }
    }
    
    fun setStandbyDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyDownRateLimit(value)
            logRepository.info("待机模式下行限频: $value%")
        }
    }
    
    fun setStandbyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyHiSpeedLoad(value)
            logRepository.info("待机模式高频负载: $value%")
        }
    }
    
    fun setStandbyTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyTargetLoads(value)
            logRepository.info("待机模式目标负载: $value%")
        }
    }
    
    // CPU 参数 - 默认模式
    fun setDefaultUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultUpRateLimit(value)
            logRepository.info("默认模式上行限频: $value%")
        }
    }
    
    fun setDefaultDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultDownRateLimit(value)
            logRepository.info("默认模式下行限频: $value%")
        }
    }
    
    fun setDefaultHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultHiSpeedLoad(value)
            logRepository.info("默认模式高频负载: $value%")
        }
    }
    
    fun setDefaultTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultTargetLoads(value)
            logRepository.info("默认模式目标负载: $value%")
        }
    }
    
    // CPU 参数 - 性能模式
    fun setPerfUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfUpRateLimit(value)
            logRepository.info("性能模式上行限频: $value%")
        }
    }
    
    fun setPerfDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfDownRateLimit(value)
            logRepository.info("性能模式下行限频: $value%")
        }
    }
    
    fun setPerfHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfHiSpeedLoad(value)
            logRepository.info("性能模式高频负载: $value%")
        }
    }
    
    fun setPerfTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfTargetLoads(value)
            logRepository.info("性能模式目标负载: $value%")
        }
    }
    
    // ========== 进程压制 ==========
    fun setSuppressEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSuppressEnabled(enabled)
            _uiState.value = _uiState.value.copy(processSuppressEnabled = enabled)
            logRepository.info("进程压制已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setSuppressMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setSuppressMode(mode)
            val modeName = when (mode) {
                "conservative" -> "保守"
                "aggressive" -> "激进"
                else -> mode
            }
            logRepository.info("压制模式已设置为: $modeName")
        }
    }
    
    fun setDebounceInterval(interval: Int) {
        viewModelScope.launch {
            settingsRepository.setDebounceInterval(interval)
            logRepository.info("防抖间隔已设置为: $interval 秒")
        }
    }
    
    fun setSuppressInterval(interval: Int) {
        viewModelScope.launch {
            settingsRepository.setSuppressInterval(interval)
            logRepository.info("压制间隔已设置为: $interval 秒")
        }
    }
    
    fun setSuppressOomValue(value: Int) {
        viewModelScope.launch {
            settingsRepository.setSuppressOomValue(value)
            logRepository.info("OOM 值已设置为: $value")
        }
    }
    
    // ========== 后台优化 ==========
    fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackgroundOptimizationEnabled(enabled)
            _uiState.value = _uiState.value.copy(backgroundOptEnabled = enabled)
            logRepository.info("后台优化已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    // ========== 其他 ==========
    fun setBootStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBootStartEnabled(enabled)
            logRepository.info("开机自启动已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            logRepository.info("通知已${if (enabled) "启用" else "禁用"}")
        }
    }
    
    fun clearLogs() {
        viewModelScope.launch {
            logRepository.clearLogs()
            logRepository.info("日志已清除")
        }
    }
    
    // ========== Toggle 方法 ==========
    fun toggleDeepSleep() {
        viewModelScope.launch {
            val currentState = settings.value.deepSleepEnabled
            val newState = !currentState
            settingsRepository.setDeepSleepHookEnabled(newState)
            _uiState.value = _uiState.value.copy(deepSleepHookEnabled = newState)
            logRepository.info("深度睡眠已${if (newState) "启用" else "禁用"}")
        }
    }
    
    fun toggleDoze() {
        viewModelScope.launch {
            val currentState = settings.value.deepDozeEnabled
            val newState = !currentState
            settingsRepository.setDeepDozeEnabled(newState)
            _uiState.value = _uiState.value.copy(dozeEnabled = newState)
            logRepository.info("Doze 模式已${if (newState) "启用" else "禁用"}")
        }
    }
    
    fun toggleProcessSuppress() {
        viewModelScope.launch {
            val currentState = settings.value.suppressEnabled
            val newState = !currentState
            settingsRepository.setSuppressEnabled(newState)
            _uiState.value = _uiState.value.copy(processSuppressEnabled = newState)
            logRepository.info("进程压制已${if (newState) "启用" else "禁用"}")
        }
    }
    
    fun toggleBackgroundOpt() {
        viewModelScope.launch {
            val currentState = settings.value.backgroundOptimizationEnabled
            val newState = !currentState
            settingsRepository.setBackgroundOptimizationEnabled(newState)
            _uiState.value = _uiState.value.copy(backgroundOptEnabled = newState)
            logRepository.info("后台优化已${if (newState) "启用" else "禁用"}")
        }
    }
    
    fun togglePowerSaver() {
        viewModelScope.launch {
            val currentState = _uiState.value.powerSaverEnabled
            val newState = !currentState
            _uiState.value = _uiState.value.copy(powerSaverEnabled = newState)
            logRepository.info("省电模式已${if (newState) "启用" else "禁用"}")
        }
    }
    
    fun toggleCpuOptimization() {
        viewModelScope.launch {
            val currentState = settings.value.cpuOptimizationEnabled
            val newState = !currentState
            settingsRepository.setCpuOptimizationEnabled(newState)
            _uiState.value = _uiState.value.copy(cpuOptEnabled = newState)
            logRepository.info("CPU 优化已${if (newState) "启用" else "禁用"}")
        }
    }
    
    // ========== 设置等级 ==========
    fun setBackgroundOptLevel(level: Int) {
        viewModelScope.launch {
            settingsRepository.setBackgroundOptLevel(level)
            _uiState.value = _uiState.value.copy(backgroundOptLevel = level)
            logRepository.info("后台优化等级已设置为: $level")
        }
    }
    
    fun setPowerSaverLevel(level: Int) {
        viewModelScope.launch {
            settingsRepository.setPowerSaverLevel(level)
            _uiState.value = _uiState.value.copy(powerSaverLevel = level)
            logRepository.info("省电等级已设置为: $level")
        }
    }
    
    fun setSuppressLevel(level: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(suppressLevel = level)
            logRepository.info("压制等级已设置为: $level")
        }
    }
    
    // ========== 自定义模式 ==========
    fun applyCustomMode(mode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(customMode = mode)
            logRepository.info("应用自定义模式: $mode")
        }
    }
    
    // ========== CPU 控制方法 ==========
    fun setCpuGovernor(governor: String) {
        viewModelScope.launch {
            _cpuParams.value = _cpuParams.value.copy(governor = governor)
            logRepository.info("设置调节器: $governor")
        }
    }
    
    fun setCpuFrequency(min: Int, max: Int) {
        viewModelScope.launch {
            _cpuParams.value = _cpuParams.value.copy(minFreq = min, maxFreq = max)
            logRepository.info("设置频率: ${min}KHz - ${max}KHz")
        }
    }
    
    // ========== Root 控制方法 ==========
    fun requestRoot() {
        viewModelScope.launch {
            _isCheckingRoot.value = true
            try {
                val hasRoot = RootCommander.requestRootAccess()
                _hasRoot.value = hasRoot
                if (hasRoot) {
                    logRepository.info("Root权限", "Root 权限获取成功")
                } else {
                    logRepository.error("Root权限", "Root 权限获取失败")
                }
            } catch (e: Exception) {
                _hasRoot.value = false
                logRepository.error("Root权限", "请求 Root 权限异常: ${e.message}")
            } finally {
                _isCheckingRoot.value = false
            }
        }
    }
    
    fun forceDoze() {
        viewModelScope.launch {
            try {
                logRepository.info("Root权限", "强制进入 Doze 模式")
                DozeController.enterDeepSleep()
            } catch (e: Exception) {
                logRepository.error("Root权限", "强制 Doze 失败: ${e.message}")
            }
        }
    }
    
    private fun getCpuModeName(mode: String): String {
        return when (mode) {
            "daily" -> "日常模式"
            "standby" -> "待机模式"
            "default" -> "默认模式"
            "performance" -> "性能模式"
            else -> mode
        }
    }
    
    // UI 状态数据类
    data class UiState(
        val deepSleepHookEnabled: Boolean = true,
        val deepDozeEnabled: Boolean = true,
        val processSuppressEnabled: Boolean = true,
        val backgroundOptEnabled: Boolean = true,
        val powerSaverEnabled: Boolean = false,
        val batterySaving: Boolean = false,
        val dozeEnabled: Boolean = true,
        val cpuOptEnabled: Boolean = false,
        val customMode: String = "",
        val backgroundOptLevel: Int = 1,
        val powerSaverLevel: Int = 1,
        val suppressLevel: Int = 1
    )
    
    // CPU 参数状态数据类
    data class CpuParamsState(
        val governor: String = "",
        val minFreq: Int = 0,
        val maxFreq: Int = 0
    )
}