package org.echno.core.network

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

// Real Retrofit + OkHttp against a local fake server, so the whole path is exercised:
// HTTP status and body → Retrofit exception → safeApiCall → AppError.
class SafeApiCallTest {
    @Serializable
    private data class ItemDto(val id: Long, val name: String)

    private interface TestApi {
        @GET("items")
        suspend fun items(): List<ItemDto>
    }

    private val server = MockWebServer()
    private lateinit var api: TestApi

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().readTimeout(200, TimeUnit.MILLISECONDS).build()
        api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .client(client)
                .addConverterFactory(NetworkJson.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(TestApi::class.java)
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun respond(code: Int, body: String = "", vararg headers: Pair<String, String>) {
        val response = MockResponse.Builder().code(code).body(body)
        headers.forEach { (name, value) -> response.addHeader(name, value) }
        server.enqueue(response.build())
    }

    private fun problem(status: Int, detail: String, extra: String = "") =
        """{"title":"t","status":$status,"detail":"$detail","message":"$detail"$extra}"""

    @Test
    fun `2xx returns the decoded body`() = runTest {
        respond(200, """[{"id":1,"name":"Tower A"}]""")

        assertEquals(ApiResult.Success(listOf(ItemDto(1, "Tower A"))), safeApiCall { api.items() })
    }

    @Test
    fun `401 without a body is Unauthorized`() = runTest {
        respond(401, "", "WWW-Authenticate" to "Bearer")

        assertEquals(ApiResult.Failure(AppError.Unauthorized), safeApiCall { api.items() })
    }

    @Test
    fun `403 is Forbidden with the problem detail`() = runTest {
        respond(403, problem(403, "Not a member of this organization"))

        assertEquals(
            ApiResult.Failure(AppError.Forbidden("Not a member of this organization")),
            safeApiCall { api.items() }
        )
    }

    @Test
    fun `404 is NotFound`() = runTest {
        respond(404, problem(404, "Project 9 not found"))

        assertEquals(ApiResult.Failure(AppError.NotFound("Project 9 not found")), safeApiCall { api.items() })
    }

    @Test
    fun `400 is Validation with field errors`() = runTest {
        respond(400, problem(400, "Validation Failed", ""","errors":{"name":"must not be blank"}"""))

        assertEquals(
            ApiResult.Failure(AppError.Validation("Validation Failed", mapOf("name" to "must not be blank"))),
            safeApiCall { api.items() }
        )
    }

    @Test
    fun `422 is Validation`() = runTest {
        respond(422, problem(422, "Outside the site geofence"))

        assertEquals(ApiResult.Failure(AppError.Validation("Outside the site geofence")), safeApiCall { api.items() })
    }

    @Test
    fun `409 is Conflict and keeps the retryable flag`() = runTest {
        respond(409, problem(409, "Concurrent update", ""","retryable":true"""))

        assertEquals(ApiResult.Failure(AppError.Conflict("Concurrent update", retryable = true)), safeApiCall { api.items() })
    }

    @Test
    fun `429 is RateLimited with Retry-After`() = runTest {
        respond(429, problem(429, "Slow down"), "Retry-After" to "30")

        assertEquals(ApiResult.Failure(AppError.RateLimited(retryAfterSeconds = 30)), safeApiCall { api.items() })
    }

    @Test
    fun `5xx is Server with the status`() = runTest {
        respond(503, problem(503, "Maintenance"))

        assertEquals(ApiResult.Failure(AppError.Server(503, "Maintenance")), safeApiCall { api.items() })
    }

    @Test
    fun `5xx with a non-JSON body still maps to Server`() = runTest {
        respond(502, "<html>Bad Gateway</html>")

        assertEquals(ApiResult.Failure(AppError.Server(502, null)), safeApiCall { api.items() })
    }

    @Test
    fun `slow server is Timeout`() = runTest {
        server.enqueue(MockResponse.Builder().body("[]").headersDelay(2, TimeUnit.SECONDS).build())

        assertEquals(ApiResult.Failure(AppError.Timeout), safeApiCall { api.items() })
    }

    @Test
    fun `dropped connection is Network`() = runTest {
        server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.ShutdownConnection).build())

        assertEquals(ApiResult.Failure(AppError.Network), safeApiCall { api.items() })
    }

    @Test
    fun `204 for a method that expects a body is Unknown, not a crash`() = runTest {
        // Retrofit skips the converter on 204 and throws KotlinNullPointerException for a non-null return type.
        respond(204)

        val result = safeApiCall { api.items() }

        assertTrue(result is ApiResult.Failure && result.error is AppError.Unknown)
    }

    @Test
    fun `unparseable success body is Unknown`() = runTest {
        respond(200, """{"unexpected":"shape"}""")

        val result = safeApiCall { api.items() }

        assertTrue(result is ApiResult.Failure && result.error is AppError.Unknown)
    }
}
