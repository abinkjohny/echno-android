package org.echno.feature.profile.data

import org.echno.feature.organization.data.dto.OrganizationDto
import org.echno.feature.profile.data.dto.UserProfileDto
import retrofit2.http.GET
import retrofit2.http.Path

/** Runs after an organization is chosen: `OrganizationInterceptor` adds `X-Organization-Id`. */
interface ProfileApi {
    @GET("api/v1/user")
    suspend fun getCurrentUser(): UserProfileDto

    @GET("api/v1/user/{userId}/organizations")
    suspend fun getOrganizations(@Path("userId") userId: Long): List<OrganizationDto>
}
