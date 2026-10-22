package org.echno.feature.organization

import java.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.echno.core.auth.FakeSessionStore
import org.echno.core.auth.FakeTokenService
import org.echno.core.auth.SessionManager
import org.echno.core.auth.SessionState
import org.echno.core.auth.StoredSession
import org.echno.core.auth.TokenResult
import org.echno.core.auth.accessTokenWithGroups
import org.echno.core.auth.tokens
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.session.SelectedOrganization
import org.echno.core.testing.MainDispatcherRule
import org.echno.feature.organization.data.OrganizationRepository
import org.echno.feature.organization.domain.Organization
import org.echno.feature.organization.ui.join.JoinOrganizationViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JoinOrganizationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeJoinRepository()
    private val tokenService = FakeTokenService()
    private val session =
        SessionManager(
            FakeSessionStore(StoredSession(tokens("before-join"), organizationId = null)),
            tokenService,
            SelectedOrganization(),
            Clock.systemUTC(),
            CoroutineScope(Dispatchers.Unconfined)
        )
    private val tornotron = Organization(7, "Tornotron Builders", null, isActive = true)

    private fun viewModel() = JoinOrganizationViewModel(repository, session)

    @Test
    fun `nothing is sent until all five digits are in`() = runTest {
        val viewModel = viewModel()
        viewModel.onCodeChange("0482")

        viewModel.join()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmit)
        assertTrue(repository.codes.isEmpty())
    }

    @Test
    fun `a good code refreshes the token and goes into the organization`() = runTest {
        repository.result = ApiResult.Success(tornotron)
        tokenService.refreshResult = TokenResult.Success(tokens(accessTokenWithGroups("/org-7")))
        val viewModel = viewModel()
        viewModel.onCodeChange("04821")

        viewModel.join()
        advanceUntilIdle()

        assertEquals(listOf("04821"), repository.codes)
        assertEquals(listOf(7L), session.memberOrganizationIds())
        assertEquals(SessionState.SignedIn(7), session.state.value)
    }

    @Test
    fun `a wrong code shows why and can be corrected`() = runTest {
        repository.result = ApiResult.Failure(AppError.NotFound("not found"))
        val viewModel = viewModel()
        viewModel.onCodeChange("11111")

        viewModel.join()
        advanceUntilIdle()

        assertEquals(AppError.NotFound("not found"), viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isJoining)
        assertEquals(SessionState.SignedIn(null), session.state.value)

        viewModel.onCodeChange("1111")
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `joined but offline for the refresh, Continue retries only the refresh`() = runTest {
        repository.result = ApiResult.Success(tornotron)
        tokenService.refreshResult = TokenResult.Failed(AppError.Network)
        val viewModel = viewModel()
        viewModel.onCodeChange("04821")

        viewModel.join()
        advanceUntilIdle()

        assertEquals(tornotron, viewModel.uiState.value.joined)
        assertEquals(SessionState.SignedIn(null), session.state.value)

        tokenService.refreshResult = TokenResult.Success(tokens(accessTokenWithGroups("/org-7")))
        viewModel.join()
        advanceUntilIdle()

        // The code was spent the first time; it isn't sent again.
        assertEquals(listOf("04821"), repository.codes)
        assertEquals(SessionState.SignedIn(7), session.state.value)
    }
}

private class FakeJoinRepository : OrganizationRepository {
    var result: ApiResult<Organization> = ApiResult.Failure(AppError.Network)
    val codes = mutableListOf<String>()

    override suspend fun getMyOrganizations(memberOf: List<Long>): ApiResult<List<Organization>> = error("not used here")

    override suspend fun joinWithInviteCode(code: String, memberOf: List<Long>): ApiResult<Organization> {
        codes += code
        return result
    }
}
