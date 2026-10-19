package org.echno.feature.organization

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.echno.core.network.ApiResult
import org.echno.core.network.NetworkJson
import org.echno.feature.organization.data.DefaultOrganizationRepository
import org.echno.feature.organization.data.OrganizationApi
import org.echno.feature.organization.domain.Organization
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class DefaultOrganizationRepositoryTest {
    private val server = MockWebServer()
    private lateinit var repository: DefaultOrganizationRepository

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(NetworkJson.asConverterFactory("application/json".toMediaType()))
                .build()
                .create<OrganizationApi>()
        repository = DefaultOrganizationRepository(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `asks for the user, then their organizations, scoped to a membership`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"id":42,"name":"Asha"}""").build())
        server.enqueue(
            MockResponse.Builder()
                .body(
                    """[{"id":12,"organizationName":"Periyar Infra","isActive":false},{"id":5,"organizationName":"Malabar Builders","organizationAddress":"Kozhikode"}]"""
                )
                .build()
        )

        val result = repository.getMyOrganizations(memberOf = listOf(5, 12))

        val user = server.takeRequest()
        assertEquals("/api/v1/user", user.target)
        assertEquals("5", user.headers["X-Organization-Id"])
        val organizations = server.takeRequest()
        assertEquals("/api/v1/user/42/organizations", organizations.target)
        assertEquals("5", organizations.headers["X-Organization-Id"])
        assertEquals(
            listOf(
                Organization(5, "Malabar Builders", "Kozhikode", isActive = true),
                Organization(12, "Periyar Infra", null, isActive = false)
            ),
            (result as ApiResult.Success).data
        )
    }

    @Test
    fun `a token without memberships still asks the server, without a scope`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"id":42}""").build())
        server.enqueue(MockResponse.Builder().body("""[{"id":5,"organizationName":"Malabar Builders"}]""").build())

        val result = repository.getMyOrganizations(emptyList())

        assertNull(server.takeRequest().headers["X-Organization-Id"])
        assertNull(server.takeRequest().headers["X-Organization-Id"])
        assertEquals(listOf(5L), (result as ApiResult.Success).data.map { it.id })
    }
}
