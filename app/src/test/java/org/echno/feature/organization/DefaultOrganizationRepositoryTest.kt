package org.echno.feature.organization

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.network.NetworkJson
import org.echno.feature.organization.data.DefaultOrganizationRepository
import org.echno.feature.organization.data.OrganizationApi
import org.echno.feature.organization.domain.Organization
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `joining sends the code for the signed-in user and returns the organization`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"id":42}""").build())
        server.enqueue(MockResponse.Builder().body("""{"id":7,"organizationName":"Tornotron Builders"}""").build())

        val result = repository.joinWithInviteCode("04821", memberOf = emptyList())

        assertEquals("/api/v1/user", server.takeRequest().target)
        val join = server.takeRequest()
        assertEquals("POST", join.method)
        assertEquals("/api/v1/invitation/web/validate/userId/42", join.target)
        assertEquals("""{"code":"04821"}""", join.body?.utf8())
        assertNull(join.headers["X-Organization-Id"])
        assertEquals(Organization(7, "Tornotron Builders", null, isActive = true), (result as ApiResult.Success).data)
    }

    @Test
    fun `an unknown code, a used-up code and too many tries come back as their own errors`() = runTest {
        val problem = """{"title":"x","status":%d,"detail":"x"}"""
        server.enqueue(MockResponse.Builder().body("""{"id":42}""").build())
        server.enqueue(MockResponse.Builder().code(404).body(problem.format(404)).build())
        server.enqueue(MockResponse.Builder().body("""{"id":42}""").build())
        server.enqueue(MockResponse.Builder().code(400).body(problem.format(400)).build())
        server.enqueue(MockResponse.Builder().body("""{"id":42}""").build())
        server.enqueue(MockResponse.Builder().code(429).addHeader("Retry-After", "120").body(problem.format(429)).build())

        assertTrue((repository.joinWithInviteCode("11111", emptyList()) as ApiResult.Failure).error is AppError.NotFound)
        assertTrue((repository.joinWithInviteCode("22222", emptyList()) as ApiResult.Failure).error is AppError.Validation)
        assertEquals(AppError.RateLimited(120), (repository.joinWithInviteCode("33333", emptyList()) as ApiResult.Failure).error)
    }
}
