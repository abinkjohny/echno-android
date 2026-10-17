package org.echno.feature.auth.ui.welcome

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.echno.R
import org.echno.core.auth.SignInFlow
import org.echno.core.auth.SignInResult
import org.echno.core.ui.messageRes

/** [isSigningIn]: the browser has returned and tokens are being fetched. */
data class WelcomeUiState(val isSigningIn: Boolean = false, @param:StringRes val errorRes: Int? = null)

/**
 * Starts and finishes a sign-in. On success there is nothing to show: the session changes and the
 * app's navigation replaces this screen.
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(private val signInFlow: SignInFlow) : ViewModel() {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    /** The Intent that opens the login page, or null (with an error shown) if it can't be opened. */
    fun signInIntent(loginHint: String?): Intent? = try {
        _uiState.update { it.copy(errorRes = null) }
        signInFlow.createIntent(loginHint)
    } catch (e: ActivityNotFoundException) {
        _uiState.update { it.copy(errorRes = R.string.sign_in_error_no_browser) }
        null
    }

    fun onSignInResult(result: Intent?) {
        _uiState.update { it.copy(isSigningIn = true, errorRes = null) }
        viewModelScope.launch {
            when (val outcome = signInFlow.complete(result)) {
                // Stay "signing in" until navigation moves on, so the button can't be pressed twice.
                SignInResult.Success -> Unit
                SignInResult.Cancelled -> _uiState.update { it.copy(isSigningIn = false) }
                is SignInResult.Failed -> _uiState.update { it.copy(isSigningIn = false, errorRes = outcome.error.messageRes()) }
            }
        }
    }
}
