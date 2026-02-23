package com.example.deepsleep.ui.optimized

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.core.performance.monitorPerformance
import com.example.deepsleep.core.result.Result
import com.example.deepsleep.core.result.ErrorCode
import com.example.deepsleep.core.security.SecurityValidator
import com.example.deepsleep.core.security.RootVerificationInfo
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.root.RootCommander
import com.example.deepsleep.root.DozeController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OptimizedMainViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository,
    private val rootCommander: RootCommander,
    private val dozeController: DozeController,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OptimizedUiState())
    val uiState: StateFlow<OptimizedUiState> = _uiState.asStateFlow()

    private val _rootState = MutableStateFlow(RootState())
    val rootState: StateFlow<RootState> = _rootState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Lazily, AppSettings())

    init {
        refreshRootStatus()
    }

    fun refreshRootStatus() {
        if (_uiState.value.isCheckingRoot) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingRoot = true)
            monitorPerformance("refreshRootStatus") {
                Result.catch<RootVerificationInfo> {
                    val info = SecurityValidator.getRootVerificationDetails()
                    _rootState.value = _rootState.value.copy(
                        hasRoot = info.isVerified,
                        verificationInfo = info,
                        lastCheckTime = System.currentTimeMillis()
                    )
                    Result.success(info)
                }.onError { message, _ ->
                    _rootState.value = _rootState.value.copy(hasRoot = false, errorMessage = message)
                    // 不再调用 rootError，避免返回非 Unit
                }
            }
            _uiState.value = _uiState.value.copy(isCheckingRoot = false)
        }
    }

    fun requestRoot() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingRoot = true)
            monitorPerformance("requestRoot") {
                Result.catch<Boolean> {
                    val granted = rootCommander.requestRootAccess()
                    if (granted) {
                        val verified = SecurityValidator.verifyRoot()
                        _rootState.value = _rootState.value.copy(
                            hasRoot = verified,
                            lastCheckTime = System.currentTimeMillis()
                        )
                        Result.success(verified)
                    } else {
                        // 显式转换为 Result<Boolean>
                        Result.error("Root 权限请求失败", null, ErrorCode.ROOT_PERMISSION_DENIED) as Result<Boolean>
                    }
                }.onError { message, _ ->
                    _rootState.value = _rootState.value.copy(hasRoot = false, errorMessage = message)
                }
            }
            _uiState.value = _uiState.value.copy(isCheckingRoot = false)
        }
    }

    fun forceDoze() {
        viewModelScope.launch {
            monitorPerformance("forceDoze") {
                Result.catch<Boolean> {
                    if (!_rootState.value.hasRoot) {
                        return@catch Result.error("未获取 Root 权限", null, ErrorCode.ROOT_NOT_AVAILABLE) as Result<Boolean>
                    }
                    val success = dozeController.enterDeepSleep()
                    if (success) {
                        statsRepository.recordEnterAttempt()
                        statsRepository.recordEnterSuccess()
                        Result.success(true)
                    } else {
                        Result.error("进入 Doze 模式失败", null, ErrorCode.DOZE_ENTER_FAILED) as Result<Boolean>
                    }
                }.onSuccess {
                    _uiState.value = _uiState.value.copy(toastMessage = "已强制进入 Doze 模式")
                }.onError { message, _ ->
                    _uiState.value = _uiState.value.copy(errorMessage = message)
                }
            }
        }
    }

    fun toggleDeepSleep() {
        viewModelScope.launch {
            monitorPerformance("toggleDeepSleep") {
                Result.catch<Boolean> {
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

    fun setDeepSleepDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            monitorPerformance("setDeepSleepDelaySeconds") {
                Result.catch<Int> {
                    if (!SecurityValidator.isValidRange(seconds, 0, 300)) {
                        return@catch Result.error("延迟时间必须在 0-300 秒之间", null) as Result<Int>
                    }
                    settingsRepository.setDeepSleepDelaySeconds(seconds)
                    Result.success(seconds)
                }.onSuccess {
                    _uiState.value = _uiState.value.copy(toastMessage = "延迟时间已设置为 $seconds 秒")
                }.onError { message, _ ->
                    _uiState.value = _uiState.value.copy(errorMessage = message)
                }
            }
        }
    }

    fun clearErrorMessage() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun clearToastMessage() { _uiState.value = _uiState.value.copy(toastMessage = null) }

    fun clearLogs() {
        viewModelScope.launch {
            monitorPerformance("clearLogs") {
                Result.catch<Unit> {
                    LogRepository.clearLogs()
                    Result.success(Unit)
                }
            }
        }
    }
}

data class OptimizedUiState(
    val isCheckingRoot: Boolean = false,
    val errorMessage: String? = null,
    val toastMessage: String? = null
)

data class RootState(
    val hasRoot: Boolean = false,
    val verificationInfo: RootVerificationInfo? = null,
    val lastCheckTime: Long = 0,
    val errorMessage: String? = null
)