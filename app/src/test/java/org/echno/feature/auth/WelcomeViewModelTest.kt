package org.echno.feature.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.echno.R
import org.echno.core.auth.SignInFlow
import org.echno.core.auth.SignInResult
import org.echno.core.network.AppError
import org.echno.core.testing.MainDispatcherRule
import org.echno.feature.auth.ui.welcome.WelcomeUiState
import org.echno.feature.auth.ui.welcome.WelcomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class WelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val signInFlow = FakeSignInFlow()
    private val viewModel = WelcomeViewModel(signInFlow)

    @Test
    fun `the login hint is passed to the login page`() {
        viewModel.signInIntent("new.user")

        assertEquals(listOf("new.user"), signInFlow.loginHints)
    }

    @Test
    fun `no browser shows an error instead of crashing`() {
        signInFlow.noBrowser = true

        assertNull(viewModel.signInIntent(null))
        assertEquals(R.string.sign_in_error_no_browser, viewModel.uiState.value.errorRes)
    }

    @Test
    fun `while tokens are fetched the screen shows progress`() = runTest {
        signInFlow.gate = CompletableDeferred()

        viewModel.onSignInResult(null)
        advanceUntilIdle()

        assertEquals(WelcomeUiState(isSigningIn = true), viewModel.uiState.value)
    }

    @Test
    fun `closing the login page is not an error`() = runTest {
        signInFlow.result = SignInResult.Cancelled

        viewModel.onSignInResult(null)
        advanceUntilIdle()

        assertEquals(WelcomeUiState(), viewModel.uiState.value)
    }

    @Test
    fun `a failed sign-in shows why`() = runTest {
        signInFlow.result = SignInResult.Failed(AppError.Network)

        viewModel.onSignInResult(null)
        advanceUntilIdle()

        assertEquals(WelcomeUiState(errorRes = R.string.error_network), viewModel.uiState.value)
    }

    @Test
    fun `success keeps showing progress until navigation moves on`() = runTest {
        signInFlow.result = SignInResult.Success

        viewModel.onSignInResult(null)
        advanceUntilIdle()

        assertEquals(WelcomeUiState(isSigningIn = true), viewModel.uiState.value)
    }
}

private class FakeSignInFlow : SignInFlow {
    var result: SignInResult = SignInResult.Success
    var noBrowser = false
    var gate: CompletableDeferred<Unit>? = null
    val loginHints = mutableListOf<String?>()

    override fun createIntent(loginHint: String?): Intent {
        loginHints += loginHint
        if (noBrowser) throw ActivityNotFoundException()
        return Intent()
    }

    override suspend fun complete(result: Intent?): SignInResult {
        gate?.await()
        return this.result
    }
}
