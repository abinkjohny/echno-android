package org.echno.feature.profile

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.session.SelectedOrganization
import org.echno.core.testing.MainDispatcherRule
import org.echno.feature.profile.data.ProfileRepository
import org.echno.feature.profile.domain.Profile
import org.echno.feature.profile.ui.ProfileUiState
import org.echno.feature.profile.ui.ProfileViewModel
import org.echno.feature.profile.ui.initials
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProfileRepository()
    private val selectedOrganization = SelectedOrganization().apply { select(5) }

    @Test
    fun `loads the profile for the selected organization`() = runTest {
        val viewModel = ProfileViewModel(repository, selectedOrganization)
        advanceUntilIdle()

        assertEquals(listOf<Long?>(5), repository.requests)
        assertEquals(ProfileUiState.Loaded(asha), viewModel.uiState.value)
    }

    @Test
    fun `a failure is shown, and retry loads again`() = runTest {
        repository.result = ApiResult.Failure(AppError.Network)
        val viewModel = ProfileViewModel(repository, selectedOrganization)
        advanceUntilIdle()
        assertEquals(ProfileUiState.Error(AppError.Network), viewModel.uiState.value)

        repository.result = ApiResult.Success(asha)
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(ProfileUiState.Loaded(asha), viewModel.uiState.value)
        assertEquals(2, repository.requests.size)
    }

    @Test
    fun `initials come from the first two words, or the start of an email`() {
        assertEquals("AM", initials("Asha Menon"))
        assertEquals("AK", initials("Abin K Johny"))
        assertEquals("A", initials("asha@example.com"))
        assertEquals("?", initials(" "))
    }
}

val asha =
    Profile(
        id = 42,
        name = "Asha Menon",
        email = "asha@example.com",
        phone = null,
        role = null,
        gender = null,
        dateOfBirth = null,
        bloodGroup = null,
        address = null,
        emergencyContact = null,
        qualification = null,
        experienceYears = null,
        skills = emptyList(),
        certifications = emptyList(),
        memberSince = null,
        organization = null
    )

class FakeProfileRepository : ProfileRepository {
    var result: ApiResult<Profile> = ApiResult.Success(asha)
    val requests = mutableListOf<Long?>()

    override suspend fun getMyProfile(organizationId: Long?): ApiResult<Profile> {
        requests += organizationId
        return result
    }
}
