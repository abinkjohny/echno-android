package org.echno.core.network

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response
import org.echno.core.auth.SessionManager

/**
 * Adds `Authorization: Bearer <access token>` to every API request while signed in.
 * Requests made while signed out (e.g. registration) go without it.
 */
class AuthInterceptor @Inject constructor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(HEADER) != null) return chain.proceed(request)
        val token = sessionManager.accessToken() ?: return chain.proceed(request)
        return chain.proceed(request.newBuilder().header(HEADER, "Bearer $token").build())
    }

    companion object {
        const val HEADER = "Authorization"
    }
}
