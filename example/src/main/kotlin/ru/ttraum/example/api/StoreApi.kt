package ru.ttraum.example.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import ru.ttraum.example.api.dto.StoreData
import ru.ttraum.kontroller.core.http.*
import java.util.*

@Controller(basePath = "router")
interface StoreApi {

    @GET("{id}")
    @HttpHeader("Content-Type", "application/json")
    fun get(@PathParam id: UUID): StoreData

    @Operation(summary = "Сохранить данные")
    @ApiResponse(responseCode = "200", description = "Ок")
    @POST("{id}")
    @HttpHeader("Content-Type", "application/json")
    fun set(
        @Parameter(description = "Идентификатор записи")
        @PathParam id: UUID,
        @RequestBody(
            description = "Сохраняемые данные",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = StoreData::class))]
        )
        @BodyParam data: StoreData
    )
}
