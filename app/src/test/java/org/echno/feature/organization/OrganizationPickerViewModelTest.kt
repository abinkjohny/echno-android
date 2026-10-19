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
import org.echno.core.auth.accessTokenWithGroups
import org.echno.core.auth.tokens
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.session.SelectedOrganization
import org.echno.core.testing.MainDispatcherRule
import org.echno.feature.organization.data.OrganizationRepository
import org.echno.feature.organization.domain.Organization
import org.echno.feature.organization.ui.OrganizationPickerUiState
import org.echno.feature.organization.ui.OrganizationPickerViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OrganizationPickerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOrganizationRepository()
    private val session =
        SessionManager(
            FakeSessionStore(StoredSession(tokens(accessTokenWithGroups("/org-5", "/org-12")), organizationId = null)),
            FakeTokenService(),
            SelectedOrganization(),
            Clock.systemUTC(),
            CoroutineScope(Dispatchers.Unconfined)
        )

    private val malabar = Organization(5, "Malabar Builders", null, isActive = true)
    private val periyar = Organization(12, "Periyar Infra", null, isActive = true)

    @Test
    fun `asks about the organizations in the token`() = runTest {
        OrganizationPickerViewModel(repository, session)
        advanceUntilIdle()

        assertEquals(listOf(listOf(5L, 12L)), repository.requests)
    }

    @Test
    fun `several organizations are listed to choose from`() = runTest {
        repository.result = ApiResult.Success(listOf(malabar, periyar))

        val viewModel = OrganizationPickerViewModel(repository, session)
        advanceUntilIdle()

        assertEquals(OrganizationPickerUiState.Loaded(listOf(malabar, periyar)), viewModel.uiState.value)
    }

    @Test
    fun `a single organization is chosen automatically`() = runTest {
        repository.result = ApiResult.Success(listOf(malabar))

        val viewModel = OrganizationPickerViewModel(repository, session)
        advanceUntilIdle()

        assertEquals(OrganizationPickerUiState.Loading, viewModel.uiState.value)
        assertEquals(SessionState.SignedIn(5), session.state.value)
    }

    @Test
    fun `a single inactive organization is listed, not chosen`() = runTest {
        val inactive = malabar.copy(isActive = false)
        repository.result = ApiResult.Success(listOf(inactive))

        val viewModel = OrganizationPickerViewModel(repository, session)
        advanceUntilIdle()

        assertEquals(OrganizationPickerUiState.Loaded(listOf(inactive)), viewModel.uiState.value)
        assertEquals(SessionState.SignedIn(null), session.state.value)
    }

    @Test
    fun `choosing one selects it for the session`() = runTest {
        repository.result = ApiResult.Success(listOf(malabar, periyar))
        val viewModel = OrganizationPickerViewModel(repository, session)
        advanceUntilIdle()

        viewModel.select(12)
        advanceUntilIdle()

        assertEquals(SessionState.SignedIn(12), session.state.value)
    }

    @Test
    fun `an error can be retried`() = runTest {
        repository.result = ApiResult.Failure(AppError.Network)
        val viewModel = OrganizationPickerViewModel(repository, session)
        advanceUntilIdle()
        assertEquals(OrganizationPickerUiState.Error(AppError.Network), viewModel.uiState.value)

        repository.result = ApiResult.Success(emptyList())
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(OrganizationPickerUiState.Loaded(emptyList()), viewModel.uiState.value)
    }

    @Test
    fun `signing out ends the session`() = runTest {
        val viewModel = OrganizationPickerViewModel(repository, session)

        viewModel.signOut()
        advanceUntilIdle()

        assertEquals(SessionState.SignedOut(), session.state.value)
    }
}

private class FakeOrganizationRepository : OrganizationRepository {
    var result: ApiResult<List<Organization>> = ApiResult.Success(emptyList())
    val requests = mutableListOf<List<Long>>()

    override suspend fun getMyOrganizations(memberOf: List<Long>): ApiResult<List<Organization>> {
        requests += memberOf
        return result
    }
}
