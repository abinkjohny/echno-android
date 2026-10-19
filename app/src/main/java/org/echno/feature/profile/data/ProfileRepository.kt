package org.echno.feature.profile.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import org.echno.core.network.ApiResult
import org.echno.core.network.safeApiCall
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.Gender
import org.echno.feature.organization.data.toDomain
import org.echno.feature.organization.domain.Organization
import org.echno.feature.profile.data.dto.UserProfileDto
import org.echno.feature.profile.domain.Profile

interface ProfileRepository {
    /** The signed-in user, with [organizationId] (the one they work in now) resolved to its name. */
    suspend fun getMyProfile(organizationId: Long?): ApiResult<Profile>
}

class DefaultProfileRepository @Inject constructor(private val api: ProfileApi) : ProfileRepository {
    override suspend fun getMyProfile(organizationId: Long?): ApiResult<Profile> = safeApiCall {
        val user = api.getCurrentUser()
        val organization = api.getOrganizations(user.id).firstOrNull { it.id == organizationId }?.toDomain()
        user.toDomain(organization)
    }
}

/** Lenient like the project mapper: a value the app doesn't recognise is left out rather than failing the screen. */
internal fun UserProfileDto.toDomain(organization: Organization?) = Profile(
    id = id,
    name = name.orBlankNull() ?: email.orBlankNull() ?: "#$id",
    email = email.orBlankNull(),
    phone = phone.orBlankNull(),
    role = AccountRole.entries.firstOrNull { it.apiValue == role },
    gender = Gender.entries.firstOrNull { it.apiValue.equals(gender, ignoreCase = true) },
    dateOfBirth = dateOfBirth.toLocalDateOrNull(),
    bloodGroup = bloodGroup.orBlankNull(),
    address = address.orBlankNull(),
    emergencyContact = emergencyContact.orBlankNull(),
    qualification = qualification.orBlankNull(),
    experienceYears = experience?.takeIf { it >= 0 },
    skills = skills.orEmpty().filter { it.isNotBlank() },
    certifications = certifications.orEmpty().filter { it.isNotBlank() },
    memberSince = createdAt.toLocalDateOrNull(),
    organization = organization
)

private fun String?.orBlankNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/** The backend sends Java `LocalDateTime` ("1994-05-01T00:00:00"); the profile only shows the date. */
private fun String?.toLocalDateOrNull(): LocalDate? = this?.let {
    try {
        LocalDateTime.parse(it).toLocalDate()
    } catch (e: DateTimeParseException) {
        null
    }
}
