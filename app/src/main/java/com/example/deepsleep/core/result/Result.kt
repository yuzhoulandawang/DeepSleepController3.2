package com.example.deepsleep.core.result

/**
 * 统一的结果封装类
 * 用于封装操作结果，提供更好的错误处理
 */
sealed class Result<T> {   // 移除 out 修饰符

    /**
     * 操作成功
     * @param data 成功返回的数据
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * 操作失败
     * @param message 错误消息
     * @param throwable 异常对象（可选）
     * @param code 错误码（可选）
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val code: Int? = null
    ) : Result<Nothing>()

    /**
     * 操作进行中
     */
    object Loading : Result<Nothing>()

    /**
     * 判断是否成功
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 判断是否失败
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 判断是否加载中
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * 获取成功数据，如果失败则返回默认值
     */
    fun getOrDefault(defaultValue: T): T {
        return when (this) {
            is Success -> data
            else -> defaultValue
        }
    }

    /**
     * 获取成功数据，如果失败则抛出异常
     */
    fun getOrThrow(): T {
        return when (this) {
            is Success -> data
            is Error -> throw throwable ?: IllegalStateException(message)
            is Loading -> throw IllegalStateException("Result is in Loading state")
        }
    }

    /**
     * 映射成功数据
     */
    fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(message, throwable, code)
            is Loading -> Loading
        }
    }

    /**
     * 扁平化映射成功数据
     */
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> Error(message, throwable, code)
            is Loading -> Loading
        }
    }

    /**
     * 处理成功结果
     */
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * 处理错误结果
     */
    fun onError(action: (String, Throwable?) -> Unit): Result<T> {
        if (this is Error) {
            action(message, throwable)
        }
        return this
    }

    /**
     * 处理加载状态
     */
    fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) {
            action()
        }
        return this
    }

    companion object {
        /**
         * 创建成功结果
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * 创建失败结果
         */
        fun error(message: String, throwable: Throwable? = null, code: Int? = null): Result<Nothing> =
            Error(message, throwable, code)

        /**
         * 创建加载结果
         */
        fun <T> loading(): Result<T> = Loading as Result<T>

        /**
         * 捕获异常并转换为 Result
         */
        inline fun <T> catch(block: () -> Result<T>): Result<T> {
            return try {
                block()
            } catch (e: Exception) {
                error(e.message ?: "Unknown error", e)
            }
        }
    }
}

/**
 * 错误码常量
 */
object ErrorCode {
    const val ROOT_PERMISSION_DENIED = 1001
    const val ROOT_COMMAND_FAILED = 1002
    const val ROOT_NOT_AVAILABLE = 1003
    const val SERVICE_START_FAILED = 2001
    const val SERVICE_STOP_FAILED = 2002
    const val DOZE_ENTER_FAILED = 3001
    const val DOZE_EXIT_FAILED = 3002
    const val SETTINGS_SAVE_FAILED = 4001
    const val SETTINGS_LOAD_FAILED = 4002
    const val NETWORK_ERROR = 5001
    const val TIMEOUT_ERROR = 5002
    const val UNKNOWN_ERROR = 9999
}

/**
 * 创建 Root 相关错误结果
 */
fun rootError(message: String, throwable: Throwable? = null): Result<Nothing> {
    val code = when {
        message.contains("permission", ignoreCase = true) -> ErrorCode.ROOT_PERMISSION_DENIED
        message.contains("not available", ignoreCase = true) -> ErrorCode.ROOT_NOT_AVAILABLE
        else -> ErrorCode.ROOT_COMMAND_FAILED
    }
    return Result.error(message, throwable, code)
}

/**
 * 创建服务相关错误结果
 */
fun serviceError(message: String, throwable: Throwable? = null): Result<Nothing> {
    val code = when {
        message.contains("start", ignoreCase = true) -> ErrorCode.SERVICE_START_FAILED
        message.contains("stop", ignoreCase = true) -> ErrorCode.SERVICE_STOP_FAILED
        else -> ErrorCode.UNKNOWN_ERROR
    }
    return Result.error(message, throwable, code)
}