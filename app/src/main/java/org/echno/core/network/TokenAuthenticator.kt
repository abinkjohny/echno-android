package org.echno.core.network

import javax.inject.Inject
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.echno.core.auth.SessionManager

/**
 * OkHttp calls this when a response is 401. It refreshes the access token and returns the request
 * to retry with the new one, or null to give up (the caller then sees [AppError.Unauthorized]).
 *
 * If the refresh token itself is refused, [SessionManager] signs the user out and the app returns
 * to the sign-in screen.
 */
class TokenAuthenticator @Inject constructor(private val sessionManager: SessionManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val sentToken = response.request.header(AuthInterceptor.HEADER)?.removePrefix("Bearer ") ?: return null
        // Already retried once with a fresh token and still refused: refreshing again won't help.
        if (response.priorResponse != null) return null
        val newToken = sessionManager.refreshAccessToken(sentToken) ?: return null
        return response.request.newBuilder().header(AuthInterceptor.HEADER, "Bearer $newToken").build()
    }
}
