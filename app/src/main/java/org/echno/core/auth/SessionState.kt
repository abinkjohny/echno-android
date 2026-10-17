package org.echno.core.auth

/** Where the user stands. The app's navigation picks its first screen from this. */
sealed interface SessionState {
    /** Reading the stored session at launch. */
    data object Loading : SessionState

    /** [sessionExpired]: Keycloak ended the session (tokens expired or revoked), not the user. */
    data class SignedOut(val sessionExpired: Boolean = false) : SessionState

    /** [organizationId] is null until the user picks the organization to work in. */
    data class SignedIn(val organizationId: Long?) : SessionState
}
