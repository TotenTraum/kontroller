package ru.ttraum.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenApiTest {
    @Test
    fun openApiDocumentTest() = testApplication {
        application {
            module()
        }

        val response = client.get("openapi.yaml")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("operationId: helloWorld"), body)
        assertTrue(body.contains("summary: Say hello"), body)
        assertTrue(body.contains("description: Returns a static greeting"), body)

        // deep annotation chain: @Operation + @ApiResponses + @Content/@ArraySchema/@Schema(implementation) + @Tag
        assertTrue(body.contains("operationId: locks"), body)
        assertTrue(body.contains("summary: Блокировка сущностей"), body)
        assertTrue(body.contains("Блокировки сущностей"), body)
        assertTrue(body.contains("Внутренняя ошибка"), body)
        assertTrue(body.contains("LockAnswer"), body)
        assertTrue(body.contains("type: array"), body)
    }
}
