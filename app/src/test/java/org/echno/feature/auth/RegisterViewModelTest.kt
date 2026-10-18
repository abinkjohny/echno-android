package org.echno.feature.auth

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.testing.MainDispatcherRule
import org.echno.feature.auth.data.RegistrationRepository
import org.echno.feature.auth.domain.FieldError
import org.echno.feature.auth.domain.RegistrationField
import org.echno.feature.auth.domain.RegistrationForm
import org.echno.feature.auth.ui.register.RegisterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRegistrationRepository()
    private val clock = Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneOffset.UTC)
    private val viewModel = RegisterViewModel(repository, clock)

    @Test
    fun `an invalid form is not sent`() = runTest {
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(repository.sent.isEmpty())
        assertEquals(FieldError.Required, viewModel.uiState.value.errors[RegistrationField.Email])
    }

    @Test
    fun `editing a field clears only its error`() {
        viewModel.submit()

        viewModel.onFormChange(RegistrationForm(email = "a"))

        val errors = viewModel.uiState.value.errors
        assertNull(errors[RegistrationField.Email])
        assertEquals(FieldError.Required, errors[RegistrationField.Username])
    }

    @Test
    fun `a valid form is sent once, showing progress meanwhile`() = runTest {
        repository.gate = CompletableDeferred()
        viewModel.onFormChange(validForm)

        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSubmitting)
        repository.gate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf(validForm), repository.sent)
    }

    @Test
    fun `success hands over the username to sign in with`() = runTest {
        viewModel.onFormChange(validForm.copy(username = " asha.menon "))

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("asha.menon", viewModel.uiState.value.registeredUsername)
    }

    @Test
    fun `a taken email is shown on the email field`() = runTest {
        repository.result = ApiResult.Failure(AppError.Conflict("User with email exists"))
        viewModel.onFormChange(validForm)

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(mapOf(RegistrationField.Email to FieldError.EmailTaken), state.errors)
        assertNull(state.submitError)
    }

    @Test
    fun `server field errors land on their fields`() = runTest {
        repository.result = ApiResult.Failure(AppError.Validation("Validation Failed", mapOf("userName" to "taken", "unknownField" to "x")))
        viewModel.onFormChange(validForm)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(mapOf(RegistrationField.Username to FieldError.Rejected), viewModel.uiState.value.errors)
    }

    @Test
    fun `other failures go to the snackbar once`() = runTest {
        repository.result = ApiResult.Failure(AppError.Network)
        viewModel.onFormChange(validForm)

        viewModel.submit()
        advanceUntilIdle()
        assertEquals(AppError.Network, viewModel.uiState.value.submitError)

        viewModel.onSubmitErrorShown()
        assertNull(viewModel.uiState.value.submitError)
    }
}

private class FakeRegistrationRepository : RegistrationRepository {
    var result: ApiResult<Unit> = ApiResult.Success(Unit)
    var gate: CompletableDeferred<Unit>? = null
    val sent = mutableListOf<RegistrationForm>()

    override suspend fun register(form: RegistrationForm): ApiResult<Unit> {
        sent += form
        gate?.await()
        return result
    }
}
