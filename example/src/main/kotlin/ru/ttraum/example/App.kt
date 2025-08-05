package ru.ttraum.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.ttraum.example.api.*
import ru.ttraum.example.api.dto.ErrorResponse
import ru.ttraum.example.controller.FileController
import ru.ttraum.example.controller.HomePageController
import ru.ttraum.example.controller.StoreController

fun main() {
    embeddedServer(Netty, port = 8089) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    serialization()
    di()
    routing()
    errorPages()
}

fun Application.routing() {
    routing {
        route("api/v1") {
            val router: StoreApiRouter by dependencies
            this.routes(router)
        }

        val homeRouter: HomePageApiRouter by dependencies
        this.routes(homeRouter)

        val fileRouter: FileApiRouter by dependencies
        this.routes(fileRouter)

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

    dependencies.provide<StoreApiRouter> { StoreApiRouter(this.resolve()) }
    dependencies.provide<HomePageApiRouter> { HomePageApiRouter(this.resolve()) }
    dependencies.provide<FileApiRouter> { FileApiRouter(this.resolve()) }
}