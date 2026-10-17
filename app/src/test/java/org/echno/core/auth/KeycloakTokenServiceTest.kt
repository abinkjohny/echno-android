package org.echno.core.auth

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import okhttp3.OkHttpClient
import org.echno.core.network.AppError
import org.echno.core.network.NetworkJson
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeycloakTokenServiceTest {
    private val server = MockWebServer()
    private val now = Instant.parse("2026-10-17T10:00:00Z")
    private lateinit var service: KeycloakTokenService

    @Before
    fun setUp() {
        server.start()
        service =
            KeycloakTokenService(
                client = OkHttpClient(),
                endpoints = AuthEndpoints.forIssuer(server.url("/realms/echno-realm").toString()),
                clientId = "echno-android-client",
                json = NetworkJson,
                clock = Clock.fixed(now, ZoneOffset.UTC)
            )
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `exchangeCode posts the code and PKCE verifier and returns the tokens`() {
        server.enqueue(tokenResponse(access = "a1", refresh = "r1", expiresIn = 300))

        val result = service.exchangeCode(code = "c0de", codeVerifier = "v3rifier", redirectUri = "org.echno.dev:/oauth2redirect")

        val request = server.takeRequest()
        assertEquals("/realms/echno-realm/protocol/openid-connect/token", request.target)
        val form = request.body!!.utf8()
        assertTrue(form.contains("grant_type=authorization_code"))
        assertTrue(form.contains("client_id=echno-android-client"))
        assertTrue(form.contains("code=c0de"))
        assertTrue(form.contains("code_verifier=v3rifier"))
        assertTrue(form.contains("redirect_uri=org.echno.dev%3A%2Foauth2redirect"))
        val tokens = (result as TokenResult.Success).tokens
        assertEquals(AuthTokens("a1", "r1", "id1", now.epochSecond + 300), tokens)
    }

    @Test
    fun `refresh keeps the old refresh token when Keycloak sends none`() {
        server.enqueue(MockResponse.Builder().body("""{"access_token":"a2","expires_in":300}""").build())

        val result = service.refresh("r1")

        assertTrue(server.takeRequest().body!!.utf8().contains("grant_type=refresh_token"))
        assertEquals("r1", (result as TokenResult.Success).tokens.refreshToken)
    }

    @Test
    fun `invalid_grant means the session is over`() {
        server.enqueue(MockResponse.Builder().code(400).body("""{"error":"invalid_grant"}""").build())

        assertEquals(TokenResult.Rejected, service.refresh("expired"))
    }

    @Test
    fun `a Keycloak outage is a failure, not a rejection`() {
        server.enqueue(MockResponse.Builder().code(503).build())

        assertEquals(TokenResult.Failed(AppError.Server(503, null)), service.refresh("r1"))
    }

    @Test
    fun `no connection is a network failure`() {
        server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.ShutdownConnection).build())

        assertEquals(TokenResult.Failed(AppError.Network), service.refresh("r1"))
    }

    @Test
    fun `revoke posts the refresh token to the logout endpoint`() {
        server.enqueue(MockResponse.Builder().code(204).build())

        assertTrue(service.revoke("r1"))

        val request = server.takeRequest()
        assertEquals("/realms/echno-realm/protocol/openid-connect/logout", request.target)
        assertTrue(request.body!!.utf8().contains("refresh_token=r1"))
    }

    @Test
    fun `revoke reports false when Keycloak can't be reached`() {
        server.close()

        assertFalse(service.revoke("r1"))
    }

    private fun tokenResponse(access: String, refresh: String, expiresIn: Long) = MockResponse.Builder()
        .body("""{"access_token":"$access","expires_in":$expiresIn,"refresh_token":"$refresh","id_token":"id1","token_type":"Bearer"}""")
        .build()
}
