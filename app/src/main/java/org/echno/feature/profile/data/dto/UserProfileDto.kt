package org.echno.feature.profile.data.dto

import kotlinx.serialization.Serializable

/** `GET /api/v1/user`: the backend's `UserDto`, only what the profile shows. Every field may be missing. */
@Serializable
data class UserProfileDto(
    val id: Long,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val bloodGroup: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val qualification: String? = null,
    val experience: Int? = null,
    val skills: List<String>? = null,
    val certifications: List<String>? = null,
    val role: String? = null,
    val createdAt: String? = null
)
