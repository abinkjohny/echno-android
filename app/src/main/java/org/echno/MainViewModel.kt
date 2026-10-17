package org.echno

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import org.echno.core.auth.SessionManager
import org.echno.core.auth.SessionState

/** Activity-level state: the session, which decides the app's first screen. */
@HiltViewModel
class MainViewModel @Inject constructor(sessionManager: SessionManager) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionManager.state
}
