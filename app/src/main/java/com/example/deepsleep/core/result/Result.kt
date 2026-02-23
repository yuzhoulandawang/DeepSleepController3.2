package com.example.deepsleep.core.result

sealed class Result<T> {

    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val throwable: Throwable? = null, val code: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrDefault(defaultValue: T): T = if (this is Success) data else defaultValue

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw throwable ?: IllegalStateException(message)
        is Loading -> throw IllegalStateException("Result is in Loading state")
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this as Result<R>
        is Loading -> Loading as Result<R>
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this as Result<R>
        is Loading -> Loading as Result<R>
    }

    fun onSuccess(action: (T) -> Unit): Result<T> { if (this is Success) action(data); return this }
    fun onError(action: (String, Throwable?) -> Unit): Result<T> { if (this is Error) action(message, throwable); return this }
    fun onLoading(action: () -> Unit): Result<T> { if (this is Loading) action(); return this }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(message: String, throwable: Throwable? = null, code: Int? = null): Result<Nothing> = Error(message, throwable, code)
        fun <T> loading(): Result<T> = Loading as Result<T>

        inline fun <T> catch(block: () -> Result<T>): Result<T> = try {
            block()
        } catch (e: Exception) {
            error(e.message ?: "Unknown error", e) as Result<T>
        }
    }
}

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

fun rootError(message: String, throwable: Throwable? = null): Result<Nothing> {
    val code = when {
        message.contains("permission", ignoreCase = true) -> ErrorCode.ROOT_PERMISSION_DENIED
        message.contains("not available", ignoreCase = true) -> ErrorCode.ROOT_NOT_AVAILABLE
        else -> ErrorCode.ROOT_COMMAND_FAILED
    }
    return Result.error(message, throwable, code)
}

fun serviceError(message: String, throwable: Throwable? = null): Result<Nothing> {
    val code = when {
        message.contains("start", ignoreCase = true) -> ErrorCode.SERVICE_START_FAILED
        message.contains("stop", ignoreCase = true) -> ErrorCode.SERVICE_STOP_FAILED
        else -> ErrorCode.UNKNOWN_ERROR
    }
    return Result.error(message, throwable, code)
}