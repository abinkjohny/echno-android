package org.echno.feature.organization.data

import org.echno.feature.organization.data.dto.CurrentUserDto
import org.echno.feature.organization.data.dto.InviteCodeRequestDto
import org.echno.feature.organization.data.dto.OrganizationDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * These calls run before an organization is chosen, so they name one explicitly: the backend answers
 * 400 when a member of several organizations sends no `X-Organization-Id`. A null header is left out.
 */
interface OrganizationApi {
    @GET("api/v1/user")
    suspend fun getCurrentUser(@Header("X-Organization-Id") organizationId: Long?): CurrentUserDto

    @GET("api/v1/user/{userId}/organizations")
    suspend fun getOrganizations(@Path("userId") userId: Long, @Header("X-Organization-Id") organizationId: Long?): List<OrganizationDto>

    /**
     * Redeems an invitation code: the backend adds the user to the code's organization (and its
     * Keycloak group) and answers with that organization. 404 for an unknown code, 400 for one
     * that expired, was switched off or is used up, 429 with Retry-After after too many tries.
     * The path says "web" but it is the only redemption endpoint, and it accepts the user's own id.
     */
    @POST("api/v1/invitation/web/validate/userId/{userId}")
    suspend fun redeemInviteCode(
        @Path("userId") userId: Long,
        @Body body: InviteCodeRequestDto,
        @Header("X-Organization-Id") organizationId: Long?
    ): OrganizationDto
}
