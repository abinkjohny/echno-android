package org.echno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.echno.core.auth.SessionManager
import org.echno.core.auth.SessionState

/** Activity-level state: the session, which decides the app's first screen, and the account actions. */
@HiltViewModel
class MainViewModel @Inject constructor(private val sessionManager: SessionManager) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionManager.state

    /** Offer "Switch organization" only when there is another one to switch to. */
    val canSwitchOrganization: StateFlow<Boolean> =
        sessionManager.state
            .map { sessionManager.memberOrganizationIds().size > 1 }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun switchOrganization() {
        viewModelScope.launch { sessionManager.selectOrganization(null) }
    }

    fun signOut() {
        viewModelScope.launch { sessionManager.signOut() }
    }
}
