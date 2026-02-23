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

        fun getAllUserProcesses(): List<String> {
            return listOf(
                "ls /proc | grep -E '^[0-9]+$'"
            )
        }

        fun getProcessUid(pid: String): List<String> {
            return listOf(
                "cat /proc/$pid/status | grep '^Uid:' | awk '{print \\$2}'"
            )
        }

        fun getProcessCmdline(pid: String): List<String> {
            return listOf(
                "cat /proc/$pid/cmdline"
            )
        }

        fun setOomScore(pid: String, oomValue: Int): List<String> {
            return listOf(
                "echo $oomValue > /proc/$pid/oom_score_adj"
            )
        }

        fun batchSetOomScore(pids: List<String>, oomValue: Int): List<String> {
            return pids.map { pid ->
                "echo $oomValue > /proc/$pid/oom_score_adj 2>/dev/null || true"
            }
        }

        fun pauseProcess(pid: String): List<String> {
            return listOf(
                "kill -STOP $pid"
            )
        }

        fun resumeProcess(pid: String): List<String> {
            return listOf(
                "kill -CONT $pid"
            )
        }

        fun killProcess(pid: String): List<String> {
            return listOf(
                "kill -9 $pid"
            )
        }

        fun getProcessMemory(pid: String): List<String> {
            return listOf(
                "cat /proc/$pid/status | grep -E '^(VmSize|VmRSS|VmPeak):'"
            )
        }

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

        fun listThirdPartyApps(): List<String> {
            return listOf(
                "pm list packages -3 | sed 's/package://g'"
            )
        }

        fun denyBackgroundRun(packageName: String): List<String> {
            return listOf(
                "appops set $packageName RUN_ANY_IN_BACKGROUND deny"
            )
        }

        fun allowBackgroundRun(packageName: String): List<String> {
            return listOf(
                "appops set $packageName RUN_ANY_IN_BACKGROUND default"
            )
        }

        fun ignoreWakeLock(packageName: String): List<String> {
            return listOf(
                "appops set $packageName WAKE_LOCK ignore"
            )
        }

        fun allowWakeLock(packageName: String): List<String> {
            return listOf(
                "appops set $packageName WAKE_LOCK default"
            )
        }

        fun setStandbyBucket(packageName: String, bucket: String): List<String> {
            return listOf(
                "pm set-app-standby-bucket $packageName $bucket"
            )
        }

        fun setRareBucket(packageName: String): List<String> {
            return setStandbyBucket(packageName, "rare")
        }

        fun setRestrictedBucket(packageName: String): List<String> {
            return setStandbyBucket(packageName, "restricted")
        }

        fun setActiveBucket(packageName: String): List<String> {
            return setStandbyBucket(packageName, "active")
        }

        /**
         * 批量优化所有第三方应用
         * @param whitelist 白名单包名列表（这些应用不会被优化）
         */
        fun batchOptimizeApps(whitelist: List<String>): List<String> {
            val commands = mutableListOf<String>()

            // 修复：转义美元符号
            commands.add("PACKAGES=\${'$'}(pm list packages -3 | sed 's/package://g')")

            // 生成优化命令
            whitelist.forEach { pkg ->
                commands.add("PACKAGES=\${'$'}(echo \${'$'}PACKAGES | grep -v '^$pkg$')")
            }

            commands.add("""
for pkg in \${'$'}PACKAGES; do
    appops set \${'$'}pkg RUN_ANY_IN_BACKGROUND deny 2>/dev/null || true
    appops set \${'$'}pkg WAKE_LOCK ignore 2>/dev/null || true
    pm set-app-standby-bucket \${'$'}pkg rare 2>/dev/null || true
done
            """.trimIndent())

            return commands
        }

        /**
         * 批量恢复所有应用
         */
        fun batchRestoreApps(): List<String> {
            return listOf("""
PACKAGES=\${'$'}(pm list packages -3 | sed 's/package://g')
for pkg in \${'$'}PACKAGES; do
    appops set \${'$'}pkg RUN_ANY_IN_BACKGROUND default 2>/dev/null || true
    appops set \${'$'}pkg WAKE_LOCK default 2>/dev/null || true
    pm set-app-standby-bucket \${'$'}pkg active 2>/dev/null || true
done
            """.trimIndent())
        }
    }

    // ============================================
    // 3. 深度 Doze 优化
    // ============================================
    object DeepDoze {

        fun getDozeState(): List<String> {
            return listOf(
                "dumpsys deviceidle | grep 'mState=' | head -1"
            )
        }

        fun forceIdle(): List<String> {
            return listOf(
                "dumpsys deviceidle force-idle"
            )
        }

        fun unforceIdle(): List<String> {
            return listOf(
                "dumpsys deviceidle unforce"
            )
        }

        fun disableMotion(): List<String> {
            return listOf(
                "dumpsys deviceidle disable motion"
            )
        }

        fun enableMotion(): List<String> {
            return listOf(
                "dumpsys deviceidle enable motion"
            )
        }

        fun getMotionState(): List<String> {
            return listOf(
                "dumpsys deviceidle enabled motion 2>&1"
            )
        }

        fun stepToIdleMaintenance(): List<String> {
            return listOf(
                "dumpsys deviceidle step"
            )
        }

        fun addToWhitelist(packageName: String): List<String> {
            return listOf(
                "dumpsys deviceidle whitelist +$packageName"
            )
        }

        fun removeFromWhitelist(packageName: String): List<String> {
            return listOf(
                "dumpsys deviceidle whitelist -$packageName"
            )
        }

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

        fun setWaltParam(cluster: Int, param: String, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/$param"
            )
        }

        fun setUpRateLimit(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/up_rate_limit_us"
            )
        }

        fun setDownRateLimit(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/down_rate_limit_us"
            )
        }

        fun setHiSpeedLoad(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/hispeed_load"
            )
        }

        fun setTargetLoad(cluster: Int, value: Int): List<String> {
            return listOf(
                "echo $value > /sys/devices/system/cpu/cpufreq/policy${cluster}walt/target_loads"
            )
        }

        fun setMinFreq(cluster: Int, frequency: Int): List<String> {
            return listOf(
                "echo $frequency > /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_min_freq"
            )
        }

        fun setMaxFreq(cluster: Int, frequency: Int): List<String> {
            return listOf(
                "echo $frequency > /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_max_freq"
            )
        }

        fun setGovernor(cluster: Int, governor: String): List<String> {
            return listOf(
                "echo $governor > /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_governor"
            )
        }

        fun getGovernor(cluster: Int): List<String> {
            return listOf(
                "cat /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_governor"
            )
        }

        fun getAvailableFrequencies(cluster: Int): List<String> {
            return listOf(
                "cat /sys/devices/system/cpu/cpufreq/policy${cluster}/scaling_available_frequencies"
            )
        }

        fun applyPerformanceMode(): List<String> {
            return listOf(
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/down_rate_limit_us",
                "echo 75 > /sys/devices/system/cpu/cpufreq/policy0walt/hispeed_load",
                "echo 70 > /sys/devices/system/cpu/cpufreq/policy0walt/target_loads",

                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/down_rate_limit_us",
                "echo 75 > /sys/devices/system/cpu/cpufreq/policy1walt/hispeed_load",
                "echo 70 > /sys/devices/system/cpu/cpufreq/policy1walt/target_loads",

                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/down_rate_limit_us",
                "echo 75 > /sys/devices/system/cpu/cpufreq/policy2walt/hispeed_load",
                "echo 70 > /sys/devices/system/cpu/cpufreq/policy2walt/target_loads",

                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy1/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy2/scaling_governor"
            )
        }

        fun applyStandbyMode(): List<String> {
            return listOf(
                "echo 5000 > /sys/devices/system/cpu/cpufreq/policy0walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/down_rate_limit_us",
                "echo 95 > /sys/devices/system/cpu/cpufreq/policy0walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy0walt/target_loads",

                "echo 5000 > /sys/devices/system/cpu/cpufreq/policy1walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/down_rate_limit_us",
                "echo 95 > /sys/devices/system/cpu/cpufreq/policy1walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy1walt/target_loads",

                "echo 5000 > /sys/devices/system/cpu/cpufreq/policy2walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/down_rate_limit_us",
                "echo 95 > /sys/devices/system/cpu/cpufreq/policy2walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy2walt/target_loads",

                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy1/scaling_governor",
                "echo schedutil > /sys/devices/system/cpu/cpufreq/policy2/scaling_governor"
            )
        }

        fun applyDefaultMode(): List<String> {
            return listOf(
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy0walt/down_rate_limit_us",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy0walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy0walt/target_loads",

                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy1walt/down_rate_limit_us",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy1walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy1walt/target_loads",

                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/up_rate_limit_us",
                "echo 0 > /sys/devices/system/cpu/cpufreq/policy2walt/down_rate_limit_us",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy2walt/hispeed_load",
                "echo 90 > /sys/devices/system/cpu/cpufreq/policy2walt/target_loads",

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

        fun enable(): List<String> {
            return listOf(
                "settings put global low_power 1",
                "cmd battery set level 20"
            )
        }

        fun disable(): List<String> {
            return listOf(
                "settings put global low_power 0",
                "cmd battery reset"
            )
        }

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

        fun disableMobileData(): List<String> {
            return listOf(
                "svc data disable"
            )
        }

        fun enableMobileData(): List<String> {
            return listOf(
                "svc data enable"
            )
        }

        fun disableWifi(): List<String> {
            return listOf(
                "svc wifi disable"
            )
        }

        fun enableWifi(): List<String> {
            return listOf(
                "svc wifi enable"
            )
        }

        fun disableBluetooth(): List<String> {
            return listOf(
                "service call bluetooth_manager 6"
            )
        }

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

        fun setBrightness(brightness: Int): List<String> {
            return listOf(
                "settings put system screen_brightness $brightness"
            )
        }

        fun setAutoBrightness(enabled: Boolean): List<String> {
            return listOf(
                "settings put system screen_brightness_mode ${if (enabled) 1 else 0}"
            )
        }

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

        fun getCpuTemperature(): List<String> {
            return listOf(
                "cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null | sort -n | tail -1"
            )
        }

        fun getThermalStatus(): List<String> {
            return listOf(
                "dumpsys thermalservice"
            )
        }

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

        fun triggerMemoryReclaim(): List<String> {
            return listOf(
                "echo 3 > /proc/sys/vm/drop_caches"
            )
        }

        fun getMemoryInfo(): List<String> {
            return listOf(
                "cat /proc/meminfo | grep -E '^(MemTotal|MemFree|MemAvailable|Cached):'"
            )
        }

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

        fun applyExtremePowerSaving(): List<String> {
            val commands = mutableListOf<String>()

            commands.addAll(DeepDoze.disableMotion())
            commands.addAll(DeepDoze.forceIdle())
            commands.addAll(CpuScheduler.applyStandbyMode())
            commands.addAll(BackgroundOptimizer.batchOptimizeApps(emptyList()))
            commands.addAll(PowerSaver.enable())
            commands.addAll(ScreenOptimizer.setBrightness(50))
            commands.addAll(ScreenOptimizer.setAutoBrightness(false))
            commands.addAll(NetworkOptimizer.disableBluetooth())

            return commands
        }

        fun applyPerformanceMode(): List<String> {
            val commands = mutableListOf<String>()

            commands.addAll(DeepDoze.unforceIdle())
            commands.addAll(DeepDoze.enableMotion())
            commands.addAll(CpuScheduler.applyPerformanceMode())
            commands.addAll(BackgroundOptimizer.batchOptimizeApps(emptyList()))
            commands.addAll(PowerSaver.disable())

            return commands
        }

        fun applyDefaultMode(): List<String> {
            val commands = mutableListOf<String>()

            commands.addAll(DeepDoze.unforceIdle())
            commands.addAll(DeepDoze.enableMotion())
            commands.addAll(CpuScheduler.applyDefaultMode())
            commands.addAll(BackgroundOptimizer.batchRestoreApps())
            commands.addAll(PowerSaver.disable())
            commands.addAll(ScreenOptimizer.setAutoBrightness(true))
            commands.addAll(NetworkOptimizer.enableMobileData())
            commands.addAll(NetworkOptimizer.enableWifi())

            return commands
        }
    }
}