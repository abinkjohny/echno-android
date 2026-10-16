package org.echno.core.network

/**
 * Outcome of a backend call: either the data or a typed [AppError].
 *
 * Repositories return this instead of throwing, so callers must handle failure explicitly
 * (the compiler checks every `when` branch).
 */
sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>

    data class Failure(val error: AppError) : ApiResult<Nothing>
}

/** Transforms the data of a [ApiResult.Success], e.g. a DTO into a domain model; failures pass through. */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Failure -> this
}
