package org.echno.core.auth

import org.echno.core.network.AppError

/**
 * Talks to Keycloak's token endpoint. Calls block the calling thread: OkHttp's [okhttp3.Authenticator]
 * runs on a background thread and needs the answer before it can retry a request. From a coroutine,
 * call them inside `withContext(Dispatchers.IO)`.
 */
interface TokenService {
    /** Trades the one-time authorization code from the browser for tokens (PKCE: proves we started the login). */
    fun exchangeCode(code: String, codeVerifier: String, redirectUri: String): TokenResult

    /** Gets a new access token. Keycloak rotates refresh tokens, so the result carries a new one too. */
    fun refresh(refreshToken: String): TokenResult

    /** Ends the Keycloak session behind [refreshToken]. Best effort: false if Keycloak couldn't be told. */
    fun revoke(refreshToken: String): Boolean
}

sealed interface TokenResult {
    data class Success(val tokens: AuthTokens) : TokenResult

    /** Keycloak refused the code or refresh token (expired, revoked, user disabled): sign in again. */
    data object Rejected : TokenResult

    /** Keycloak couldn't be reached or failed. The session may still be valid, so try again later. */
    data class Failed(val error: AppError) : TokenResult
}
