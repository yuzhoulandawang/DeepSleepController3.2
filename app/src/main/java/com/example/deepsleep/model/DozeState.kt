package com.example.deepsleep.model

enum class DozeState {
    ACTIVE,           // 设备活跃
    INACTIVE,         // 设备闲置
    IDLE,             // 进入 Doze 模式
    IDLE_MAINTENANCE, // Doze 维护窗口
    UNKNOWN           // 未知状态
}