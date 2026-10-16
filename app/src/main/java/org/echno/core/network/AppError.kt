package org.echno.core.network

/**
 * Every way a backend call can fail, as the UI needs to tell them apart.
 *
 * Screens decide what to show from the case alone (e.g. [Network] → "Check your connection",
 * [Unauthorized] → back to login), so no HTTP status codes leak above the data layer.
 */
sealed interface AppError {
    /** No connection, DNS failure or connection refused. */
    data object Network : AppError

    /** The server didn't answer in time. */
    data object Timeout : AppError

    /** 401: the access token is missing, expired or rejected. */
    data object Unauthorized : AppError

    /** 403: signed in, but not allowed (role, or not a member of the organization). */
    data class Forbidden(val detail: String?) : AppError

    /** 404 */
    data class NotFound(val detail: String?) : AppError

    /** 400 / 422: the request was rejected. [fieldErrors] maps a field name to its message. */
    data class Validation(val detail: String?, val fieldErrors: Map<String, String> = emptyMap()) : AppError

    /** 409: e.g. a duplicate, or a concurrent update. [retryable] is set when trying again may succeed. */
    data class Conflict(val detail: String?, val retryable: Boolean = false) : AppError

    /** 429: too many requests. */
    data class RateLimited(val retryAfterSeconds: Long?) : AppError

    /** 5xx */
    data class Server(val status: Int, val detail: String?) : AppError

    /** Anything unexpected, such as a response we couldn't parse. */
    data class Unknown(val cause: Throwable) : AppError
}
