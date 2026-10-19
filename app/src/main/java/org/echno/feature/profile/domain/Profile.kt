package org.echno.feature.profile.domain

import java.time.LocalDate
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.Gender
import org.echno.feature.organization.domain.Organization

/** The signed-in user, as the profile screen shows them. Optional details the user never filled in are null or empty. */
data class Profile(
    val id: Long,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: AccountRole?,
    val gender: Gender?,
    val dateOfBirth: LocalDate?,
    val bloodGroup: String?,
    val address: String?,
    val emergencyContact: String?,
    val qualification: String?,
    val experienceYears: Int?,
    val skills: List<String>,
    val certifications: List<String>,
    val memberSince: LocalDate?,
    /** The organization the user is working in now; null if the server no longer lists it. */
    val organization: Organization?
)
