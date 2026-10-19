package org.echno.feature.profile

import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.network.NetworkJson
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.Gender
import org.echno.feature.organization.domain.Organization
import org.echno.feature.profile.data.DefaultProfileRepository
import org.echno.feature.profile.data.ProfileApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class DefaultProfileRepositoryTest {
    private val server = MockWebServer()
    private lateinit var repository: DefaultProfileRepository

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(NetworkJson.asConverterFactory("application/json".toMediaType()))
                .build()
                .create<ProfileApi>()
        repository = DefaultProfileRepository(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `reads the user and names the organization they work in`() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .body(
                    """{"id":42,"name":"Asha Menon","email":"asha@example.com","phone":"+919876543210","gender":"Female",
                    |"dateOfBirth":"1994-05-01T00:00:00","role":"HR_MANAGER","experience":6,"skills":["Payroll"," "],
                    |"createdAt":"2026-01-15T09:30:12.345","unknownField":true}
                    """.trimMargin()
                )
                .build()
        )
        server.enqueue(
            MockResponse.Builder()
                .body("""[{"id":5,"organizationName":"Malabar Builders"},{"id":12,"organizationName":"Periyar Infra"}]""")
                .build()
        )

        val profile = (repository.getMyProfile(organizationId = 12) as ApiResult.Success).data

        assertEquals("/api/v1/user", server.takeRequest().target)
        assertEquals("/api/v1/user/42/organizations", server.takeRequest().target)
        assertEquals("Asha Menon", profile.name)
        assertEquals(AccountRole.HrManager, profile.role)
        assertEquals(Gender.Female, profile.gender)
        assertEquals(LocalDate.of(1994, 5, 1), profile.dateOfBirth)
        assertEquals(LocalDate.of(2026, 1, 15), profile.memberSince)
        assertEquals(listOf("Payroll"), profile.skills)
        assertEquals(Organization(12, "Periyar Infra", null, isActive = true), profile.organization)
    }

    @Test
    fun `missing details stay empty, and the name falls back to the email`() = runTest {
        server.enqueue(
            MockResponse.Builder().body("""{"id":42,"name":" ","email":"asha@example.com","role":"PILOT","dateOfBirth":"soon"}""").build()
        )
        server.enqueue(MockResponse.Builder().body("[]").build())

        val profile = (repository.getMyProfile(organizationId = 5) as ApiResult.Success).data

        assertEquals("asha@example.com", profile.name)
        assertNull(profile.role)
        assertNull(profile.dateOfBirth)
        assertNull(profile.organization)
        assertTrue(profile.skills.isEmpty())
    }

    @Test
    fun `a failed request is reported, not thrown`() = runTest {
        server.enqueue(MockResponse.Builder().code(403).build())

        val result = repository.getMyProfile(organizationId = 5)

        assertTrue((result as ApiResult.Failure).error is AppError.Forbidden)
    }
}
