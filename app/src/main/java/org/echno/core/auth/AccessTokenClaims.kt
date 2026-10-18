package org.echno.core.auth

import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Reads what the app needs from the access token's payload (the middle part of the JWT).
 *
 * The signature is not checked: the app only uses this to decide what to show and which
 * organization to ask about. The backend verifies the token on every request.
 */
object AccessTokenClaims {
    // The backend's JwtAuthConverter: "/org-5" means member of organization 5;
    // "/org-5/system-admin" is a role inside it.
    private val membershipGroup = Regex("^/?org-(\\d+)$")

    /** The organizations the user belongs to, from Keycloak's `groups` claim. Empty if unreadable. */
    fun organizationIds(accessToken: String): List<Long> {
        val payload = accessToken.split('.').getOrNull(1) ?: return emptyList()
        val groups =
            runCatching {
                val json = String(Base64.getUrlDecoder().decode(payload))
                Json.parseToJsonElement(json).jsonObject["groups"]?.jsonArray
            }.getOrNull() ?: return emptyList()
        return groups
            .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            .mapNotNull { membershipGroup.matchEntire(it)?.groupValues?.get(1)?.toLongOrNull() }
            .distinct()
    }
}
