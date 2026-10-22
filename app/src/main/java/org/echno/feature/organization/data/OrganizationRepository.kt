package org.echno.feature.organization.data

import javax.inject.Inject
import org.echno.core.network.ApiResult
import org.echno.core.network.map
import org.echno.core.network.safeApiCall
import org.echno.feature.organization.data.dto.InviteCodeRequestDto
import org.echno.feature.organization.data.dto.OrganizationDto
import org.echno.feature.organization.domain.Organization

interface OrganizationRepository {
    /**
     * The organizations the user belongs to. [memberOf] are the ids from the access token; the
     * first scopes the requests. When the token lists none (no `groups` claim, e.g. the client's
     * mapper is missing), the server is still asked, without a scope: the backend works it out
     * when the user belongs to at most one organization.
     */
    suspend fun getMyOrganizations(memberOf: List<Long>): ApiResult<List<Organization>>

    /** Joins the organization an invitation [code] belongs to, and returns it. [memberOf] as above. */
    suspend fun joinWithInviteCode(code: String, memberOf: List<Long>): ApiResult<Organization>
}

class DefaultOrganizationRepository @Inject constructor(private val api: OrganizationApi) : OrganizationRepository {
    override suspend fun getMyOrganizations(memberOf: List<Long>): ApiResult<List<Organization>> {
        val scope = memberOf.firstOrNull()
        return safeApiCall {
            val user = api.getCurrentUser(scope)
            api.getOrganizations(user.id, scope)
        }.map { organizations -> organizations.map { it.toDomain() }.sortedBy { it.name.lowercase() } }
    }

    override suspend fun joinWithInviteCode(code: String, memberOf: List<Long>): ApiResult<Organization> {
        val scope = memberOf.firstOrNull()
        return safeApiCall {
            val user = api.getCurrentUser(scope)
            api.redeemInviteCode(user.id, InviteCodeRequestDto(code), scope)
        }.map { it.toDomain() }
    }
}

internal fun OrganizationDto.toDomain() = Organization(
    id = id,
    name = organizationName?.takeIf { it.isNotBlank() } ?: "#$id",
    address = organizationAddress?.takeIf { it.isNotBlank() },
    isActive = isActive != false
)
