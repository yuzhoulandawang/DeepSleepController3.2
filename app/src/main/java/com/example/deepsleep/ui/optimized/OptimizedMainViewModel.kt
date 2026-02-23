package com.example.deepsleep.ui.optimized

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.core.performance.monitorPerformance
import com.example.deepsleep.core.result.Result
import com.example.deepsleep.core.result.ErrorCode
import com.example.deepsleep.core.result.rootError
import com.example.deepsleep.core.security.SecurityValidator
import com.example.deepsleep.data.LogRepository  // 添加导入
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.root.RootCommander
import com.example.deepsleep.root.DozeController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 优化后的 MainViewModel
 * 使用 Hilt 依赖注入、统一错误处理、性能监控
 */
@HiltViewModel
class OptimizedMainViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository,
    private val rootCommander: RootCommander,
    private val dozeController: DozeController,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // ========== UI 状态 ==========

    private val _uiState = MutableStateFlow(OptimizedUiState())
    val uiState: StateFlow<OptimizedUiState> = _uiState.asStateFlow()

    // ========== Root 权限状态 ==========

    private val _rootState = MutableStateFlow(RootState())
    val rootState: StateFlow<RootState> = _rootState.asStateFlow()

    // ========== 设置数据流 ==========

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Lazily, AppSettings())

    init {
        refreshRootStatus()
    }

    // ========== Root 权限管理 ==========

    /**
     * 刷新 Root 权限状态（带性能监控和错误处理）
     */
    fun refreshRootStatus() {
        if (_uiState.value.isCheckingRoot) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingRoot = true)

            monitorPerformance("refreshRootStatus") {
                Result.catch {
                    // 使用增强的安全验证
                    val verificationInfo = SecurityValidator.getRootVerificationDetails()

                    _rootState.value = _rootState.value.copy(
                        hasRoot = verificationInfo.isVerified,
                        verificationInfo = verificationInfo,
                        lastCheckTime = System.currentTimeMillis()
                    )

                    Result.success(verificationInfo)
                }.onError { message, throwable ->
                    _rootState.value = _rootState.value.copy(
                        hasRoot = false,
                        errorMessage = message
                    )
                    rootError(message, throwable)
                }
            }

            _uiState.value = _uiState.value.copy(isCheckingRoot = false)
        }
    }

    /**
     * 请求 Root 权限
     */
    fun requestRoot() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingRoot = true)

            monitorPerformance("requestRoot") {
                Result.catch {
                    val granted = rootCommander.requestRootAccess()

                    if (granted) {
                        // 双重验证
                        val verified = SecurityValidator.verifyRoot()
                        _rootState.value = _rootState.value.copy(
                            hasRoot = verified,
                            lastCheckTime = System.currentTimeMillis()
                        )
                        Result.success(verified)
                    } else {
                        Result.error("Root 权限请求失败", null, ErrorCode.ROOT_PERMISSION_DENIED)
                    }
                }.onError { message, throwable ->
                    _uiState.value = _uiState.value.copy(
                        hasRoot = false,
                        errorMessage = message
                    )
                    rootError(message, throwable)
                }
            }

            _uiState.value = _uiState.value.copy(isCheckingRoot = false)
        }
    }

    /**
     * 强制进入 Doze 模式
     */
    fun forceDoze() {
        viewModelScope.launch {
            monitorPerformance("forceDoze") {
                Result.catch {
                    if (!_rootState.value.hasRoot) {
                        return@catch Result.error("未获取 Root 权限", null, ErrorCode.ROOT_NOT_AVAILABLE)
                    }

                    val success = dozeController.enterDeepSleep()

                    if (success) {
                        statsRepository.recordEnterAttempt()
                        statsRepository.recordEnterSuccess()
                        Result.success(true)
                    } else {
                        Result.error("进入 Doze 模式失败", null, ErrorCode.DOZE_ENTER_FAILED)
                    }
                }.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "已强制进入 Doze 模式"
                    )
                }.onError { message, _ ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = message
                    )
                }
            }
        }
    }

    // ========== 设置管理 ==========

    /**
     * 切换深度睡眠开关
     */
    fun toggleDeepSleep() {
        viewModelScope.launch {
            monitorPerformance("toggleDeepSleep") {
                Result.catch {
                    val currentEnabled = settings.value.deepSleepHookEnabled
                    settingsRepository.setDeepSleepHookEnabled(!currentEnabled)

                    _uiState.value = _uiState.value.copy(
                        toastMessage = if (!currentEnabled) "深度睡眠已启用" else "深度睡眠已禁用"
                    )

                    Result.success(!currentEnabled)
                }
            }
        }
    }

    /**
     * 更新深度睡眠延迟时间
     */
    fun setDeepSleepDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            monitorPerformance("setDeepSleepDelaySeconds") {
                Result.catch {
                    if (!SecurityValidator.isValidRange(seconds, 0, 300)) {
                        return@catch Result.error("延迟时间必须在 0-300 秒之间", null)
                    }

                    settingsRepository.setDeepSleepDelaySeconds(seconds)
                    Result.success(seconds)
                }.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "延迟时间已设置为 $seconds 秒"
                    )
                }.onError { message, _ ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = message
                    )
                }
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 清除提示消息
     */
    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    /**
     * 清除日志
     * 修复：改为 LogRepository.clearLogs()
     */
    fun clearLogs() {
        viewModelScope.launch {
            monitorPerformance("clearLogs") {
                Result.catch {
                    LogRepository.clearLogs()   // 原为 settingsRepository.clearLogs()
                    Result.success(Unit)
                }
            }
        }
    }
}

/**
 * 优化后的 UI 状态
 */
data class OptimizedUiState(
    val isCheckingRoot: Boolean = false,
    val errorMessage: String? = null,
    val toastMessage: String? = null
)

/**
 * Root 权限状态
 */
data class RootState(
    val hasRoot: Boolean = false,
    val verificationInfo: com.example.deepsleep.core.security.RootVerificationInfo? = null,
    val lastCheckTime: Long = 0,
    val errorMessage: String? = null
)