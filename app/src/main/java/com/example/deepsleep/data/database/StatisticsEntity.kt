package com.example.deepsleep.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 统计数据实体
 */
@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey
    val id: Int = 1,
    
    // 进入深度睡眠统计
    val totalEnterCount: Int = 0,
    val totalEnterSuccess: Int = 0,
    
    // 退出深度睡眠统计
    val totalExitCount: Int = 0,
    val totalExitSuccess: Int = 0,
    
    // 自动退出统计
    val totalAutoExitCount: Int = 0,
    val totalAutoExitRecover: Int = 0,
    
    // 维护窗口统计
    val totalMaintenanceCount: Int = 0,
    
    // 状态变更统计
    val totalStateChangeCount: Int = 0,
    
    // 服务启动时间
    val serviceStartTime: Long = 0,
    
    // 最后更新时间
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 转换为 Statistics 数据类
 */
fun StatisticsEntity.toStatistics(): com.example.deepsleep.model.Statistics {
    return com.example.deepsleep.model.Statistics(
        totalEnterCount = totalEnterCount,
        totalEnterSuccess = totalEnterSuccess,
        totalExitCount = totalExitCount,
        totalExitSuccess = totalExitSuccess,
        totalAutoExitCount = totalAutoExitCount,
        totalAutoExitRecover = totalAutoExitRecover,
        totalMaintenanceCount = totalMaintenanceCount,
        totalStateChangeCount = totalStateChangeCount,
        serviceStartTime = serviceStartTime
    )
}

/**
 * 从 Statistics 数据类转换
 */
fun com.example.deepsleep.model.Statistics.toEntity(): StatisticsEntity {
    return StatisticsEntity(
        totalEnterCount = totalEnterCount,
        totalEnterSuccess = totalEnterSuccess,
        totalExitCount = totalExitCount,
        totalExitSuccess = totalExitSuccess,
        totalAutoExitCount = totalAutoExitCount,
        totalAutoExitRecover = totalAutoExitRecover,
        totalMaintenanceCount = totalMaintenanceCount,
        totalStateChangeCount = totalStateChangeCount,
        serviceStartTime = serviceStartTime,
        lastUpdateTime = System.currentTimeMillis()
    )
}
