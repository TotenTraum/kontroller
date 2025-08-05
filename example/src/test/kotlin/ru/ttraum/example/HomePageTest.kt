package ru.ttraum.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class HomePageTest {

    @Test
    fun tailCardTest() = testApplication {
        application {
            module()
        }
        val response = client.get("long/path/tailcard/with/many/words")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("tailcard/with/many/words", response.bodyAsText())
    }

    @Test
    fun queryParamTest() = testApplication {
        application {
            module()
        }
        val response = client.get("query?names=john,ron,tom,sam")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("john/ron/tom/sam", response.bodyAsText())
    }

    @Test
    fun queryModelTest() = testApplication {
        application {
            module()
        }
        var response = client.get("query/model?message=text")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text and 0", response.bodyAsText())

        response = client.get("query/model?message=text&number=1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text and 1", response.bodyAsText())

        response = client.get("query/model?number=1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("empty and 1", response.bodyAsText())
    }
}