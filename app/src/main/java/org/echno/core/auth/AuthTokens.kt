package org.echno.core.auth

import java.time.Instant
import kotlinx.serialization.Serializable

/**
 * The tokens Keycloak issued for the signed-in user.
 *
 * Stored encrypted on the device. [refreshToken] is an offline token when the
 * `offline_access` scope was granted, so the session survives app restarts for weeks, not hours.
 */
@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    /** When [accessToken] stops being accepted, as epoch seconds. */
    val accessTokenExpiresAt: Long
) {
    /** True when the access token has expired, or will within [leewaySeconds] (clock skew, slow networks). */
    fun isAccessTokenExpired(now: Instant, leewaySeconds: Long = 30): Boolean = now.epochSecond + leewaySeconds >= accessTokenExpiresAt
}
