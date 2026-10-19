package org.echno.feature.organization.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.echno.core.auth.SessionManager
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.feature.organization.data.OrganizationRepository
import org.echno.feature.organization.domain.Organization

sealed interface OrganizationPickerUiState {
    data object Loading : OrganizationPickerUiState

    data class Error(val error: AppError) : OrganizationPickerUiState

    /** Empty: signed in, but nobody has added the user to an organization yet (e.g. just registered). */
    data class Loaded(val organizations: List<Organization>) : OrganizationPickerUiState
}

@HiltViewModel
class OrganizationPickerViewModel @Inject constructor(
    private val repository: OrganizationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<OrganizationPickerUiState>(OrganizationPickerUiState.Loading)
    val uiState: StateFlow<OrganizationPickerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _uiState.value = OrganizationPickerUiState.Loading
        load()
    }

    /** Navigation follows the session: once an organization is selected, Home replaces this screen. */
    fun select(organizationId: Long) {
        viewModelScope.launch { sessionManager.selectOrganization(organizationId) }
    }

    fun signOut() {
        viewModelScope.launch { sessionManager.signOut() }
    }

    private fun load() {
        viewModelScope.launch {
            when (val result = repository.getMyOrganizations(sessionManager.memberOrganizationIds())) {
                is ApiResult.Success -> {
                    val organizations = result.data
                    // Nothing to choose between: go straight in, still showing progress. An inactive
                    // one is listed instead, disabled, as it would be among several.
                    if (organizations.size == 1 && organizations.single().isActive) {
                        sessionManager.selectOrganization(organizations.single().id)
                    } else {
                        _uiState.value = OrganizationPickerUiState.Loaded(organizations)
                    }
                }
                is ApiResult.Failure -> _uiState.value = OrganizationPickerUiState.Error(result.error)
            }
        }
    }
}
