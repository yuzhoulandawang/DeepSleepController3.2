package com.example.deepsleep.utils

/**
 * 全局优化命令集合
 * 包含所有优化功能对应的 Root 命令
 */
object OptimizationCommands {

    // ============================================
    // 1. 进程压制优化
    // ============================================
    object ProcessSuppressor {
        
        /**
         * 获取所有用户进程（排除系统进程）
         */
        fun getAllUserProcesses(): List<String> {
            return listOf(
                "ls /proc | grep -E '^[0-9]+$'"
            )
        }
        
        /**
         * 获取进程的 UID
         */
        fun getProcessUid(pid: String): List<String> {
            return listOf(
                "cat /proc/$pid/status | grep '^Uid:' | awk '{print \\$2}'"
            )
        }
        
        /**
         * 获取进程的命令行
         */
        fun getProcessCmdline(pid: String): List<String> {
            return listOf(
                "cat /proc/$pid/cmdline"
            )
        }
        
        /**
         * 设置进程的 OOM 分数（优先级）
         * @param pid 进程 ID
         * @param oomValue OOM 值（范围：-1000 到 1000，数值越大越容易被杀）
         *   -1000: 最不容易被杀（保护进程）
         *   0: 默认值
         *   1000: 最容易被杀（低优先级）
         */
        fun setOomScore(pid: String, oomValue: Int): List<String> {
            return listOf(
                "echo $oomValue > /proc/$pid/oom_score_adj"
            )
        }
        
        /**
         * 批量设置进程 OOM 分数
         * @param pids 进程 ID 列表
         * @param oomValue OOM 值
         */
        fun batchSetOomScore(pids: List<String>, oomValue: Int): List<String> {
            return pids.map { pid ->
                "echo $oomValue > /proc/$pid/oom_score_adj 2>/dev/null || true"
            }
        }
        
        /**
         * 暂停进程（不推荐，可能导致应用崩溃）
         */
        fun pauseProcess(pid: String): List<String> {
            return listOf(
                "kill -STOP $pid"
            )
        }
        
        /**
         * 恢复进程
         */
        fun resumeProcess(pid: String): List<String> {
            return listOf(
                "kill -CONT $pid"
            )
        }
        
        /**
         * 终止进程（强制杀掉）
         */
        fun killProcess(pid: String): List<String> {
            return listOf(
                "kill -9 $pid"
            )
        }
        
        /**
         * 获取进程的内存使用情况
         */
        fun getProcessMemory(pid: String): List<String> {
            return listOf(
                "cat /proc/$pid/status | grep -E '^(VmSize|VmRSS|VmPeak):'"
            )
        }
        
        /**
         * 获取进程的 CPU 使用情况
         */
        fun getProcessCpu(pid: String): List<String> {
            return listOf(
                "cat /proc/$pid/stat"
            )
        }
    }

    // ============================================
    // 2. 后台应用优化
    // ============================================
    object BackgroundOptimizer {
        
        /**
         * 列出所有已安装的第三方应用
         */
        fun listThirdPartyApps(): List<String> {
            return listOf(
                "pm list packages -3 | sed 's/package://g'"
            )
        }
        
        /**
         * 禁止应用在后台运行（RUN_ANY_IN_BACKGROUND）
         * @param packageName 应用包名
         */
        fun denyBackgroundRun(packageName: String): List<String> {
            return listOf(
                "appops set $packageName RUN_ANY_IN_BACKGROUND deny"
            )
        }
        
        /**
         * 允许应用在后台运行
         */
        fun allowBackgroundRun(packageName: String): List<String> {
            return listOf(
                "appops set $packageName RUN_ANY_IN_BACKGROUND default"
            )
        }
        
        /**
         * 忽略应用的 Wake Lock 权限（防止应用保持唤醒）
         * @param packageName 应用包名
         */
        fun ignoreWakeLock(packageName: String): List<String> {
            return listOf(
                "appops set $packageName WAKE_LOCK ignore"
            )
        }
        
        /**
         * 恢复应用的 Wake Lock 权限
         */
        fun allowWakeLock(packageName: String): List<String> {
            return listOf(
                "appops set $packageName WAKE_LOCK default"
            )
        }
        
