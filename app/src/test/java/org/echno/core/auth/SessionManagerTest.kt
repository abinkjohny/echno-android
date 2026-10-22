package org.echno.core.auth

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.echno.core.network.AppError
import org.echno.core.session.SelectedOrganization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerTest {
    private val now = Instant.parse("2026-10-17T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val selectedOrganization = SelectedOrganization()
    private val tokenService = FakeTokenService()

    private fun TestScope.manager(store: SessionStore): SessionManager {
        val manager = SessionManager(store, tokenService, selectedOrganization, clock, backgroundScope)
        runCurrent() // let the stored session be read (advanceUntilIdle skips backgroundScope work)
        return manager
    }

    @Test
    fun `nothing stored means signed out`() = runTest {
        assertEquals(SessionState.SignedOut(), manager(FakeSessionStore()).state.value)
    }

    @Test
    fun `a stored session is restored with its organization`() = runTest {
        val manager = manager(FakeSessionStore(StoredSession(tokens("a1"), organizationId = 5)))

        assertEquals(SessionState.SignedIn(5), manager.state.value)
        assertEquals(5L, selectedOrganization.id.value)
        assertEquals("a1", manager.accessToken())
    }

    @Test
    fun `signing in starts without an organization`() = runTest {
        val store = FakeSessionStore(StoredSession(null, organizationId = 9))
        val manager = manager(store)

        manager.signIn(tokens("a1"))

        assertEquals(SessionState.SignedIn(null), manager.state.value)
        assertEquals(StoredSession(tokens("a1"), null), store.stored)
    }

    @Test
    fun `selecting an organization is saved and sent`() = runTest {
        val store = FakeSessionStore(StoredSession(tokens("a1"), null))
        val manager = manager(store)

        manager.selectOrganization(7)

        assertEquals(SessionState.SignedIn(7), manager.state.value)
        assertEquals(7L, store.stored.organizationId)
        assertEquals(7L, selectedOrganization.id.value)
    }

    @Test
    fun `an expired access token is refreshed before use and the new one saved`() = runTest {
        val store = FakeSessionStore(StoredSession(tokens("old", expiresAt = now.epochSecond), 5))
        tokenService.refreshResult = TokenResult.Success(tokens("new"))
        val manager = manager(store)

        assertEquals("new", manager.accessToken())
        assertEquals(listOf("refresh-old"), tokenService.refreshedWith)
        assertEquals("new", store.stored.tokens?.accessToken)
    }

    @Test
    fun `refreshing memberships gets a new token even though the current one is still valid`() = runTest {
        val store = FakeSessionStore(StoredSession(tokens("a1"), null))
        tokenService.refreshResult = TokenResult.Success(tokens("a2"))
        val manager = manager(store)

        assertTrue(manager.refreshMemberships())
        assertEquals("a2", manager.accessToken())
        assertEquals("a2", store.stored.tokens?.accessToken)
    }

    @Test
    fun `refreshing memberships offline keeps the session and says so`() = runTest {
        tokenService.refreshResult = TokenResult.Failed(AppError.Network)
        val manager = manager(FakeSessionStore(StoredSession(tokens("a1"), null)))

        assertFalse(manager.refreshMemberships())
        assertEquals(SessionState.SignedIn(null), manager.state.value)
        assertEquals("a1", manager.accessToken())
    }

    @Test
    fun `clearing an expired session never erases a sign-in that came after it`() = runTest {
        val store = FakeSessionStore(StoredSession(tokens("old"), 5))
        tokenService.refreshResult = TokenResult.Rejected
        val manager = manager(store)

        manager.refreshAccessToken("old") // expired: the clear is queued, not run yet
        manager.signIn(tokens("a2"))
        runCurrent() // now the queued clear runs

        assertEquals(SessionState.SignedIn(null), manager.state.value)
        assertEquals("a2", store.stored.tokens?.accessToken)
    }

    @Test
    fun `a token another request already refreshed is reused`() = runTest {
        tokenService.refreshResult = TokenResult.Success(tokens("new"))
        val manager = manager(FakeSessionStore(StoredSession(tokens("old"), 5)))

        manager.refreshAccessToken("old")
        val second = manager.refreshAccessToken("old")

        assertEquals("new", second)
        assertEquals(1, tokenService.refreshedWith.size)
    }

    @Test
    fun `a refused refresh token ends the session`() = runTest {
        val store = FakeSessionStore(StoredSession(tokens("old"), 5))
        tokenService.refreshResult = TokenResult.Rejected
        val manager = manager(store)

        assertNull(manager.refreshAccessToken("old"))
        runCurrent()

        assertEquals(SessionState.SignedOut(sessionExpired = true), manager.state.value)
        assertNull(selectedOrganization.id.value)
        assertEquals(StoredSession(null, null), store.stored)
    }

    @Test
    fun `an unreachable Keycloak keeps the session`() = runTest {
        tokenService.refreshResult = TokenResult.Failed(AppError.Network)
        val manager = manager(FakeSessionStore(StoredSession(tokens("old"), 5)))

        assertNull(manager.refreshAccessToken("old"))

        assertEquals(SessionState.SignedIn(5), manager.state.value)
    }

    @Test
    fun `signing out clears the device and ends the Keycloak session`() = runTest {
        val store = FakeSessionStore(StoredSession(tokens("a1"), 5))
        val manager = manager(store)

        manager.signOut()
        runCurrent()

        assertEquals(SessionState.SignedOut(), manager.state.value)
        assertNull(manager.accessToken())
        assertEquals(StoredSession(null, null), store.stored)
        assertEquals(listOf("refresh-a1"), tokenService.revoked)
    }
}
