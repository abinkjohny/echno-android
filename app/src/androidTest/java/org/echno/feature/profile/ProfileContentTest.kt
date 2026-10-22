package org.echno.feature.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.network.AppError
import org.echno.core.ui.AccountActions
import org.echno.core.ui.LayoutWidth
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.organization.domain.Organization
import org.echno.feature.profile.domain.Profile
import org.echno.feature.profile.ui.ProfileContent
import org.echno.feature.profile.ui.ProfileUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var retries = 0

    private val profile =
        Profile(
            id = 42,
            name = "Asha Menon",
            email = "asha@example.com",
            phone = null,
            role = AccountRole.HrManager,
            gender = null,
            dateOfBirth = null,
            bloodGroup = null,
            address = null,
            emergencyContact = null,
            qualification = null,
            experienceYears = 1,
            skills = listOf("Payroll"),
            certifications = emptyList(),
            memberSince = LocalDate.of(2026, 1, 15),
            organization = Organization(5, "Malabar Builders", null, isActive = true)
        )

    private var switches = 0
    private var signOuts = 0

    private val account = AccountActions(canSwitchOrganization = true, onSwitchOrganization = { switches++ }, onSignOut = { signOuts++ })

    // The layout is pinned, so the tests behave the same on a phone and a tablet emulator.
    private fun show(state: ProfileUiState, layoutWidth: LayoutWidth = LayoutWidth.Compact) {
        composeRule.setContent {
            EchnoTheme { ProfileContent(uiState = state, onRetry = { retries++ }, account = account, layoutWidth = layoutWidth) }
        }
    }

    @Test
    fun loaded_showsTheFilledDetails_andHidesEmptySections() {
        show(ProfileUiState.Loaded(profile))

        composeRule.onNodeWithText("Asha Menon").assertIsDisplayed()
        composeRule.onNodeWithText("HR Manager").assertIsDisplayed()
        composeRule.onNodeWithText("Malabar Builders").assertIsDisplayed()
        composeRule.onNodeWithText("asha@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("1 Year").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Payroll").performScrollTo().assertIsDisplayed()
        // Nothing personal is filled in, so that card isn't shown at all.
        composeRule.onNodeWithText("Personal").assertDoesNotExist()
        composeRule.onNodeWithText("Phone").assertDoesNotExist()
    }

    @Test
    fun error_offersRetry() {
        show(ProfileUiState.Error(AppError.Network))

        composeRule.onNodeWithText("Can't reach Echno. Check your internet connection.").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun accountMenu_switchesOrganizationAndSignsOut() {
        show(ProfileUiState.Loaded(profile))

        composeRule.onNodeWithText("Account").performClick()
        composeRule.onNodeWithText("Switch Organization").performClick()
        composeRule.onNodeWithText("Account").performClick()
        composeRule.onNodeWithText("Sign Out").performClick()

        assertEquals(1, switches)
        assertEquals(1, signOuts)
    }

    @Test
    fun onATablet_theAccountActionsAreButtons() {
        show(ProfileUiState.Loaded(profile), LayoutWidth.Expanded)

        composeRule.onNodeWithText("Asha Menon").assertIsDisplayed()
        composeRule.onNodeWithText("Switch Organization").performClick()
        composeRule.onNodeWithText("Sign Out").performClick()

        assertEquals(1, switches)
        assertEquals(1, signOuts)
    }
}
