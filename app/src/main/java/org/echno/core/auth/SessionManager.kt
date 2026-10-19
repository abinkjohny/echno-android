package org.echno.core.auth

import java.time.Clock
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.echno.core.coroutines.ApplicationScope
import org.echno.core.session.SelectedOrganization

/**
 * The one owner of the signed-in session: the tokens, the selected organization and [state].
 *
 * Two kinds of caller:
 * - Screens call the suspend functions ([signIn], [selectOrganization], [signOut]).
 * - The HTTP layer calls [accessToken] and [refreshAccessToken]. These block, because OkHttp
 *   interceptors run on its own background threads and need an answer before the request goes out.
 */
@Singleton
class SessionManager @Inject constructor(
    private val store: SessionStore,
    private val tokenService: TokenService,
    private val selectedOrganization: SelectedOrganization,
    private val clock: Clock,
    @param:ApplicationScope private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    // AtomicReference: a refresh only installs its result if nobody signed out in the meantime.
    private val tokens = AtomicReference<AuthTokens?>(null)
    private val restored = CompletableDeferred<Unit>()
    private val refreshLock = Any()

    // Every write to the store goes through this lock and first checks that it still matches the
    // session in memory. Without it, a refresh finishing just after a sign-out could save its tokens
    // again (signed back in on the next launch), or a late clear could erase a newer sign-in.
    private val storeLock = Mutex()

    init {
        scope.launch { restore() }
    }

    private suspend fun restore() {
        val stored = store.read()
        val organizationId = stored.organizationId.takeIf { stored.tokens != null }
        tokens.set(stored.tokens)
        selectedOrganization.select(organizationId)
        _state.value = if (stored.tokens == null) SessionState.SignedOut() else SessionState.SignedIn(organizationId)
        restored.complete(Unit)
    }

    /** Called once Keycloak has issued tokens for a new sign-in. */
    suspend fun signIn(newTokens: AuthTokens) {
        storeLock.withLock {
            store.saveOrganization(null)
            store.saveTokens(newTokens)
            tokens.set(newTokens)
        }
        selectedOrganization.select(null)
        _state.value = SessionState.SignedIn(organizationId = null)
    }

    /** Sets the organization sent as `X-Organization-Id`; null goes back to the organization picker. */
    suspend fun selectOrganization(organizationId: Long?) {
        storeLock.withLock {
            if (tokens.get() == null) return
            store.saveOrganization(organizationId)
        }
        selectedOrganization.select(organizationId)
        _state.value = SessionState.SignedIn(organizationId)
    }

    /** Signs out at once on the device, then tells Keycloak in the background. */
    suspend fun signOut() {
        val ended = tokens.getAndSet(null) ?: return
        selectedOrganization.select(null)
        _state.value = SessionState.SignedOut()
        clearStoreIfSignedOut()
        ended.refreshToken?.let { refreshToken -> scope.launch { tokenService.revoke(refreshToken) } }
    }

    /** The organizations the signed-in user belongs to, as the current access token lists them. */
    fun memberOrganizationIds(): List<Long> = tokens.get()?.let { AccessTokenClaims.organizationIds(it.accessToken) }.orEmpty()

    /**
     * Blocking. The access token to send, refreshed first when it has expired, or null when signed out.
     * Waits for the stored session to be read, so requests made right after launch are authenticated.
     */
    fun accessToken(): String? {
        if (!restored.isCompleted) runBlocking { restored.await() }
        val current = tokens.get() ?: return null
        if (!current.isAccessTokenExpired(clock.instant())) return current.accessToken
        // If the refresh fails for a network reason, send the old token: the request then fails with
        // its own error instead of being sent without credentials.
        return refreshAccessToken(current.accessToken) ?: tokens.get()?.accessToken
    }

    /**
     * Blocking. Gets a new access token because [rejectedToken] was refused or has expired.
     * Returns null when there is none to be had: signed out, or Keycloak unreachable.
     *
     * Several requests can fail with 401 at once. The lock makes them queue: the first refreshes,
     * the rest see the token has already changed and reuse the new one. Keycloak rotates refresh
     * tokens, so two refreshes with the same one would be wasteful at best.
     */
    fun refreshAccessToken(rejectedToken: String): String? = synchronized(refreshLock) {
        val current = tokens.get() ?: return null
        if (current.accessToken != rejectedToken) return current.accessToken

        val refreshToken = current.refreshToken
        if (refreshToken == null) {
            endExpiredSession(current)
            return null
        }
        when (val result = tokenService.refresh(refreshToken)) {
            is TokenResult.Success -> {
                if (!tokens.compareAndSet(current, result.tokens)) return null // signed out meanwhile
                // Saved before returning: Keycloak has already invalidated the old refresh token.
                runBlocking {
                    storeLock.withLock {
                        // A sign-out may have happened since the line above; then there's nothing to save.
                        if (tokens.get() === result.tokens) store.saveTokens(result.tokens)
                    }
                }
                result.tokens.accessToken
            }
            TokenResult.Rejected -> {
                endExpiredSession(current)
                null
            }
            is TokenResult.Failed -> null
        }
    }

    private fun endExpiredSession(expired: AuthTokens) {
        if (!tokens.compareAndSet(expired, null)) return
        selectedOrganization.select(null)
        _state.value = SessionState.SignedOut(sessionExpired = true)
        scope.launch { clearStoreIfSignedOut() }
    }

    // Skipped when someone signed in again before this ran: the store then holds the new session.
    private suspend fun clearStoreIfSignedOut() = storeLock.withLock {
        if (tokens.get() == null) store.clear()
    }
}
