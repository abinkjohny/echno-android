package org.echno.feature.organization.data

import org.echno.feature.organization.data.dto.CurrentUserDto
import org.echno.feature.organization.data.dto.OrganizationDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Both calls run before an organization is chosen, so they name one explicitly: the backend answers
 * 400 when a member of several organizations sends no `X-Organization-Id`. A null header is left out.
 */
interface OrganizationApi {
    @GET("api/v1/user")
    suspend fun getCurrentUser(@Header("X-Organization-Id") organizationId: Long?): CurrentUserDto

    @GET("api/v1/user/{userId}/organizations")
    suspend fun getOrganizations(@Path("userId") userId: Long, @Header("X-Organization-Id") organizationId: Long?): List<OrganizationDto>
}
