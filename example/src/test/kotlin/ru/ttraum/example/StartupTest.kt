package ru.ttraum.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class StartupTest {
    @Test
    fun startupTest() = testApplication {
        application {
            module()
        }
        val response = client.get("")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("hello world", response.bodyAsText())
    }
}