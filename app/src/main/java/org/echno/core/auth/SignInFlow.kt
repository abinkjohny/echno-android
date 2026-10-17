package org.echno.core.auth

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.echno.BuildConfig
import org.echno.core.network.AppError

/**
 * Signing in through Keycloak's own login page, in two halves around the browser:
 * [createIntent] opens the page; [complete] handles what the browser sends back.
 */
interface SignInFlow {
    /** [loginHint] pre-fills the username field, e.g. right after registering. */
    fun createIntent(loginHint: String? = null): Intent

    /** [result] is the Intent the login activity returned (null if it was closed). */
    suspend fun complete(result: Intent?): SignInResult
}

sealed interface SignInResult {
    data object Success : SignInResult

    /** The user closed the browser tab: not an error. */
    data object Cancelled : SignInResult

    data class Failed(val error: AppError) : SignInResult
}

/**
 * [SignInFlow] with AppAuth, the OpenID Foundation's library for native apps (RFC 8252).
 *
 * AppAuth opens the login page in a Custom Tab (the user's browser, shown inside the app), so the
 * app never sees the password. It adds PKCE: a random secret whose hash goes with the login request
 * and whose original goes with the code exchange, so a stolen code is useless. It checks `state` so
 * a redirect can't be forged. The code exchange itself goes through [TokenService].
 */
@Singleton
class AppAuthSignInFlow @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val endpoints: AuthEndpoints,
    private val tokenService: TokenService,
    private val sessionManager: SessionManager
) : SignInFlow {
    // Created on first use: it connects to the browser's Custom Tabs service to open the tab faster.
    private val authorizationService by lazy { AuthorizationService(context) }

    override fun createIntent(loginHint: String?): Intent {
        val configuration =
            AuthorizationServiceConfiguration(
                endpoints.authorization.toString().toUri(),
                endpoints.token.toString().toUri(),
                null,
                endpoints.endSession.toString().toUri()
            )
        // build() generates the state, the nonce and the PKCE verifier with its S256 challenge.
        val request =
            AuthorizationRequest.Builder(
                configuration,
                BuildConfig.AUTH_CLIENT_ID,
                ResponseTypeValues.CODE,
                BuildConfig.AUTH_REDIRECT_URI.toUri()
            )
                .setScope(SCOPES)
                .setLoginHint(loginHint)
                .build()
        // Throws ActivityNotFoundException when the device has no browser.
        return authorizationService.getAuthorizationRequestIntent(request)
    }

    override suspend fun complete(result: Intent?): SignInResult {
        val response = result?.let(AuthorizationResponse::fromIntent)
        val code = response?.authorizationCode
        val codeVerifier = response?.request?.codeVerifier
        if (code == null || codeVerifier == null) return failureFrom(result?.let(AuthorizationException::fromIntent))

        return when (
            val tokens = withContext(Dispatchers.IO) {
                tokenService.exchangeCode(code, codeVerifier, BuildConfig.AUTH_REDIRECT_URI)
            }
        ) {
            is TokenResult.Success -> {
                sessionManager.signIn(tokens.tokens)
                SignInResult.Success
            }
            // The code was refused, e.g. it expired because the login page was left open too long.
            TokenResult.Rejected -> SignInResult.Failed(AppError.Unauthorized)
            is TokenResult.Failed -> SignInResult.Failed(tokens.error)
        }
    }

    private fun failureFrom(exception: AuthorizationException?): SignInResult = when {
        exception == null || exception.isGeneral(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW) -> SignInResult.Cancelled
        exception.isGeneral(AuthorizationException.GeneralErrors.NETWORK_ERROR) -> SignInResult.Failed(AppError.Network)
        else -> SignInResult.Failed(AppError.Unknown(exception))
    }

    private fun AuthorizationException.isGeneral(expected: AuthorizationException) = type == expected.type && code == expected.code

    private companion object {
        // offline_access: a refresh token that outlives the browser session, so field staff stay
        // signed in for weeks (local-docs/keycloak-setup.md, decision 1).
        const val SCOPES = "openid profile email offline_access"
    }
}
