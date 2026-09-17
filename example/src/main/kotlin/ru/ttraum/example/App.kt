package ru.ttraum.example

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import ru.ttraum.example.api.*
import ru.ttraum.example.api.dto.ErrorResponse
import ru.ttraum.example.controller.FileController
import ru.ttraum.example.controller.HomePageController
import ru.ttraum.example.controller.SecurityController
import ru.ttraum.example.controller.StoreController

fun main() {
    embeddedServer(CIO, port = 8067) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    serialization()
    security()
    di()
    routing()
    errorPages()
}

fun Application.security() {
    install(Authentication) {
        basic("auth-a") {
            validate { credentials ->
                if (credentials.name == "user-a" && credentials.password == "pass-a") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        basic("auth-b") {
            validate { credentials ->
                if (credentials.name == "user-b" && credentials.password == "pass-b") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        basic("auth-с") {
            validate { credentials ->
                val p = this.request.call.principal<UserIdPrincipal>()
                if (p?.name == "user-a") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}

fun Application.routing() {
    routing {
        get("openapi.yaml") {
            val source = OpenApiDocSource.Routing(contentType = ContentType.Application.Yaml)
            val info = OpenApiInfo(title = "kontroller example", version = "0.1.0")
            val doc = source.read(
                application, OpenApiDoc(
                    info = info,
                    servers = listOf(Server("http://localhost:8080"))
                )
            )
            call.respondText(doc.content, doc.contentType)
        }


        route("api/v1") {
            val router: StoreApiRouter by dependencies
            this.routes(router)
        }

        val homeRouter: HomePageApiRouter by dependencies
        this.routes(homeRouter)

        val fileRouter: FileApiRouter by dependencies
        this.routes(fileRouter)

        val securityRouter: SecurityApiRouter by dependencies
        this.routes(securityRouter)
    }
}

fun Application.errorPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val err = ErrorResponse(500, cause.stackTraceToString())
            call.respond(status = HttpStatusCode.InternalServerError, message = err)
        }
    }
}

fun Application.serialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.di() {
    dependencies.provide<StoreApi> { StoreController() }
    dependencies.provide<HomePageApi> { HomePageController() }
    dependencies.provide<FileApi> { FileController() }
    dependencies.provide<SecurityApi> { SecurityController() }

    dependencies.provide<StoreApiRouter> { StoreApiRouter(this.resolve()) }
    dependencies.provide<HomePageApiRouter> { HomePageApiRouter(this.resolve()) }
    dependencies.provide<FileApiRouter> { FileApiRouter(this.resolve()) }
    dependencies.provide<SecurityApiRouter> { SecurityApiRouter(this.resolve()) }
}