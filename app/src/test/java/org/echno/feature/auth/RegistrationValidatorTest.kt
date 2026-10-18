package org.echno.feature.auth

import java.time.LocalDate
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.FieldError
import org.echno.feature.auth.domain.Gender
import org.echno.feature.auth.domain.RegistrationField
import org.echno.feature.auth.domain.RegistrationForm
import org.echno.feature.auth.domain.RegistrationValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

val validForm =
    RegistrationForm(
        fullName = "Asha Menon",
        username = "asha.menon",
        email = "asha@example.com",
        phone = "+91 98765 43210",
        dateOfBirth = LocalDate.of(1995, 3, 14),
        gender = Gender.Female,
        role = AccountRole.Employee,
        password = "Site#2026a",
        confirmPassword = "Site#2026a",
        acceptedTerms = true
    )

class RegistrationValidatorTest {
    private val today = LocalDate.of(2026, 10, 17)

    private fun errorFor(form: RegistrationForm, field: RegistrationField) = RegistrationValidator.validate(form, today)[field]

    @Test
    fun `a complete form has no errors`() {
        assertEquals(emptyMap<RegistrationField, FieldError>(), RegistrationValidator.validate(validForm, today))
    }

    @Test
    fun `an empty form flags every field`() {
        val errors = RegistrationValidator.validate(RegistrationForm(), today)

        assertEquals(RegistrationField.entries.toSet(), errors.keys)
        assertEquals(FieldError.TermsNotAccepted, errors[RegistrationField.Terms])
    }

    @Test
    fun `usernames are 4 to 20 characters without spaces`() {
        assertEquals(FieldError.UsernameFormat, errorFor(validForm.copy(username = "abc"), RegistrationField.Username))
        assertEquals(FieldError.UsernameFormat, errorFor(validForm.copy(username = "asha menon"), RegistrationField.Username))
        assertNull(errorFor(validForm.copy(username = "a_b-c.d"), RegistrationField.Username))
    }

    @Test
    fun `names may use any script`() {
        assertNull(errorFor(validForm.copy(fullName = "ആശ മേനോൻ"), RegistrationField.FullName))
        assertEquals(FieldError.NameFormat, errorFor(validForm.copy(fullName = "Asha 2"), RegistrationField.FullName))
    }

    @Test
    fun `phone numbers may be typed with spaces and dashes`() {
        assertNull(errorFor(validForm.copy(phone = "+91 98765-43210"), RegistrationField.Phone))
        assertEquals(FieldError.PhoneFormat, errorFor(validForm.copy(phone = "12345"), RegistrationField.Phone))
        assertEquals("+919876543210", RegistrationValidator.normalizePhone("+91 (98765) 43210"))
    }

    @Test
    fun `users must be 18`() {
        assertEquals(FieldError.TooYoung, errorFor(validForm.copy(dateOfBirth = LocalDate.of(2008, 10, 18)), RegistrationField.DateOfBirth))
        assertNull(errorFor(validForm.copy(dateOfBirth = LocalDate.of(2008, 10, 17)), RegistrationField.DateOfBirth))
    }

    @Test
    fun `passwords need length, both cases, a digit and a symbol`() {
        listOf("Short#1", "nouppercase#1", "NOLOWERCASE#1", "NoDigits#here", "NoSymbol123").forEach { password ->
            assertEquals(password, FieldError.PasswordWeak, errorFor(validForm.copy(password = password), RegistrationField.Password))
        }
    }

    @Test
    fun `the confirmation must match`() {
        assertEquals(
            FieldError.PasswordMismatch,
            errorFor(validForm.copy(confirmPassword = "Site#2026b"), RegistrationField.ConfirmPassword)
        )
    }
}
