package org.echno.feature.organization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.ui.LayoutWidth
import org.echno.feature.organization.domain.Organization
import org.echno.feature.organization.ui.OrganizationPickerContent
import org.echno.feature.organization.ui.OrganizationPickerUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationPickerContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var selected: Long? = null
    private var signOuts = 0
    private var retries = 0

    private fun show(state: OrganizationPickerUiState) {
        composeRule.setContent {
            EchnoTheme {
                OrganizationPickerContent(
                    uiState = state,
                    onSelect = { selected = it },
                    onRetry = { retries++ },
                    onSignOut = { signOuts++ },
                    // Pinned, so the test behaves the same on a phone and a tablet emulator.
                    layoutWidth = LayoutWidth.Compact
                )
            }
        }
    }

    @Test
    fun tappingAnOrganization_selectsIt_butNotAnInactiveOne() {
        show(
            OrganizationPickerUiState.Loaded(
                listOf(
                    Organization(5, "Malabar Builders", "Kozhikode", isActive = true),
                    Organization(12, "Periyar Infra", null, isActive = false)
                )
            )
        )

        composeRule.onNodeWithText("Periyar Infra").performClick()
        assertEquals(null, selected)
        composeRule.onNodeWithText("Malabar Builders").performClick()
        assertEquals(5L, selected)
    }

    @Test
    fun noOrganization_explainsAndOffersCheckAgainAndSignOut() {
        show(OrganizationPickerUiState.Loaded(emptyList()))

        composeRule.onNodeWithText("You're not in an organization yet").assertIsDisplayed()
        composeRule.onNodeWithText("Check Again").performClick()
        // Sign Out is in the header and in the empty state.
        composeRule.onAllNodesWithText("Sign Out")[1].performClick()

        assertEquals(1, retries)
        assertEquals(1, signOuts)
    }
}
