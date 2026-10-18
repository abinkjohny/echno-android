package org.echno.feature.auth

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.echno.core.network.ApiResult
import org.echno.core.network.AppError
import org.echno.core.network.NetworkJson
import org.echno.feature.auth.data.AuthApi
import org.echno.feature.auth.data.DefaultRegistrationRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class DefaultRegistrationRepositoryTest {
    private val server = MockWebServer()
    private lateinit var repository: DefaultRegistrationRepository

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(NetworkJson.asConverterFactory("application/json".toMediaType()))
                .build()
                .create<AuthApi>()
        repository = DefaultRegistrationRepository(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `register posts the backend's field names and formats`() = runTest {
        server.enqueue(MockResponse.Builder().code(201).body("""{"id":42,"name":"Asha Menon","role":"EMPLOYEE"}""").build())

        val result = repository.register(validForm)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/auth/register", request.target)
        val body = Json.parseToJsonElement(request.body!!.utf8()).jsonObject
        assertEquals(
            JsonObject(
                mapOf(
                    "userName" to JsonPrimitive("asha.menon"),
                    "name" to JsonPrimitive("Asha Menon"),
                    "email" to JsonPrimitive("asha@example.com"),
                    "password" to JsonPrimitive("Site#2026a"),
                    "phone" to JsonPrimitive("+919876543210"),
                    "gender" to JsonPrimitive("Female"),
                    "dateOfBirth" to JsonPrimitive("1995-03-14T00:00:00"),
                    "role" to JsonPrimitive("EMPLOYEE"),
                    "acceptTerms" to JsonPrimitive(true)
                )
            ),
            body
        )
        assertTrue(result is ApiResult.Success)
    }

    @Test
    fun `a duplicate email is a conflict`() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .code(409)
                .body("""{"title":"Duplicate Resource","status":409,"detail":"User with email 'asha@example.com' already exists"}""")
                .build()
        )

        val result = repository.register(validForm)

        assertTrue((result as ApiResult.Failure).error is AppError.Conflict)
    }
}
