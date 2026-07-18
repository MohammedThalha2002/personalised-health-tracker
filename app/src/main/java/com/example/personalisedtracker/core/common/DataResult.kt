package com.example.personalisedtracker.core.common

/**
 * Sealed app-level error type. Use across all layers instead of throwing.
 */
sealed class AppError(open val message: String) {
    data class Database(override val message: String) : AppError(message)
    data class NotFound(override val message: String) : AppError(message)
    data class Validation(override val message: String) : AppError(message)
    data class Unknown(override val message: String, val cause: Throwable? = null) : AppError(message)
}

/**
 * Lightweight [Result]-like wrapper aligned with Clean Architecture boundaries.
 * Prefer this over kotlin.Result when crossing layers — it keeps errors typed.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: AppError) : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

inline fun <T> runCatchingApp(block: () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (t: Throwable) {
    DataResult.Failure(AppError.Unknown(t.message ?: "Unexpected error", t))
}

