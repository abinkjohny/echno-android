package org.echno.feature.auth.data.dto

import kotlinx.serialization.Serializable

/** Body of `POST /api/v1/auth/register` (the backend's `UserRegistrationDto`). */
@Serializable
data class RegistrationRequestDto(
    val userName: String,
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val gender: String,
    /** A `LocalDateTime` on the backend: `2000-01-31T00:00:00`. */
    val dateOfBirth: String,
    val role: String,
    val acceptTerms: Boolean
)
