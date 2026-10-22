package org.echno.feature.organization.ui.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.echno.core.auth.SessionManager
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.feature.organization.data.OrganizationRepository
import org.echno.feature.organization.domain.Organization

/** Invitation codes are five digits (the backend generates 10000–99999 and accepts exactly five characters). */
const val INVITE_CODE_LENGTH = 5

data class JoinOrganizationUiState(
    val code: String = "",
    val isJoining: Boolean = false,
    val error: AppError? = null,
    /**
     * Set when the server has added the user but the session couldn't pick the membership up
     * (offline right after joining). The code is spent; only the token refresh is left to retry.
     */
    val joined: Organization? = null
) {
    val canSubmit: Boolean get() = !isJoining && (joined != null || code.length == INVITE_CODE_LENGTH)
}

@HiltViewModel
class JoinOrganizationViewModel @Inject constructor(
    private val repository: OrganizationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(JoinOrganizationUiState())
    val uiState: StateFlow<JoinOrganizationUiState> = _uiState.asStateFlow()

    fun onCodeChange(code: String) {
        if (_uiState.value.joined != null) return
        _uiState.update { it.copy(code = code, error = null) }
    }

    /**
     * Redeems the code, then makes the new organization the one the app works in. Navigation
     * follows the session: once it is selected, Profile replaces the organization screens.
     */
    fun join() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(isJoining = true, error = null) }
        viewModelScope.launch {
            val joined = state.joined
            if (joined != null) {
                enter(joined)
                return@launch
            }
            when (val result = repository.joinWithInviteCode(state.code, sessionManager.memberOrganizationIds())) {
                is ApiResult.Success -> enter(result.data)
                is ApiResult.Failure -> _uiState.update { it.copy(isJoining = false, error = result.error) }
            }
        }
    }

    private suspend fun enter(organization: Organization) {
        if (sessionManager.refreshMemberships()) {
            sessionManager.selectOrganization(organization.id)
        } else {
            _uiState.update { it.copy(isJoining = false, joined = organization, error = AppError.Network) }
        }
    }
}
