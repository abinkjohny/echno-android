package org.echno.feature.organization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.network.AppError
import org.echno.core.ui.LayoutWidth
import org.echno.feature.organization.ui.join.JoinOrganizationContent
import org.echno.feature.organization.ui.join.JoinOrganizationUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The join page with its state held here, so typing into the code boxes really changes it. */
@RunWith(AndroidJUnit4::class)
class JoinOrganizationContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var state by mutableStateOf(JoinOrganizationUiState())
    private var joins = 0

    private fun show(initial: JoinOrganizationUiState = JoinOrganizationUiState(), layoutWidth: LayoutWidth = LayoutWidth.Compact) {
        state = initial
        composeRule.setContent {
            EchnoTheme {
                JoinOrganizationContent(
                    uiState = state,
                    onCodeChange = { state = state.copy(code = it) },
                    onJoin = { joins++ },
                    onBack = {},
                    layoutWidth = layoutWidth
                )
            }
        }
    }

    private val codeField get() = composeRule.onNode(hasSetTextAction() and hasContentDescription("Invitation code"))

    @Test
    fun typing_keepsDigitsOnly_andAtMostFive() {
        show()

        codeField.performTextInput("0a4-82199")

        assertEquals("04821", state.code)
    }

    @Test
    fun pastingAWholeCode_fillsIt() {
        show()

        codeField.performTextReplacement("04821")

        assertEquals("04821", state.code)
        composeRule.onNodeWithText("Join").assertIsEnabled()
    }

    @Test
    fun join_waitsForAllFiveDigits() {
        show(JoinOrganizationUiState(code = "048"))

        composeRule.onNodeWithText("Join").assertIsNotEnabled()
    }

    @Test
    fun join_sendsTheCode_alsoOnATablet() {
        show(JoinOrganizationUiState(code = "04821"), LayoutWidth.Expanded)

        composeRule.onNodeWithText("Join").performClick()

        assertEquals(1, joins)
    }

    @Test
    fun errors_sayWhatIsWrongWithTheCode() {
        show(JoinOrganizationUiState(code = "11111", error = AppError.NotFound(null)))
        composeRule.onNodeWithText("No organization uses this code. Check the digits and try again.").assertIsDisplayed()

        state = state.copy(error = AppError.RateLimited(retryAfterSeconds = 61))
        composeRule.onNodeWithText("Too many tries. Try again in 2 minutes.").assertIsDisplayed()
    }
}
