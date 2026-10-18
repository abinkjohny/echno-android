package org.echno.feature.auth.domain

import java.time.LocalDate
import java.time.Period

/** What the user typed into the registration form. Choices are null until made. */
data class RegistrationForm(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null,
    val role: AccountRole? = null,
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false
)

/** Sent as these exact strings, like echno-web does. */
enum class Gender(val apiValue: String) {
    Male("Male"),
    Female("Female"),
    Other("Other")
}

/** The backend's `UserRole`: the user's role in their company, not a permission. */
enum class AccountRole(val apiValue: String) {
    Owner("OWNER"),
    CoFounder("CO_FOUNDER"),
    HrManager("HR_MANAGER"),
    Employee("EMPLOYEE"),
    Student("STUDENT"),
    Management("MANAGEMENT"),
    Administrator("ADMINISTRATOR")
}

enum class RegistrationField {
    FullName,
    Username,
    Email,
    Phone,
    DateOfBirth,
    Gender,
    Role,
    Password,
    ConfirmPassword,
    Terms
}

enum class FieldError {
    Required,
    UsernameFormat,
    NameFormat,
    EmailFormat,
    PhoneFormat,
    TooYoung,
    PasswordWeak,
    PasswordMismatch,
    TermsNotAccepted,
    EmailTaken,

    /** The server refused the value for a reason the app doesn't check itself. */
    Rejected
}

/**
 * The form's rules, the same as echno-web's (`lib/validators`), checked before anything is sent.
 * The backend checks again; its answers are mapped in the ViewModel.
 */
object RegistrationValidator {
    const val MINIMUM_AGE = 18

    private val usernamePattern = Regex("^[A-Za-z0-9._-]{4,20}$")

    // Letters from any script, so names written in Malayalam or Hindi are accepted too.
    private val namePattern = Regex("^[\\p{L}\\p{M}\\s'.-]{2,}$")
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private val phonePattern = Regex("^\\+?[1-9]\\d{7,14}$")

    fun validate(form: RegistrationForm, today: LocalDate): Map<RegistrationField, FieldError> = buildMap {
        fun check(field: RegistrationField, error: FieldError?) {
            if (error != null) put(field, error)
        }
        check(RegistrationField.FullName, text(form.fullName.trim(), namePattern, FieldError.NameFormat))
        check(RegistrationField.Username, text(form.username.trim(), usernamePattern, FieldError.UsernameFormat))
        check(RegistrationField.Email, text(form.email.trim(), emailPattern, FieldError.EmailFormat))
        check(RegistrationField.Phone, text(normalizePhone(form.phone), phonePattern, FieldError.PhoneFormat))
        check(RegistrationField.DateOfBirth, dateOfBirth(form.dateOfBirth, today))
        check(RegistrationField.Gender, FieldError.Required.takeIf { form.gender == null })
        check(RegistrationField.Role, FieldError.Required.takeIf { form.role == null })
        check(RegistrationField.Password, password(form.password))
        check(
            RegistrationField.ConfirmPassword,
            when {
                form.confirmPassword.isEmpty() -> FieldError.Required
                form.confirmPassword != form.password -> FieldError.PasswordMismatch
                else -> null
            }
        )
        check(RegistrationField.Terms, FieldError.TermsNotAccepted.takeIf { !form.acceptedTerms })
    }

    /** Spaces, dashes and brackets help reading a number but aren't part of it: "+91 98765-43210" → "+919876543210". */
    fun normalizePhone(phone: String): String = phone.filterNot { it.isWhitespace() || it in "-()" }

    private fun text(value: String, pattern: Regex, formatError: FieldError): FieldError? = when {
        value.isEmpty() -> FieldError.Required
        !pattern.matches(value) -> formatError
        else -> null
    }

    private fun dateOfBirth(date: LocalDate?, today: LocalDate): FieldError? = when {
        date == null -> FieldError.Required
        Period.between(date, today).years < MINIMUM_AGE -> FieldError.TooYoung
        else -> null
    }

    // At least 8 characters with an upper-case letter, a lower-case letter, a digit and a symbol.
    private fun password(value: String): FieldError? = when {
        value.isEmpty() -> FieldError.Required
        value.length < 8 ||
            value.none(Char::isUpperCase) ||
            value.none(Char::isLowerCase) ||
            value.none(Char::isDigit) ||
            value.none { !it.isLetterOrDigit() && !it.isWhitespace() } -> FieldError.PasswordWeak
        else -> null
    }
}
