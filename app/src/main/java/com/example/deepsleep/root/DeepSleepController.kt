package com.example.deepsleep.root

import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object DeepSleepController {
    private const val TAG = "DeepSleepController"
    private var isInDeepSleep = false
    private var checkJob: Job? = null
    
    // 进入深度睡眠
    suspend fun enterDeepSleep(
        blockExit: Boolean,
        checkIntervalSeconds: Int
    ): Boolean {
        return try {
            Logger.i(TAG, "准备进入深度睡眠...")
            Logger.d(TAG, "参数: blockExit=$blockExit, checkInterval=$checkIntervalSeconds")
            
            // 1. 进入 Doze 模式
            val dozeSuccess = DozeController.enterDeepSleep()
            if (!dozeSuccess) {
                Logger.e(TAG, "Doze 进入失败")
                return false
            }
            Logger.s(TAG, "Doze 模式已进入")
            
            // 2. 屏蔽自动退出
            if (blockExit) {
                Logger.i(TAG, "屏蔽移动检测...")
                RootCommander.exec("settings put global motion_detection_enabled 0")
                Logger.s(TAG, "移动检测已屏蔽")
                
                Logger.i(TAG, "限制唤醒源...")
                RootCommander.exec("echo 0 > /sys/power/autosleep")
                Logger.s(TAG, "唤醒源已限制")
            }
            
            // 3. 标记状态
            RootCommander.exec("echo 'active' > /data/local/tmp/deep_sleep_status")
            Logger.s(TAG, "深度睡眠已进入")
            
            isInDeepSleep = true
            
            // 4. 启动状态检查
            if (blockExit) {
                startStatusCheck(checkIntervalSeconds)
            }
            
            true
        } catch (e: Exception) {
            Logger.e(TAG, "进入深度睡眠失败: ${e.message}", e)
            false
        }
    }
    
    // 退出深度睡眠
    suspend fun exitDeepSleep(): Boolean {
        return try {
            Logger.i(TAG, "准备退出深度睡眠...")
            
            // 1. 停止状态检查
            checkJob?.cancel()
            checkJob = null
            
            // 2. 恢复移动检测
            Logger.i(TAG, "恢复移动检测...")
            RootCommander.exec("settings put global motion_detection_enabled 1")
            
            // 3. 恢复唤醒源
            Logger.i(TAG, "恢复唤醒源...")
            RootCommander.exec("echo 1 > /sys/power/autosleep")
            
            // 4. 清除标记
            RootCommander.exec("rm -f /data/local/tmp/deep_sleep_status")
            
            // 5. 退出 Doze 模式
            val dozeSuccess = DozeController.exitDeepSleep()
            if (dozeSuccess) {
                Logger.s(TAG, "已退出深度睡眠")
            } else {
                Logger.w(TAG, "退出 Doze 模式失败")
            }
            
            isInDeepSleep = false
            
            true
        } catch (e: Exception) {
            Logger.e(TAG, "退出深度睡眠失败: ${e.message}", e)
            false
        }
    }
    
    // 检查是否在深度睡眠状态
    fun isInDeepSleepState(): Boolean {
        return isInDeepSleep
    }
    
    // 启动状态检查循环
    private fun startStatusCheck(intervalSeconds: Int) {
        checkJob?.cancel()
        checkJob = CoroutineScope(Dispatchers.IO).launch {
            var checkCount = 0
            while (isActive) {
                delay(intervalSeconds * 1000L)
                checkCount++
                
                val isIdle = checkDozeStatus()
                
                if (isIdle) {
                    if (checkCount % 10 == 0) {  // 每10次检查记录一次，避免日志过多
                        Logger.d(TAG, "状态检查 #$checkCount: 正常")
                    }
                } else {
                    Logger.w(TAG, "状态检查 #$checkCount: 检测到意外退出，正在重新进入...")
                    enterDeepSleep(blockExit = true, checkIntervalSeconds = intervalSeconds)
                    Logger.s(TAG, "已重新进入深度睡眠")
                    checkCount = 0  // 重置计数
                }
            }
        }
    }
    
    // 检查 Doze 状态
    private suspend fun checkDozeStatus(): Boolean {
        return try {
            val result = RootCommander.exec("dumpsys deviceidle")
            result.out.any { it.contains("IDLE") }
        } catch (e: Exception) {
            false
        }
    }
    
    // 停止所有检查
    fun stopAll() {
        checkJob?.cancel()
        checkJob = null
        isInDeepSleep = false
    }

    /**
     * 检查当前是否处于深度睡眠状态
     */
    suspend fun isDeepSleeping(): Boolean {
        return try {
            val result = RootCommander.exec("dumpsys deviceidle | grep 'Idle'")
            result.out.any { it.contains("IDLE") || it.contains("true") }
        } catch (e: Exception) {
            false
        }
    }
}