package org.echno.feature.organization.data.dto

import kotlinx.serialization.Serializable

/** `GET /api/v1/user`: the signed-in user (the backend's `UserDto`), only what the app uses. */
@Serializable
data class CurrentUserDto(val id: Long, val name: String? = null)

/** `GET /api/v1/user/{id}/organizations`: the backend's `OrganizationDto`, only what the app uses. */
@Serializable
data class OrganizationDto(
    val id: Long,
    val organizationName: String? = null,
    val organizationAddress: String? = null,
    val isActive: Boolean? = null
)
