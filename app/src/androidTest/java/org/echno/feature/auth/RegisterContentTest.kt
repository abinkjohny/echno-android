package org.echno.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.FieldError
import org.echno.feature.auth.domain.Gender
import org.echno.feature.auth.domain.RegistrationField
import org.echno.feature.auth.ui.register.RegisterContent
import org.echno.feature.auth.ui.register.RegisterUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The registration form with its state held here instead of a ViewModel, so typing really changes it. */
@RunWith(AndroidJUnit4::class)
class RegisterContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var state by mutableStateOf(RegisterUiState())
    private var submits = 0

    private fun show(initial: RegisterUiState = RegisterUiState()) {
        state = initial
        composeRule.setContent {
            EchnoTheme {
                RegisterContent(
                    uiState = state,
                    onFormChange = { state = state.copy(form = it) },
                    onSubmit = { submits++ },
                    onSubmitErrorShown = {},
                    onBack = {}
                )
            }
        }
    }

    @Test
    fun typing_fillsTheForm() {
        show()

        composeRule.onNode(hasSetTextAction() and hasText("Full Name")).performTextInput("Asha Menon")
        composeRule.onNode(hasSetTextAction() and hasText("Email")).performTextInput("asha@example.com")

        assertEquals("Asha Menon", state.form.fullName)
        assertEquals("asha@example.com", state.form.email)
    }

    @Test
    fun choices_setGenderRoleAndTerms() {
        show()

        composeRule.onNodeWithText("Female").performScrollTo().performClick()
        composeRule.onNode(hasText("Role")).performScrollTo().performClick()
        composeRule.onNodeWithText("HR Manager").performClick()
        composeRule.onNodeWithText("I agree to the Terms of Service and Privacy Policy").performScrollTo().performClick()

        assertEquals(Gender.Female, state.form.gender)
        assertEquals(AccountRole.HrManager, state.form.role)
        assertTrue(state.form.acceptedTerms)
        composeRule.onNodeWithText("I agree to the Terms of Service and Privacy Policy").assertIsOn()
    }

    @Test
    fun errors_areShownUnderTheirFields() {
        show(
            RegisterUiState(
                errors =
                mapOf(
                    RegistrationField.Email to FieldError.EmailTaken,
                    RegistrationField.DateOfBirth to FieldError.TooYoung
                )
            )
        )

        composeRule.onNodeWithText("An account with this email already exists").assertIsDisplayed()
        composeRule.onNodeWithText("You must be at least 18 years old").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun createAccount_submits() {
        show()

        // The title also reads "Create Account"; the button is the one that can be clicked.
        composeRule.onNode(hasText("Create Account") and hasClickAction()).performScrollTo().performClick()

        assertEquals(1, submits)
    }
}
