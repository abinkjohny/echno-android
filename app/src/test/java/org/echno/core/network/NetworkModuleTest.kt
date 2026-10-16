package org.echno.core.network

import okhttp3.logging.HttpLoggingInterceptor
import org.echno.BuildConfig
import org.echno.core.session.SelectedOrganization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkModuleTest {
    private val client = NetworkModule.provideOkHttpClient(OrganizationInterceptor(SelectedOrganization()))

    @Test
    fun `client sets timeouts`() {
        assertEquals(15_000, client.connectTimeoutMillis)
        assertEquals(30_000, client.readTimeoutMillis)
        assertEquals(30_000, client.writeTimeoutMillis)
    }

    @Test
    fun `client sends the organization header`() {
        assertTrue(client.interceptors.any { it is OrganizationInterceptor })
    }

    @Test
    fun `http logging exists only in debug builds`() {
        assertEquals(BuildConfig.DEBUG, client.interceptors.any { it is HttpLoggingInterceptor })
    }

    @Test
    fun `retrofit points at the flavor's API base URL`() {
        val retrofit = NetworkModule.provideRetrofit(client, NetworkModule.provideJson())

        assertEquals(BuildConfig.API_BASE_URL, retrofit.baseUrl().toString())
    }
}
