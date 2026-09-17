package ru.ttraum.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityTest {

    @Test
    fun classSecurityTest() = testApplication {
        application {
            module()
        }

        val unauthorized = client.get("security/class")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val wrongProvider = client.get("security/class") { basicAuth("user-b", "pass-b") }
        assertEquals(HttpStatusCode.Unauthorized, wrongProvider.status)

        val authorized = client.get("security/class") { basicAuth("user-a", "pass-a") }
        assertEquals(HttpStatusCode.OK, authorized.status)
        assertEquals("class security", authorized.bodyAsText())
    }

    @Test
    fun funSecurityTest() = testApplication {
        application {
            module()
        }

        val unauthorized = client.get("security/fun")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val wrongProvider = client.get("security/fun") { basicAuth("user-a", "pass-a") }
        assertEquals(HttpStatusCode.Unauthorized, wrongProvider.status)

        val authorized = client.get("security/fun") { basicAuth("user-b", "pass-b") }
        assertEquals(HttpStatusCode.OK, authorized.status)
        assertEquals("fun security", authorized.bodyAsText())
    }

    @Test
    fun classAndFunSecurityTest() = testApplication {
        application {
            module()
        }

        val unauthorized = client.get("security/class-and-fun")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val authorizedWithClassProvider = client.get("security/class-and-fun") { basicAuth("user-a", "pass-a") }
        assertEquals(HttpStatusCode.OK, authorizedWithClassProvider.status)
        assertEquals("class and fun security", authorizedWithClassProvider.bodyAsText())

        val authorizedWithFunProvider = client.get("security/class-and-fun") { basicAuth("user-b", "pass-b") }
        assertEquals(HttpStatusCode.OK, authorizedWithFunProvider.status)
        assertEquals("class and fun security", authorizedWithFunProvider.bodyAsText())
    }

    @Test
    fun nestedFunSecurityTest() = testApplication {
        application {
            module()
        }

        val unauthorized = client.get("security/nested-fun")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val onlyClassProvider = client.get("security/nested-fun") { basicAuth("user-a", "pass-a") }
        assertEquals(HttpStatusCode.OK, onlyClassProvider.status)

        val onlyFunProvider = client.get("security/nested-fun") { basicAuth("user-b", "pass-b") }
        assertEquals(HttpStatusCode.Unauthorized, onlyFunProvider.status)
    }
}