        /**
         * 设置应用待机桶（Standby Bucket）
         * @param packageName 应用包名
         * @param bucket 待机桶级别：
         *   - active: 活跃（最优先）
         *   - working_set: 工作集
         *   - frequent: 频繁使用
         *   - rare: 稀少使用（最严格）
         *   - restricted: 受限（最极端）
         */
        fun setStandbyBucket(packageName: String, bucket: String): List<String> {
            return listOf(
                "pm set-app-standby-bucket $packageName $bucket"
            )
        }
        
        /**
         * 将应用设置为稀少使用（最严格的后台限制）
         */
        fun setRareBucket(packageName: String): List<String> {
            return setStandbyBucket(packageName, "rare")
        }
        
        /**
         * 将应用设置为受限模式（最极端的限制）
         */
        fun setRestrictedBucket(packageName: String): List<String> {
            return setStandbyBucket(packageName, "restricted")
        }
        
        /**
         * 恢复应用到活跃状态
         */
        fun setActiveBucket(packageName: String): List<String> {
            return setStandbyBucket(packageName, "active")
        }
        
        /**
         * 批量优化所有第三方应用
         * @param whitelist 白名单包名列表（这些应用不会被优化）
         */
        fun batchOptimizeApps(whitelist: List<String>): List<String> {
            val commands = mutableListOf<String>()
            
            // 获取所有第三方应用
            commands.add("PACKAGES=\\$(pm list packages -3 | sed 's/package://g')")
            
            // 生成优化命令
            whitelist.forEach { pkg ->
                commands.add("PACKAGES=\\$(echo \\$PACKAGES | grep -v '^$pkg$')")
            }
            
            commands.add("""
for pkg in \\$PACKAGES; do
    appops set \\$pkg RUN_ANY_IN_BACKGROUND deny 2>/dev/null || true
    appops set \\$pkg WAKE_LOCK ignore 2>/dev/null || true
    pm set-app-standby-bucket \\$pkg rare 2>/dev/null || true
done
            """.trimIndent())
            
            return commands
        }
        
        /**
         * 批量恢复所有应用
         */
        fun batchRestoreApps(): List<String> {
            return listOf("""
PACKAGES=\\$(pm list packages -3 | sed 's/package://g')
for pkg in \\$PACKAGES; do
    appops set \\$pkg RUN_ANY_IN_BACKGROUND default 2>/dev/null || true
    appops set \\$pkg WAKE_LOCK default 2>/dev/null || true
    pm set-app-standby-bucket \\$pkg active 2>/dev/null || true
done
            """.trimIndent())
        }
    }

    // ============================================
    // 3. 深度 Doze 优化
    // ============================================
    object DeepDoze {
        
        /**
         * 获取当前 Doze 状态
         */
        fun getDozeState(): List<String> {
            return listOf(
                "dumpsys deviceidle | grep 'mState=' | head -1"
            )
        }
        
        /**
         * 强制进入 Doze 模式（深度睡眠）
         */
        fun forceIdle(): List<String> {
            return listOf(
                "dumpsys deviceidle force-idle"
            )
        }
        
        /**
         * 取消强制 Doze 模式
         */
        fun unforceIdle(): List<String> {
            return listOf(
                "dumpsys deviceidle unforce"
            )
        }
        
        /**
         * 禁用运动检测（强制进入 Doze）
         * 这样即使设备在移动，也能进入 Doze 模式
         */
        fun disableMotion(): List<String> {
            return listOf(
                "dumpsys deviceidle disable motion"
            )
        }
        
        /**
         * 启用运动检测
         */
        fun enableMotion(): List<String> {
            return listOf(
                "dumpsys deviceidle enable motion"
            )
        }
        
        /**
         * 获取运动检测状态
         */
        fun getMotionState(): List<String> {
            return listOf(
                "dumpsys deviceidle enabled motion 2>&1"
            )
        }
        
        /**
         * 让设备进入空闲维护模式（允许执行后台任务）
         */
        fun stepToIdleMaintenance(): List<String> {
            return listOf(
                "dumpsys deviceidle step"
            )
        }
        
