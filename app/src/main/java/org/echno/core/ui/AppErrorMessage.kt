package org.echno.core.ui

import androidx.annotation.StringRes
import org.echno.R
import org.echno.core.network.AppError

/**
 * The sentence shown for each kind of failure, so every screen words errors the same way.
 * Server-provided detail is not shown: it's written for developers and isn't translated.
 */
@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.Network -> R.string.error_network
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    is AppError.Forbidden -> R.string.error_forbidden
    is AppError.NotFound -> R.string.error_not_found
    is AppError.Validation -> R.string.error_validation
    is AppError.Conflict -> R.string.error_conflict
    is AppError.RateLimited -> R.string.error_rate_limited
    is AppError.Server -> R.string.error_server
    is AppError.Unknown -> R.string.error_unknown
}
