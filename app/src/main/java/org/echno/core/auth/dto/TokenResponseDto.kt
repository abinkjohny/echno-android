package org.echno.core.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Keycloak's token endpoint response (RFC 6749 §5.1), only the fields the app uses. */
@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("id_token") val idToken: String? = null
)

/** Keycloak's error body (RFC 6749 §5.2), e.g. `{"error":"invalid_grant"}`. */
@Serializable
data class TokenErrorDto(val error: String? = null)
