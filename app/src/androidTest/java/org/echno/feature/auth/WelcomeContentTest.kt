package org.echno.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.feature.auth.ui.welcome.WelcomeContent
import org.echno.feature.auth.ui.welcome.WelcomeNotice
import org.echno.feature.auth.ui.welcome.WelcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var signIns = 0
    private var createAccounts = 0

    private fun show(state: WelcomeUiState = WelcomeUiState(), notice: WelcomeNotice? = null) {
        composeRule.setContent {
            EchnoTheme {
                WelcomeContent(uiState = state, notice = notice, onSignIn = { signIns++ }, onCreateAccount = { createAccounts++ })
            }
        }
    }

    @Test
    fun buttons_reachTheirCallbacks() {
        show()

        composeRule.onNodeWithText("Sign in").performClick()
        composeRule.onNodeWithText("Create Account").performClick()

        assertEquals(1, signIns)
        assertEquals(1, createAccounts)
    }

    @Test
    fun afterRegistering_theUsernameIsShown() {
        show(notice = WelcomeNotice.AccountCreated("asha.menon"))

        composeRule.onNodeWithText("Account created for asha.menon. Sign In to Continue.").assertIsDisplayed()
    }

    @Test
    fun whileSigningIn_createAccountIsDisabled() {
        show(WelcomeUiState(isSigningIn = true))

        composeRule.onNodeWithText("Create Account").assertIsNotEnabled()
    }
}
