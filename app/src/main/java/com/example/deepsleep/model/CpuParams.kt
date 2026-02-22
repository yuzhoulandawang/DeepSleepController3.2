package com.example.deepsleep.model

/**
 * CPU 参数配置数据模型
 */
data class CpuParams(
    // WALT 参数
    val utilThreshold: Int = 500,
    val utilEstFactor: Int = 100,
    val utilFilter: Int = 50,
    val windowSize: Int = 20,
    val decayRate: Int = 10,
    val minLoad: Int = 10,

    // CPU 频率参数
    val minFreq: Int = 300000,      // 最小频率 (300 MHz)
    val maxFreq: Int = 2457600,     // 最大频率 (2457.6 MHz)
    val scalingGovernor: String = "schedutil",

    // 调度器参数
    val upThreshold: Int = 85,
    val downThreshold: Int = 30,
    val samplingRate: Int = 10000   // 采样率 (10ms)
) {
    companion object {
        // 预设配置
        val DAILY_MODE = CpuParams(
            utilThreshold = 500,
            utilEstFactor = 100,
            utilFilter = 50,
            windowSize = 20,
            decayRate = 10,
            minLoad = 10,
            minFreq = 800000,
            maxFreq = 2457600,
            scalingGovernor = "schedutil",
            upThreshold = 85,
            downThreshold = 30,
            samplingRate = 10000
        )

        val STANDBY_MODE = CpuParams(
            utilThreshold = 300,
            utilEstFactor = 80,
            utilFilter = 30,
            windowSize = 15,
            decayRate = 15,
            minLoad = 5,
            minFreq = 300000,
            maxFreq = 1200000,
            scalingGovernor = "conservative",
            upThreshold = 70,
            downThreshold = 20,
            samplingRate = 20000
        )

        val PERFORMANCE_MODE = CpuParams(
            utilThreshold = 600,
            utilEstFactor = 120,
            utilFilter = 70,
            windowSize = 25,
            decayRate = 5,
            minLoad = 20,
            minFreq = 1200000,
            maxFreq = 2457600,
            scalingGovernor = "performance",
            upThreshold = 95,
            downThreshold = 40,
            samplingRate = 5000
        )

        val DEFAULT_MODE = CpuParams(
            utilThreshold = 500,
            utilEstFactor = 100,
            utilFilter = 50,
            windowSize = 20,
            decayRate = 10,
            minLoad = 10,
            minFreq = 500000,
            maxFreq = 2000000,
            scalingGovernor = "schedutil",
            upThreshold = 80,
            downThreshold = 25,
            samplingRate = 10000
        )
    }

    /**
     * 验证参数是否有效
     */
    fun isValid(): Boolean {
        return utilThreshold in 0..1000 &&
               utilEstFactor in 0..200 &&
               utilFilter in 0..100 &&
               windowSize in 5..50 &&
               decayRate in 1..20 &&
               minLoad in 0..100 &&
               minFreq > 0 &&
               maxFreq > minFreq &&
               upThreshold in 0..100 &&
               downThreshold in 0..upThreshold &&
               samplingRate in 1000..50000
    }

    /**
     * 获取参数描述
     */
    fun getDescription(): String {
        return """
            CPU 参数配置:
            - 最小频率: ${minFreq / 1000} MHz
            - 最大频率: ${maxFreq / 1000} MHz
            - 调度器: $scalingGovernor
            - 上升阈值: $upThreshold%
            - 下降阈值: $downThreshold%
            - 采样率: ${samplingRate / 1000} ms
            - WALT 阈值: $utilThreshold
            - WALT 估算因子: $utilEstFactor%
        """.trimIndent()
    }
}

/**
 * CPU 调度器类型
 */
enum class CpuGovernor(val displayName: String, val description: String) {
    PERFORMANCE("性能模式", "CPU 始终运行在最高频率，性能最强但功耗最高"),
    SCHEDUTIL("调度优化", "根据任务调度需求动态调整，平衡性能和功耗"),
    CONSERVATIVE("保守模式", "CPU 频率缓慢变化，功耗较低但性能稍差"),
    POWERSAVE("省电模式", "CPU 始终运行在最低频率，功耗最低但性能最差"),
    ONDEMAND("按需模式", "根据负载快速调整，响应快但功耗较高"),
    USERSPACE("用户模式", "完全由用户控制频率");

    companion object {
        fun fromString(value: String): CpuGovernor {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: SCHEDUTIL
        }
    }
}
