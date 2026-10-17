package org.echno.core.network

import java.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.echno.core.auth.FakeSessionStore
import org.echno.core.auth.FakeTokenService
import org.echno.core.auth.SessionManager
import org.echno.core.auth.StoredSession
import org.echno.core.auth.TokenResult
import org.echno.core.auth.tokens
import org.echno.core.session.SelectedOrganization
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// AuthInterceptor + TokenAuthenticator in a real OkHttpClient against a local server.
class TokenAuthenticatorTest {
    private val server = MockWebServer()
    private val tokenService = FakeTokenService()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun client(store: FakeSessionStore): OkHttpClient {
        // Unconfined: the stored session is read immediately, inside the constructor.
        val session = SessionManager(store, tokenService, SelectedOrganization(), Clock.systemUTC(), CoroutineScope(Dispatchers.Unconfined))
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(session))
            .authenticator(TokenAuthenticator(session))
            .build()
    }

    private fun OkHttpClient.get(): Int = newCall(Request.Builder().url(server.url("/api/v1/project")).build()).execute().use { it.code }

    @Test
    fun `signed in requests carry the access token`() {
        server.enqueue(MockResponse.Builder().body("[]").build())

        client(FakeSessionStore(StoredSession(tokens("a1"), 5))).get()

        assertEquals("Bearer a1", server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `signed out requests go without one`() {
        server.enqueue(MockResponse.Builder().code(201).build())

        client(FakeSessionStore()).get()

        assertNull(server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `a 401 refreshes the token and retries once`() {
        tokenService.refreshResult = TokenResult.Success(tokens("a2"))
        server.enqueue(MockResponse.Builder().code(401).build())
        server.enqueue(MockResponse.Builder().body("[]").build())

        val status = client(FakeSessionStore(StoredSession(tokens("a1"), 5))).get()

        assertEquals(200, status)
        assertEquals("Bearer a1", server.takeRequest().headers["Authorization"])
        assertEquals("Bearer a2", server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `a second 401 after refreshing is returned to the caller`() {
        tokenService.refreshResult = TokenResult.Success(tokens("a2"))
        server.enqueue(MockResponse.Builder().code(401).build())
        server.enqueue(MockResponse.Builder().code(401).build())

        val status = client(FakeSessionStore(StoredSession(tokens("a1"), 5))).get()

        assertEquals(401, status)
        assertEquals(1, tokenService.refreshedWith.size)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `a refused refresh returns the 401 without retrying`() {
        tokenService.refreshResult = TokenResult.Rejected
        server.enqueue(MockResponse.Builder().code(401).build())

        val status = client(FakeSessionStore(StoredSession(tokens("a1"), 5))).get()

        assertEquals(401, status)
        assertEquals(1, server.requestCount)
    }
}
