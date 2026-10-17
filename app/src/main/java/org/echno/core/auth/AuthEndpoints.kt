package org.echno.core.auth

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Keycloak's OpenID Connect endpoints for one realm. Keycloak's paths are fixed, so they are derived
 * from the issuer instead of fetched from `.well-known/openid-configuration` on every sign-in.
 */
data class AuthEndpoints(val authorization: HttpUrl, val token: HttpUrl, val endSession: HttpUrl) {
    companion object {
        /** [issuer] is the realm URL, e.g. `https://auth.echno.in/realms/echno-realm`. */
        fun forIssuer(issuer: String): AuthEndpoints {
            val base = issuer.trimEnd('/')
            return AuthEndpoints(
                authorization = "$base/protocol/openid-connect/auth".toHttpUrl(),
                token = "$base/protocol/openid-connect/token".toHttpUrl(),
                endSession = "$base/protocol/openid-connect/logout".toHttpUrl()
            )
        }
    }
}
