package com.example.deepsleep.core.security

import com.example.deepsleep.root.RootCommander

/**
 * 安全验证器
 * 用于加强权限验证和输入过滤
 */
object SecurityValidator {
    
    /**
     * 多重验证 Root 权限
     * 至少需要 3 个检查通过
     */
    suspend fun verifyRoot(): Boolean {
        val checks = mutableListOf<Boolean>()
        
        try {
            // 检查 1: 验证 uid 是否为 0
            val idCheck = RootCommander.exec("id").out.any { it.contains("uid=0") }
            checks.add(idCheck)
            
            // 检查 2: 验证 whoami 是否返回 root
            val whoamiCheck = RootCommander.exec("whoami").out.any { it.trim() == "root" }
            checks.add(whoamiCheck)
            
            // 检查 3: 验证是否可以执行需要 root 的命令
            val suCheck = RootCommander.exec("su -c 'echo test'").isSuccess
            checks.add(suCheck)
            
            // 检查 4: 验证是否可以访问 /data 目录
            val dataCheck = RootCommander.exec("ls /data").isSuccess
            checks.add(dataCheck)
            
            // 检查 5: 验证是否可以写入系统文件
            val writeCheck = RootCommander.safeWrite("/data/local/tmp/root_test", "test")
            checks.add(writeCheck)
            
        } catch (e: Exception) {
            return false
        }
        
        // 至少 3 个检查通过
        return checks.count { it } >= 3
    }
    
    /**
     * 获取详细的 Root 权限信息
     */
    suspend fun getRootVerificationDetails(): RootVerificationInfo {
        val results = mutableMapOf<String, Boolean>()
        
        try {
            results["uid=0"] = RootCommander.exec("id").out.any { it.contains("uid=0") }
            results["whoami=root"] = RootCommander.exec("whoami").out.any { it.trim() == "root" }
            results["su command"] = RootCommander.exec("su -c 'echo test'").isSuccess
            results["access /data"] = RootCommander.exec("ls /data").isSuccess
            results["write system"] = RootCommander.safeWrite("/data/local/tmp/root_test", "test")
        } catch (e: Exception) {
            // 忽略错误
        }
        
        val passedCount = results.count { it.value }
        val totalCount = results.size
        
        return RootVerificationInfo(
            isVerified = passedCount >= 3,
            passedChecks = passedCount,
            totalChecks = totalCount,
            details = results
        )
    }
    
    /**
     * 验证命令参数，防止注入攻击
     */
    fun sanitizeCommandParam(param: String): String {
        // 移除危险字符
        return param
            .replace(Regex("[;&|`$()]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * 验证包名格式
     */
    fun isValidPackageName(packageName: String): Boolean {
        return packageName.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$"))
    }
    
    /**
     * 验证文件路径，防止目录遍历攻击
     */
    fun isValidFilePath(path: String): Boolean {
        return !path.contains("..") && 
               path.startsWith("/") && 
               path.length < 512
    }
    
    /**
     * 验证数值范围
     */
    fun <T : Number> isValidRange(value: T, min: T, max: T): Boolean {
        val numValue = value.toLong()
        return numValue in min.toLong()..max.toLong()
    }
}

/**
 * Root 权限验证信息
 */
data class RootVerificationInfo(
    val isVerified: Boolean,
    val passedChecks: Int,
    val totalChecks: Int,
    val details: Map<String, Boolean>
) {
    val passRate: Float
        get() = if (totalChecks > 0) passedChecks.toFloat() / totalChecks else 0f
}
