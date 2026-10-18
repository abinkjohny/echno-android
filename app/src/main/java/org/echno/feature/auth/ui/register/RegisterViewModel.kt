package org.echno.feature.auth.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.feature.auth.data.RegistrationRepository
import org.echno.feature.auth.domain.FieldError
import org.echno.feature.auth.domain.RegistrationField
import org.echno.feature.auth.domain.RegistrationForm
import org.echno.feature.auth.domain.RegistrationValidator

data class RegisterUiState(
    val form: RegistrationForm = RegistrationForm(),
    val errors: Map<RegistrationField, FieldError> = emptyMap(),
    val isSubmitting: Boolean = false,
    /** A failure that isn't about one field (no connection, server error), shown once in a snackbar. */
    val submitError: AppError? = null,
    /** Set when the account exists; the screen then moves on to signing in. */
    val registeredUsername: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: RegistrationRepository, private val clock: Clock) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /** Every keystroke and choice. A field's error disappears as soon as the user changes that field. */
    fun onFormChange(form: RegistrationForm) {
        _uiState.update { state ->
            state.copy(form = form, errors = state.errors.filterKeys { field -> field.valueIn(form) == field.valueIn(state.form) })
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val errors = RegistrationValidator.validate(state.form, LocalDate.now(clock))
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        _uiState.update { it.copy(errors = emptyMap(), isSubmitting = true) }
        viewModelScope.launch {
            when (val result = repository.register(state.form)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSubmitting = false, registeredUsername = state.form.username.trim()) }
                is ApiResult.Failure -> _uiState.update { it.withFailure(result.error) }
            }
        }
    }

    fun onSubmitErrorShown() {
        _uiState.update { it.copy(submitError = null) }
    }

    private fun RegisterUiState.withFailure(error: AppError): RegisterUiState {
        val fieldErrors =
            when (error) {
                // The backend answers 409 when the email is already registered.
                is AppError.Conflict -> mapOf(RegistrationField.Email to FieldError.EmailTaken)
                // Bean Validation names the DTO's fields; anything the app can't place goes to the snackbar.
                is AppError.Validation -> error.fieldErrors.keys.mapNotNull(::fieldForServerName).associateWith { FieldError.Rejected }
                else -> emptyMap()
            }
        return copy(isSubmitting = false, errors = fieldErrors, submitError = error.takeIf { fieldErrors.isEmpty() })
    }

    private fun fieldForServerName(name: String): RegistrationField? = when (name) {
        "userName" -> RegistrationField.Username
        "name" -> RegistrationField.FullName
        "email" -> RegistrationField.Email
        "phone" -> RegistrationField.Phone
        "dateOfBirth" -> RegistrationField.DateOfBirth
        "gender" -> RegistrationField.Gender
        "role" -> RegistrationField.Role
        "password" -> RegistrationField.Password
        else -> null
    }

    private fun RegistrationField.valueIn(form: RegistrationForm): Any? = when (this) {
        RegistrationField.FullName -> form.fullName
        RegistrationField.Username -> form.username
        RegistrationField.Email -> form.email
        RegistrationField.Phone -> form.phone
        RegistrationField.DateOfBirth -> form.dateOfBirth
        RegistrationField.Gender -> form.gender
        RegistrationField.Role -> form.role
        RegistrationField.Password -> form.password
        // Changing either password can fix a mismatch.
        RegistrationField.ConfirmPassword -> form.confirmPassword to form.password
        RegistrationField.Terms -> form.acceptedTerms
    }
}
