package com.example.deepsleep.root

import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.utils.Logger

object ProcessSuppressor {
    private const val TAG = "ProcessSuppressor"
    
    /**
     * 压制后台进程
     */
    suspend fun suppress(oomValue: Int, whitelist: List<String>) {
        try {
            val pids = RootCommander.exec(
                "ls /proc | grep -E '^[0-9]+$'"
            ).out
            
            val commands = mutableListOf<String>()
            
            for (pid in pids) {
                val pidNum = pid.toIntOrNull() ?: continue
                if (pidNum == android.os.Process.myPid()) continue
                
                // 使用 UID 过滤系统进程（UID < 10000 为系统进程）
                val status = RootCommander.readFile("/proc/$pidNum/status") ?: continue
                val uidLine = status.lines().find { it.startsWith("Uid:") } ?: continue
                val uid = uidLine.split("\t").getOrNull(1)?.toIntOrNull() ?: 0
                if (uid < 10000) continue  // 跳过系统进程
                
                val cmdline = RootCommander.readFile("/proc/$pidNum/cmdline") ?: ""
                if (isWhitelisted(cmdline, whitelist)) continue
                
                commands.add("echo $oomValue > /proc/$pidNum/oom_score_adj 2>/dev/null || true")
            }
            
            if (commands.isNotEmpty()) {
                commands.chunked(100).forEach { batch ->
                    RootCommander.execBatch(batch)
                }
            }
            
            Logger.i(TAG, "已压制 ${commands.size} 个进程")
        } catch (e: Exception) {
            Logger.e(TAG, "进程压制失败: ${e.message}")
        }
    }
    
    /** 
     * 检查进程是否在白名单中
     */
    private fun isWhitelisted(cmdline: String, whitelist: List<String>): Boolean {
        val name = cmdline.split('\u0000').firstOrNull() ?: ""
        val basename = name.substringAfterLast("/")
        
        return whitelist.any { pattern ->
            name.contains(pattern) || basename.contains(pattern)
        }
    }
    
    /**
     * 停止进程压制
     */
    suspend fun unsuppress() {
        try {
            Logger.i(TAG, "停止进程压制")
            // 恢复默认 OOM 调整值
            val commands = listOf(
                "for pid in /proc/[0-9]*; do echo 0 > $pid/oom_score_adj 2>/dev/null || true; done"
            )
            RootCommander.execBatch(commands)
        } catch (e: Exception) {
            Logger.e(TAG, "停止进程压制失败: ${e.message}")
        }
    }
}