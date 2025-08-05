package ru.ttraum.example

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class FileTest {
    @Test
    fun fileTest() = testApplication {
        application {
            module()
        }
        val response = client.post("/file") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("text", "custom text", Headers.build {
                            append(HttpHeaders.ContentType, "text/plain")
                            append(HttpHeaders.ContentDisposition, "text")
                        })
                        append("text2", "custom text2", Headers.build {
                            append(HttpHeaders.ContentType, "text/plain")
                            append(HttpHeaders.ContentDisposition, "text2")
                        })
                    }
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/text2", response.bodyAsText())
    }
}