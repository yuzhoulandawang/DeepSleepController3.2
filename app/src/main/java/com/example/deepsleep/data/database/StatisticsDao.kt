package com.example.deepsleep.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 统计数据 DAO
 */
@Dao
interface StatisticsDao {
    
    /**
     * 获取统计数据
     */
    @Query("SELECT * FROM statistics WHERE id = 1")
    fun getStatistics(): Flow<StatisticsEntity?>
    
    /**
     * 获取统计数据（一次性）
     */
    @Query("SELECT * FROM statistics WHERE id = 1")
    suspend fun getStatisticsOnce(): StatisticsEntity?
    
    /**
     * 插入或更新统计数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(statistics: StatisticsEntity)
    
    /**
     * 增加进入尝试次数
     */
    @Query("UPDATE statistics SET totalEnterCount = totalEnterCount + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementEnterCount(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 增加进入成功次数
     */
    @Query("UPDATE statistics SET totalEnterSuccess = totalEnterSuccess + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementEnterSuccess(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 增加退出尝试次数
     */
    @Query("UPDATE statistics SET totalExitCount = totalExitCount + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementExitCount(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 增加退出成功次数
     */
    @Query("UPDATE statistics SET totalExitSuccess = totalExitSuccess + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementExitSuccess(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 增加自动退出次数
     */
    @Query("UPDATE statistics SET totalAutoExitCount = totalAutoExitCount + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementAutoExitCount(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 增加自动恢复次数
     */
    @Query("UPDATE statistics SET totalAutoExitRecover = totalAutoExitRecover + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementAutoExitRecover(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 增加维护窗口次数
     */
    @Query("UPDATE statistics SET totalMaintenanceCount = totalMaintenanceCount + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementMaintenanceCount(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 增加状态变更次数
     */
    @Query("UPDATE statistics SET totalStateChangeCount = totalStateChangeCount + 1, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun incrementStateChangeCount(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 设置服务启动时间
     */
    @Query("UPDATE statistics SET serviceStartTime = :timestamp, lastUpdateTime = :timestamp WHERE id = 1")
    suspend fun setServiceStartTime(timestamp: Long = System.currentTimeMillis())
    
    /**
     * 重置所有统计数据
     */
    @Query("UPDATE statistics SET " +
           "totalEnterCount = 0, " +
           "totalEnterSuccess = 0, " +
           "totalExitCount = 0, " +
           "totalExitSuccess = 0, " +
           "totalAutoExitCount = 0, " +
           "totalAutoExitRecover = 0, " +
           "totalMaintenanceCount = 0, " +
           "totalStateChangeCount = 0, " +
           "serviceStartTime = 0, " +
           "lastUpdateTime = :timestamp " +
           "WHERE id = 1")
    suspend fun resetAll(timestamp: Long = System.currentTimeMillis())
}
