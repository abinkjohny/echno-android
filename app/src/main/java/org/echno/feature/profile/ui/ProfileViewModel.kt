package org.echno.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.session.SelectedOrganization
import org.echno.feature.profile.data.ProfileRepository
import org.echno.feature.profile.domain.Profile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Error(val error: AppError) : ProfileUiState

    data class Loaded(val profile: Profile) : ProfileUiState
}

/**
 * Loads the signed-in user's profile once. Switching organization goes back through the picker,
 * which removes this screen, so a new ViewModel loads the new organization's view.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val selectedOrganization: SelectedOrganization
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _uiState.value = ProfileUiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getMyProfile(selectedOrganization.id.value)) {
                    is ApiResult.Success -> ProfileUiState.Loaded(result.data)
                    is ApiResult.Failure -> ProfileUiState.Error(result.error)
                }
        }
    }
}
