package com.example.deepsleep.root

import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.DozeState
import kotlinx.coroutines.delay

object DozeController {
    private const val TAG = "DozeController"
    
    /**
     * 获取当前 Doze 状态
     */
    suspend fun getState(): DozeState {
        return try {
            val output = RootCommander.exec(
                "dumpsys deviceidle | grep 'mState=' | head -1"
            ).out.firstOrNull() ?: ""
            
            when {
                output.contains("ACTIVE") -> DozeState.ACTIVE
                output.contains("INACTIVE") -> DozeState.INACTIVE
                output.contains("IDLE_MAINTENANCE") -> DozeState.IDLE_MAINTENANCE
                output.contains("IDLE") -> DozeState.IDLE
                else -> DozeState.UNKNOWN
            }
        } catch (e: Exception) {
            LogRepository.error(TAG, "获取 Doze 状态失败: ${e.message}")
            DozeState.UNKNOWN
        }
    }
    
    /**
     * 强制进入深度睡眠（异步）
     */
    suspend fun enterDeepSleep(): Boolean {
        return try {
            LogRepository.info(TAG, "强制进入 Doze 模式")
            RootCommander.exec("dumpsys deviceidle force-idle")
            delay(3000)
            getState() == DozeState.IDLE
        } catch (e: Exception) {
            LogRepository.error(TAG, "进入 Doze 失败: ${e.message}")
            false
        }
    }
    
    /**
     * 退出深度睡眠（异步）
     */
    suspend fun exitDeepSleep(): Boolean {
        return try {
            LogRepository.info(TAG, "退出 Doze 模式")
            RootCommander.exec("dumpsys deviceidle unforce")
            delay(2000)
            getState() == DozeState.ACTIVE
        } catch (e: Exception) {
            LogRepository.error(TAG, "退出 Doze 失败: ${e.message}")
            false
        }
    }
    
    /**
     * 禁用运动检测
     */
    suspend fun disableMotion(): Boolean {
        return try {
            RootCommander.exec("dumpsys deviceidle disable motion").isSuccess
        } catch (e: Exception) {
            LogRepository.error(TAG, "禁用运动检测失败: ${e.message}")
            false
        }
    }
    
    /**
     * 启用运动检测
     */
    suspend fun enableMotion(): Boolean {
        return try {
            RootCommander.exec("dumpsys deviceidle enable motion").isSuccess
        } catch (e: Exception) {
            LogRepository.error(TAG, "启用运动检测失败: ${e.message}")
            false
        }
    }
    
    /**
     * 备份运动状态
     */
    suspend fun backupMotionState(): String {
        return try {
            val output = RootCommander.exec(
                "dumpsys deviceidle enabled motion 2>&1"
            ).out.joinToString()
            
            if (output.contains("false", ignoreCase = true)) "disabled" else "enabled"
        } catch (e: Exception) {
            "enabled" // 默认启用
        }
    }
    
    /**
     * 恢复运动状态
     */
    suspend fun restoreMotionState(state: String) {
        try {
            if (state == "disabled") disableMotion() else enableMotion()
        } catch (e: Exception) {
            LogRepository.error(TAG, "恢复运动状态失败: ${e.message}")
        }
    }
    
    /**
     * 单步推进 Doze 状态
     */
    suspend fun step(): Boolean {
        return try {
            RootCommander.exec("dumpsys deviceidle step").isSuccess
        } catch (e: Exception) {
            LogRepository.error(TAG, "推进 Doze 状态失败: ${e.message}")
            false
        }
    }
    
    /**
     * 启用 Doze
     */
    suspend fun enable(): Boolean {
        return try {
            RootCommander.exec("dumpsys deviceidle enable").isSuccess
        } catch (e: Exception) {
            LogRepository.error(TAG, "启用 Doze 失败: ${e.message}")
            false
        }
    }
    
    /**
     * 获取当前闲置状态
     */
    suspend fun getIdleState(): String {
        return try {
            val result = RootCommander.exec("dumpsys deviceidle | grep 'mState'")
            if (result.out.isNotEmpty()) {
                result.out[0].trim()
            } else {
                "UNKNOWN"
            }
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }
}