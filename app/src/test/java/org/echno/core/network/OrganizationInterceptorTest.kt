package org.echno.core.network

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.echno.core.session.SelectedOrganization
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class OrganizationInterceptorTest {
    private val server = MockWebServer()
    private val selectedOrganization = SelectedOrganization()
    private val client = OkHttpClient.Builder().addInterceptor(OrganizationInterceptor(selectedOrganization)).build()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    /** Sends one request and returns the X-Organization-Id header the server received. */
    private fun sentHeader(request: Request = Request.Builder().url(server.url("/api/v1/project")).build()): String? {
        server.enqueue(MockResponse.Builder().code(200).build())
        client.newCall(request).execute().close()
        return server.takeRequest().headers[OrganizationInterceptor.HEADER]
    }

    @Test
    fun `adds the selected organization`() {
        selectedOrganization.select(42)

        assertEquals("42", sentHeader())
    }

    @Test
    fun `sends no header when no organization is selected`() {
        assertNull(sentHeader())
    }

    @Test
    fun `follows organization changes`() {
        selectedOrganization.select(1)
        assertEquals("1", sentHeader())

        selectedOrganization.select(2)
        assertEquals("2", sentHeader())
    }

    @Test
    fun `keeps a header the call set explicitly`() {
        selectedOrganization.select(42)
        val request = Request.Builder().url(server.url("/")).header(OrganizationInterceptor.HEADER, "7").build()

        assertEquals("7", sentHeader(request))
    }
}
