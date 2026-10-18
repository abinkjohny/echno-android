package org.echno.feature.auth.data

import javax.inject.Inject
import org.echno.core.network.ApiResult
import org.echno.core.network.safeApiCall
import org.echno.feature.auth.data.dto.RegistrationRequestDto
import org.echno.feature.auth.domain.RegistrationForm
import org.echno.feature.auth.domain.RegistrationValidator

interface RegistrationRepository {
    /** [form] must have passed [RegistrationValidator]. */
    suspend fun register(form: RegistrationForm): ApiResult<Unit>
}

class DefaultRegistrationRepository @Inject constructor(private val api: AuthApi) : RegistrationRepository {
    override suspend fun register(form: RegistrationForm): ApiResult<Unit> = safeApiCall { api.register(form.toDto()) }
}

internal fun RegistrationForm.toDto() = RegistrationRequestDto(
    userName = username.trim(),
    name = fullName.trim(),
    email = email.trim(),
    password = password,
    phone = RegistrationValidator.normalizePhone(phone),
    gender = checkNotNull(gender).apiValue,
    dateOfBirth = "${checkNotNull(dateOfBirth)}T00:00:00",
    role = checkNotNull(role).apiValue,
    acceptTerms = acceptedTerms
)