        /**
         * 添加应用到 Doze 白名单（应用不会受 Doze 限制）
         * @param packageName 应用包名
         */
        fun addToWhitelist(packageName: String): List<String> {
            return listOf(
                "dumpsys deviceidle whitelist +$packageName"
            )
        }
        
        /**
         * 从 Doze 白名单中移除应用
         */
        fun removeFromWhitelist(packageName: String): List<String> {
            return listOf(
                "dumpsys deviceidle whitelist -$packageName"
            )
        }
        
        /**
         * 列出所有 Doze 白名单应用
         */
        fun listWhitelist(): List<String> {
            return listOf(
                "dumpsys deviceidle whitelist"
            )
        }
    }

    // ============================================
    // 4. CPU 调度优化（WALT）
    // ============================================
    object CpuScheduler {
        
        /**
         * 设置 WALT 调度器参数
         * @param cluster CPU 集群（0: 小核, 1: 中核, 2: 大核）
         * @param param 参数名
         * @param value 参数值
         */
        fun setWaltParam(cluster: Int, param: String, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/$param"
            )
        }
        
        /**
         * 设置频率上升限制（us）
         * 值越小，CPU 频率上升越快（性能优先）
         * 值越大，CPU 频率上升越慢（省电优先）
         */
        fun setUpRateLimit(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/up_rate_limit_us"
            )
        }
        
        /**
         * 设置频率下降限制（us）
         * 值越小，CPU 频率下降越快（省电优先）
         * 值越大，CPU 频率下降越慢（性能优先）
         */
        fun setDownRateLimit(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/down_rate_limit_us"
            )
        }
        
        /**
         * 设置高速频率触发负载（%）
         * 值越小，更容易提升到高频（性能优先）
         * 值越大，更难提升到高频（省电优先）
         */
        fun setHiSpeedLoad(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/hispeed_load"
            )
        }
        
        /**
         * 设置目标负载（%）
         * 调度器会尝试将 CPU 负载维持在这个水平
         */
        fun setTargetLoad(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/target_loads"
            )
        }
        
        /**
         * 设置最小频率
         * @param cluster CPU 集群
         * @param frequency 频率值（单位：KHz）
         */
        fun setMinFreq(cluster: Int, frequency: Int): List<String> {
            return listOf(
                "echo $frequency > /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_min_freq"
            )
        }
        
        /**
         * 设置最大频率
         */
        fun setMaxFreq(cluster: Int, frequency: Int): List<String> {
            return listOf(
                "echo $frequency > /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_max_freq"
            )
        }
        
        /**
         * 设置调速器（Governor）
         * 常见的调速器：
         * - performance: 性能模式（始终最高频率）
         * - powersave: 省电模式（始终最低频率）
         * - schedutil: 智能调度（推荐）
         * - interactive: 交互模式
         */
        fun setGovernor(cluster: Int, governor: String): List<String> {
            return listOf(
                "echo $governor > /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_governor"
            )
        }
        
        /**
         * 获取当前调速器
         */
        fun getGovernor(cluster: Int): List<String> {
            return listOf(
                "cat /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_governor"
            )
        }
        
        /**
         * 获取可用频率列表
         */
        fun getAvailableFrequencies(cluster: Int): List<String> {
            return listOf(
                "cat /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_available_frequencies"
            )
        }
        
        /**
         * 应用性能模式配置
         */
        fun applyPerformanceMode(): List<String> {
            return listOf(
                # 小核（cluster 0）
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/down_rate_limit_us",
                "echo 75 > /sys/devices/system/cpu/cpufreq/policy0walt/hispeed_load",
                "echo 70 > /sys/devices/system/cpu/cpufreq/policy0walt/target_loads",
                
                # 中核（cluster 1）
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/down_rate_limit_us",
                "echo 75 > /sys/devices/system/cpu/cpufreq/policy1walt/hispeed_load",
                "echo 70 > /sys/devices/system/cpu/cpufreq/policy1walt/target_loads",
                
                # 大核（cluster 2）
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/down_rate_limit_us",
                "echo 75 > /sys/devices/system/cpu/cpufreq/policy2walt/hispeed_load",
                "echo 70 > /sys/devices/system/cpu/cpufreq/policy2walt/target_loads",
                
                # 设置调速器为 schedutil
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy1/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy2/scaling_governor"
            )
        }
        
        /**
         * 应用待机模式配置
         */
        fun applyStandbyMode(): List<String> {
            return listOf(
                # 小核（cluster 0）
                "echo 5000 > /sys/devices/system/cpu/cpufreq/policy0walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/down_rate_limit_us",
                "echo 95 > /sys/devices/system/cpu/cpufreq/policy0walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy0walt/target_loads",
                
                # 中核（cluster 1）
                "echo 5000 > /sys/devices/system/cpu/cpufreq/policy1walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/down_rate_limit_us",
                "echo 95 > /sys/devices/system/cpu/cpufreq/policy1walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy1walt/target_loads",
                
                # 大核（cluster 2）
                "echo 5000 > /sys/devices/system/cpu/cpufreq/policy2walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/down_rate_limit_us",
                "echo 95 > /sys/devices/system/cpu/cpufreq/policy2walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy2walt/target_loads",
                
                # 设置调速器为 schedutil
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy1/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy2/scaling_governor"
            )
        }
        
        /**
         * 恢复默认配置
         */
        fun applyDefaultMode(): List<String> {
            return listOf(
                # 小核（cluster 0）
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/down_rate_limit_us",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy0walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy0walt/target_loads",
                
                # 中核（cluster 1）
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/down_rate_limit_us",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy1walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy1walt/target_loads",
                
                # 大核（cluster 2）
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/down_rate_limit_us",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy2walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy2walt/target_loads",
                
                # 恢复默认调速器
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy1/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy2/scaling_governor"
            )
        }
    }

    // ============================================
    // 5. 系统省电模式
    // ============================================
    object PowerSaver {
        
        /**
         * 开启系统省电模式
         */
        fun enable(): List<String> {
            return listOf(
                "settings put global low_power 1",
                "cmd battery set level 20"
            )
        }
        
        /**
         * 关闭系统省电模式
         */
        fun disable(): List<String> {
            return listOf(
                "settings put global low_power 0",
                "cmd battery reset"
            )
        }
        
        /**
         * 获取省电模式状态
         */
        fun getStatus(): List<String> {
            return listOf(
                "settings get global low_power"
            )
        }
    }

    // ============================================
    // 6. 网络优化
    // ============================================
    object NetworkOptimizer {
        
        /**
         * 禁用移动数据（用于深度睡眠）
         */
        fun disableMobileData(): List<String> {
            return listOf(
                "svc data disable"
            )
        }
        
        /**
         * 启用移动数据
         */
        fun enableMobileData(): List<String> {
            return listOf(
                "svc data enable"
            )
        }
        
        /**
         * 禁用 Wi-Fi
         */
        fun disableWifi(): List<String> {
            return listOf(
                "svc wifi disable"
            )
        }
        
        /**
         * 启用 Wi-Fi
         */
        fun enableWifi(): List<String> {
            return listOf(
                "svc wifi enable"
            )
        }
        
        /**
         * 禁用蓝牙
         */
        fun disableBluetooth(): List<String> {
            return listOf(
                "service call bluetooth_manager 6"
            )
        }
        
        /**
         * 启用蓝牙
         */
        fun enableBluetooth(): List<String> {
            return listOf(
                "service call bluetooth_manager 8"
            )
        }
    }

    // ============================================
    // 7. 屏幕优化
    // ============================================
    object ScreenOptimizer {
        
        /**
         * 降低屏幕亮度
         * @param brightness 亮度值（0-255）
         */
        fun setBrightness(brightness: Int): List<String> {
            return listOf(
                "settings put system screen_brightness $brightness"
            )
        }
        
        /**
         * 设置自动亮度
         */
        fun setAutoBrightness(enabled: Boolean): List<String> {
            return listOf(
                "settings put system screen_brightness_mode ${if (enabled) 1 else 0}"
            )
        }
        
        /**
         * 设置屏幕超时时间（秒）
         */
        fun setScreenTimeout(seconds: Int): List<String> {
            return listOf(
                "settings put system screen_off_timeout ${seconds * 1000}"
            )
        }
    }

    // ============================================
    // 8. 温度优化（过热保护）
    // ============================================
    object ThermalOptimizer {
        
        /**
         * 获取 CPU 温度
         */
        fun getCpuTemperature(): List<String> {
            return listOf(
                "cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null | sort -n | tail -1"
            )
        }
        
        /**
         * 获取温度状态
         */
        fun getThermalStatus(): List<String> {
            return listOf(
                "dumpsys thermalservice"
            )
        }
        
        /**
         * 限制 CPU 最大频率（过热时使用）
         * @param cluster CPU 集群
         * @param frequency 频率限制（KHz）
         */
        fun limitMaxFreq(cluster: Int, frequency: Int): List<String> {
            return listOf(
                "echo $frequency > /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_max_freq"
            )
        }
    }

    // ============================================
    // 9. 内存优化
    // ============================================
    object MemoryOptimizer {
        
        /**
         * 触发内存回收（清理缓存）
         */
        fun triggerMemoryReclaim(): List<String> {
            return listOf(
                "echo 3 > /proc/sys/vm/drop_caches"
            )
        }
        
        /**
         * 获取内存使用情况
         */
        fun getMemoryInfo(): List<String> {
            return listOf(
                "cat /proc/meminfo | grep -E '^(MemTotal|MemFree|MemAvailable|Cached):'"
            )
        }
        
        /**
         * 获取 Swap 使用情况
         */
        fun getSwapInfo(): List<String> {
            return listOf(
                "cat /proc/swaps",
                "cat /proc/meminfo | grep -E '^Swap'"
            )
        }
    }

    // ============================================
    // 10. 综合优化方案
    // ============================================
    object ComprehensiveOptimizer {
        
        /**
         * 极致省电模式（夜间使用）
         * 包含：深度 Doze + CPU 降频 + 后台限制 + 网络关闭
         */
        fun applyExtremePowerSaving(): List<String> {
            val commands = mutableListOf<String>()
            
            // 深度 Doze
            commands.addAll(DeepDoze.disableMotion())
            commands.addAll(DeepDoze.forceIdle())
            
            // CPU 待机模式
            commands.addAll(CpuScheduler.applyStandbyMode())
            
            // 后台应用优化
            commands.addAll(BackgroundOptimizer.batchOptimizeApps(emptyList()))
            
            // 系统省电模式
            commands.addAll(PowerSaver.enable())
            
            // 降低屏幕亮度
            commands.addAll(ScreenOptimizer.setBrightness(50))
            commands.addAll(ScreenOptimizer.setAutoBrightness(false))
            
            // 网络优化（可选）
            commands.addAll(NetworkOptimizer.disableBluetooth())
            
            return commands
        }
        
        /**
         * 性能模式（游戏使用）
         * 包含：CPU 高频 + 禁用 Doze + 后台限制
         */
        fun applyPerformanceMode(): List<String> {
            val commands = mutableListOf<String>()
            
            // 退出 Doze
            commands.addAll(DeepDoze.unforceIdle())
            commands.addAll(DeepDoze.enableMotion())
            
            // CPU 性能模式
            commands.addAll(CpuScheduler.applyPerformanceMode())
            
            // 后台应用优化（减少后台干扰）
            commands.addAll(BackgroundOptimizer.batchOptimizeApps(emptyList()))
            
            // 关闭系统省电模式
            commands.addAll(PowerSaver.disable())
            
            return commands
        }
        
        /**
         * 恢复默认模式
         */
        fun applyDefaultMode(): List<String> {
            val commands = mutableListOf<String>()
            
            // 恢复 Doze
            commands.addAll(DeepDoze.unforceIdle())
            commands.addAll(DeepDoze.enableMotion())
            
            // CPU 默认模式
            commands.addAll(CpuScheduler.applyDefaultMode())
            
            // 恢复后台应用
            commands.addAll(BackgroundOptimizer.batchRestoreApps())
            
            // 关闭系统省电模式
            commands.addAll(PowerSaver.disable())
            
            // 恢复屏幕设置
            commands.addAll(ScreenOptimizer.setAutoBrightness(true))
            
            // 恢复网络
            commands.addAll(NetworkOptimizer.enableMobileData())
            commands.addAll(NetworkOptimizer.enableWifi())
            
            return commands
        }
    }
}
