package org.echno.core.network

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response
import org.echno.core.session.SelectedOrganization

/**
 * Adds `X-Organization-Id` to every request, read by the backend's `TenantFilter`.
 *
 * Skipped when no organization is selected, and when a call already sets the header itself
 * (e.g. acting on a specific organization from the org picker).
 */
class OrganizationInterceptor @Inject constructor(private val selectedOrganization: SelectedOrganization) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val organizationId = selectedOrganization.id.value
        if (organizationId == null || request.header(HEADER) != null) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder().header(HEADER, organizationId.toString()).build())
    }

    companion object {
        const val HEADER = "X-Organization-Id"
    }
}
