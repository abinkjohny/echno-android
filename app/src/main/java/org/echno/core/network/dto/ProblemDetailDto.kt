package org.echno.core.network.dto

import kotlinx.serialization.Serializable

/**
 * Error body the backend returns for handled failures (RFC 7807 `ProblemDetail`).
 *
 * Mirrors `GlobalExceptionHandler.problem()` in echno-backend: the standard fields, plus
 * `message` (same as `detail`), `errors` on validation failures and `retryable` on
 * conflicts that may succeed on a retry.
 */
@Serializable
data class ProblemDetailDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val message: String? = null,
    val errors: Map<String, String> = emptyMap(),
    val retryable: Boolean = false
)
