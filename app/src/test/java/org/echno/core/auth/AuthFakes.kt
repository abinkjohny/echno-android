package org.echno.core.auth

import java.util.concurrent.CopyOnWriteArrayList
import org.echno.core.network.AppError

class FakeSessionStore(initial: StoredSession = StoredSession(null, null)) : SessionStore {
    @Volatile var stored: StoredSession = initial

    override suspend fun read(): StoredSession = stored

    override suspend fun saveTokens(tokens: AuthTokens) {
        stored = stored.copy(tokens = tokens)
    }

    override suspend fun saveOrganization(organizationId: Long?) {
        stored = stored.copy(organizationId = organizationId)
    }

    override suspend fun clear() {
        stored = StoredSession(null, null)
    }
}

/** Answers refreshes with [refreshResult] and records every call. */
class FakeTokenService(var refreshResult: TokenResult = TokenResult.Rejected) : TokenService {
    val refreshedWith = CopyOnWriteArrayList<String>()
    val revoked = CopyOnWriteArrayList<String>()
    var exchangeResult: TokenResult = TokenResult.Failed(AppError.Network)

    override fun exchangeCode(code: String, codeVerifier: String, redirectUri: String): TokenResult = exchangeResult

    override fun refresh(refreshToken: String): TokenResult {
        refreshedWith += refreshToken
        return refreshResult
    }

    override fun revoke(refreshToken: String): Boolean {
        revoked += refreshToken
        return true
    }
}

/** Tokens that expire at [expiresAt] (epoch seconds). */
fun tokens(access: String, refresh: String? = "refresh-$access", expiresAt: Long = Long.MAX_VALUE / 2) =
    AuthTokens(access, refresh, idToken = null, accessTokenExpiresAt = expiresAt)
