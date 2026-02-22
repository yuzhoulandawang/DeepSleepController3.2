package com.example.deepsleep.root
import android.content.Context

import com.example.deepsleep.BuildConfig
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootCommander {

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    // 确保 Shell 已连接，如果未连接则尝试连接
    private suspend fun ensureShell(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 主动请求 root 授权（触发 Magisk 弹窗）
    suspend fun requestRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查设备是否具有 root 权限
     * 
     * 修复说明：
     * - 原实现只检查命令是否执行成功，导致未 root 设备也会返回 true
     * - 新实现检查输出是否包含 "uid=0"，确保真正的 root 权限
     * 
     * @return true 如果设备具有 root 权限，否则 false
     */
    suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        if (!ensureShell()) return@withContext false
        
        try {
            val result = Shell.cmd("id").exec()
            
            // 检查命令是否成功执行
            if (!result.isSuccess) {
                return@withContext false
            }
            
            // 关键修复：检查输出是否包含 uid=0
            // root 用户的 id 输出示例：uid=0(root) gid=0(root) groups=0(root)
            // 普通 user 的 id 输出示例：uid=10134(u0_a134) gid=10134(u0_a134) groups=10134(u0_a134)
            val output = result.out.joinToString("\n")
            
            if (output.contains("uid=0")) {
                // 进一步验证：确保确实有 root 权限，可以执行需要 root 的命令
                val testResult = Shell.cmd("whoami").exec()
                val whoamiOutput = testResult.out.joinToString("\n")
                
                // whoami 应该返回 "root"
                return@withContext whoamiOutput.trim() == "root"
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取详细的 root 权限信息（用于调试）
     */
    suspend fun getRootInfo(): RootInfo = withContext(Dispatchers.IO) {
        try {
            val idResult = Shell.cmd("id").exec()
            val whoamiResult = Shell.cmd("whoami").exec()
            val suResult = Shell.cmd("su -c 'echo test'").exec()
            
            RootInfo(
                hasRoot = checkRoot(),
                idOutput = idResult.out.joinToString("\n"),
                whoamiOutput = whoamiResult.out.joinToString("\n"),
                suTestSuccess = suResult.isSuccess,
                errorMessage = if (!checkRoot()) "设备未获取 root 权限或授权被拒绝" else null
            )
        } catch (e: Exception) {
            RootInfo(
                hasRoot = false,
                idOutput = "",
                whoamiOutput = "",
                suTestSuccess = false,
                errorMessage = "检查 root 权限时发生异常: ${e.message}"
            )
        }
    }

    suspend fun exec(command: String): Shell.Result = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd(command).exec()
    }

    suspend fun exec(vararg commands: String): Shell.Result = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd(*commands).exec()
    }

    suspend fun execBatch(commands: List<String>): Shell.Result = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd(*commands.toTypedArray()).exec()
    }

    suspend fun safeWrite(path: String, value: String): Boolean = withContext(Dispatchers.IO) {
        ensureShell()
        val result = Shell.cmd("printf '%s' \"$value\" > $path").exec()
        result.isSuccess
    }

    suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        ensureShell()
        val result = Shell.cmd("cat $path 2>/dev/null").exec()
        if (result.isSuccess) result.out.joinToString("\n") else null
    }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd("[ -f $path ]").exec().isSuccess
    }

    suspend fun mkdir(path: String): Boolean = withContext(Dispatchers.IO) {
        ensureShell()
        Shell.cmd("mkdir -p $path").exec().isSuccess
    }
}

/**
 * Root 权限信息数据类
 */
data class RootInfo(
    val hasRoot: Boolean,
    val idOutput: String,
    val whoamiOutput: String,
    val suTestSuccess: Boolean,
    val errorMessage: String? = null
)
