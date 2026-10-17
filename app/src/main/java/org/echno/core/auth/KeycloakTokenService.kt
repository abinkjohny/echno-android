package org.echno.core.auth

import java.io.IOException
import java.io.InterruptedIOException
import java.time.Clock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.echno.core.auth.dto.TokenErrorDto
import org.echno.core.auth.dto.TokenResponseDto
import org.echno.core.network.AppError
import timber.log.Timber

/**
 * [TokenService] over plain OkHttp form posts.
 *
 * [client] must not be the API client: that one adds the access token and refreshes on 401,
 * which would loop back here.
 */
class KeycloakTokenService(
    private val client: OkHttpClient,
    private val endpoints: AuthEndpoints,
    private val clientId: String,
    private val json: Json,
    private val clock: Clock
) : TokenService {
    override fun exchangeCode(code: String, codeVerifier: String, redirectUri: String): TokenResult = requestTokens(
        FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("code", code)
            .add("code_verifier", codeVerifier)
            .add("redirect_uri", redirectUri)
            .build(),
        previousRefreshToken = null
    )

    override fun refresh(refreshToken: String): TokenResult = requestTokens(
        FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("client_id", clientId)
            .add("refresh_token", refreshToken)
            .build(),
        previousRefreshToken = refreshToken
    )

    override fun revoke(refreshToken: String): Boolean {
        // Keycloak's logout endpoint ends the session when a public client posts its refresh token,
        // with no browser involved. The browser's Keycloak cookie then points at a dead session, so
        // the next sign-in asks for the password again.
        val body = FormBody.Builder().add("client_id", clientId).add("refresh_token", refreshToken).build()
        return try {
            client.newCall(Request.Builder().url(endpoints.endSession).post(body).build()).execute().use { it.isSuccessful }
        } catch (e: IOException) {
            Timber.w(e, "Couldn't end the Keycloak session")
            false
        }
    }

    private fun requestTokens(form: FormBody, previousRefreshToken: String?): TokenResult = try {
        post(endpoints.token, form).let { (status, body) ->
            when {
                status in 200..299 -> TokenResult.Success(parseTokens(body, previousRefreshToken))
                // 400 invalid_grant: code or refresh token expired/revoked. 401: client rejected.
                status == 400 || status == 401 -> {
                    Timber.w("Keycloak refused the token request: %s", errorCode(body))
                    TokenResult.Rejected
                }
                status in 500..599 -> TokenResult.Failed(AppError.Server(status, null))
                else -> TokenResult.Failed(AppError.Unknown(IllegalStateException("Token endpoint answered $status")))
            }
        }
    } catch (e: InterruptedIOException) {
        TokenResult.Failed(AppError.Timeout)
    } catch (e: IOException) {
        TokenResult.Failed(AppError.Network)
    } catch (e: SerializationException) {
        TokenResult.Failed(AppError.Unknown(e))
    } catch (e: IllegalArgumentException) {
        // kotlinx.serialization reports a missing required field this way.
        TokenResult.Failed(AppError.Unknown(e))
    }

    private fun post(url: HttpUrl, form: FormBody): Pair<Int, String> = client
        .newCall(Request.Builder().url(url).post(form).build())
        .execute()
        .use { response -> response.code to response.body.string() }

    private fun parseTokens(body: String, previousRefreshToken: String?): AuthTokens {
        val dto = json.decodeFromString<TokenResponseDto>(body)
        return AuthTokens(
            accessToken = dto.accessToken,
            // A refresh may answer without a new refresh token; the old one then stays valid.
            refreshToken = dto.refreshToken ?: previousRefreshToken,
            idToken = dto.idToken,
            accessTokenExpiresAt = clock.instant().epochSecond + dto.expiresIn
        )
    }

    private fun errorCode(body: String): String? = runCatching { json.decodeFromString<TokenErrorDto>(body).error }.getOrNull()
}
