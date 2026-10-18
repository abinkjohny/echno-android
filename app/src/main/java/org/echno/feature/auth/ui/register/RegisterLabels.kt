package org.echno.feature.auth.ui.register

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import org.echno.R
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.FieldError
import org.echno.feature.auth.domain.Gender
import org.echno.feature.auth.domain.RegistrationValidator

@StringRes
fun Gender.labelRes(): Int = when (this) {
    Gender.Male -> R.string.gender_male
    Gender.Female -> R.string.gender_female
    Gender.Other -> R.string.gender_other
}

@StringRes
fun AccountRole.labelRes(): Int = when (this) {
    AccountRole.Owner -> R.string.role_owner
    AccountRole.CoFounder -> R.string.role_co_founder
    AccountRole.HrManager -> R.string.role_hr_manager
    AccountRole.Employee -> R.string.role_employee
    AccountRole.Student -> R.string.role_student
    AccountRole.Management -> R.string.role_management
    AccountRole.Administrator -> R.string.role_administrator
}

@Composable
fun FieldError.message(): String = when (this) {
    FieldError.Required -> stringResource(R.string.field_error_required)
    FieldError.UsernameFormat -> stringResource(R.string.field_error_username)
    FieldError.NameFormat -> stringResource(R.string.field_error_name)
    FieldError.EmailFormat -> stringResource(R.string.field_error_email)
    FieldError.PhoneFormat -> stringResource(R.string.field_error_phone)
    FieldError.TooYoung -> pluralStringResource(
        R.plurals.field_error_too_young,
        RegistrationValidator.MINIMUM_AGE,
        RegistrationValidator.MINIMUM_AGE
    )
    FieldError.PasswordWeak -> stringResource(R.string.field_error_password_weak)
    FieldError.PasswordMismatch -> stringResource(R.string.field_error_password_mismatch)
    FieldError.TermsNotAccepted -> stringResource(R.string.field_error_terms)
    FieldError.EmailTaken -> stringResource(R.string.field_error_email_taken)
    FieldError.Rejected -> stringResource(R.string.field_error_rejected)
}
