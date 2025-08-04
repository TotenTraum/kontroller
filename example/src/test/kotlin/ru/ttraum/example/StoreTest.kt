package ru.ttraum.example

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import ru.ttraum.example.api.dto.StoreData
import kotlin.test.Test
import kotlin.test.assertEquals

class StoreTest {
    @Test
    fun storeSetAndGetTest() = testApplication {
        application {
            module()
        }
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val data = StoreData("msg")

        var response = client.post("api/v1/router/b0773186-ac22-42df-9452-dca131a44871") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        response = client.get("api/v1/router/b0773186-ac22-42df-9452-dca131a44871")
        assertEquals(HttpStatusCode.OK, response.status)
        val actual: StoreData = response.body()
        assertEquals(data, actual)
    }
}