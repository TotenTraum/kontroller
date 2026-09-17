package ru.ttraum.example.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import ru.ttraum.example.api.dto.LockAnswer
import ru.ttraum.example.api.dto.SelectQuery
import ru.ttraum.kontroller.core.http.*

@Controller
interface HomePageApi {

    @Operation(summary = "Say hello", description = "Returns a static greeting")
    @ApiResponse(responseCode = "200", description = "Greeting returned")
    @GET
    fun helloWorld(): String

    @GET("long/path/{path...}")
    fun longPath(@PathParam path: List<String>): String

    @GET("query")
    fun query(@QueryParam names: List<String>): String

    @GET("query/model")
    fun queryModel(@QueryModel model: SelectQuery): String

    @GET("header")
    fun header(@HeaderParam name: String?): String

    @Security("auth-b")
    @GET("security/fun")
    fun funSecurity(): String

    @Operation(summary = "Блокировка сущностей")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Ок",
            content = [
                Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(LockAnswer::class))
                )
            ]
        ),
        ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
    )
    @Tag(
        name = "Блокировки сущностей",
        description = "предоставляет методы для работы с блокировками сущностей пользователей"
    )
    @GET("locks")
    fun locks(): List<LockAnswer>
}
