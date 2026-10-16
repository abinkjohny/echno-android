package org.echno.core.network.dto

import kotlinx.serialization.Serializable
import org.echno.core.network.NetworkJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Samples copy the backend's real response shapes, including fields the app ignores.
class ResponseDtoDecodingTest {
    @Serializable
    private data class ItemDto(val id: Long, val name: String)

    @Test
    fun `validation problem decodes title, detail and field errors`() {
        val json =
            """
            {
              "type": "about:blank",
              "title": "Validation Failed",
              "status": 400,
              "detail": "Validation Failed",
              "instance": "/api/v1/project",
              "message": "Validation Failed",
              "details": "uri=/api/v1/project",
              "timestamp": "2026-10-16T10:15:30",
              "errors": { "name": "must not be blank", "startDate": "must be a date in the future" }
            }
            """.trimIndent()

        val problem = NetworkJson.decodeFromString<ProblemDetailDto>(json)

        assertEquals("Validation Failed", problem.title)
        assertEquals(400, problem.status)
        assertEquals("must not be blank", problem.errors["name"])
        assertEquals(2, problem.errors.size)
    }

    @Test
    fun `conflict problem decodes retryable flag`() {
        val json = """{"title":"Concurrent Update Conflict","status":409,"detail":"Try again","retryable":true,"retryAfterSeconds":1}"""

        val problem = NetworkJson.decodeFromString<ProblemDetailDto>(json)

        assertTrue(problem.retryable)
        assertEquals(emptyMap<String, String>(), problem.errors)
    }

    @Test
    fun `spring page decodes content and paging fields`() {
        val json =
            """
            {
              "content": [ { "id": 1, "name": "Tower A" }, { "id": 2, "name": "Tower B" } ],
              "pageable": { "pageNumber": 0, "pageSize": 2, "sort": { "sorted": false } },
              "totalElements": 5, "totalPages": 3, "last": false, "size": 2, "number": 0,
              "sort": { "sorted": false, "empty": true }, "numberOfElements": 2, "first": true, "empty": false
            }
            """.trimIndent()

        val page = NetworkJson.decodeFromString<PageDto<ItemDto>>(json)

        assertEquals(listOf(ItemDto(1, "Tower A"), ItemDto(2, "Tower B")), page.content)
        assertEquals(5, page.totalElements)
        assertEquals(3, page.totalPages)
        assertTrue(page.first)
        assertEquals(false, page.last)
    }

    @Test
    fun `message response decodes`() {
        assertEquals("Deleted", NetworkJson.decodeFromString<MessageResponseDto>("""{"message":"Deleted"}""").message)
    }
}
