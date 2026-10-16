package org.echno.core.network

import java.io.IOException
import java.io.InterruptedIOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import org.echno.core.network.dto.ProblemDetailDto
import retrofit2.HttpException

/**
 * Runs a Retrofit call and turns every outcome into an [ApiResult].
 *
 * Use it in repositories: `safeApiCall { api.getProjects() }`. Nothing is thrown to callers
 * except coroutine cancellation, which must propagate so that leaving a screen stops its requests.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(call())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    ApiResult.Failure(e.toAppError())
} catch (e: InterruptedIOException) {
    // SocketTimeoutException and OkHttp's call timeout both extend InterruptedIOException.
    ApiResult.Failure(AppError.Timeout)
} catch (e: IOException) {
    ApiResult.Failure(AppError.Network)
} catch (e: SerializationException) {
    ApiResult.Failure(AppError.Unknown(e))
} catch (e: Exception) {
    // Anything else, e.g. Retrofit's KotlinNullPointerException when a 204 answers a method
    // declared to return a body (declare such endpoints as returning Unit instead).
    ApiResult.Failure(AppError.Unknown(e))
}

/** Maps a non-2xx response to an [AppError], using the backend's ProblemDetail body when present. */
internal fun HttpException.toAppError(): AppError {
    val response = response()
    // 401s come from Spring Security with no body; other errors carry a ProblemDetail.
    val problem =
        response?.errorBody()?.string()?.let { body ->
            runCatching { NetworkJson.decodeFromString<ProblemDetailDto>(body) }.getOrNull()
        }
    val detail = problem?.detail ?: problem?.message

    return when (val status = code()) {
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden(detail)
        404 -> AppError.NotFound(detail)
        400, 422 -> AppError.Validation(detail, problem?.errors.orEmpty())
        409 -> AppError.Conflict(detail, retryable = problem?.retryable == true)
        429 -> AppError.RateLimited(retryAfterSeconds = response?.headers()?.get("Retry-After")?.toLongOrNull())
        in 500..599 -> AppError.Server(status, detail)
        else -> AppError.Unknown(this)
    }
}
