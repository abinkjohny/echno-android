package org.echno.feature.auth.data

import org.echno.feature.auth.data.dto.RegistrationRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    /**
     * Creates the Keycloak account and the echno user in one call. Open to signed-out callers.
     * Answers 201 with the new user, which the app doesn't need (it signs in next), so Unit.
     */
    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegistrationRequestDto)
}
