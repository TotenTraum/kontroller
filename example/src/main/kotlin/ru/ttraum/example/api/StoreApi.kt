package ru.ttraum.example.api

import ru.ttraum.example.api.dto.StoreData
import ru.ttraum.kontroller.core.http.*
import java.util.*

@Controller(basePath = "router")
interface StoreApi {

    @GET("{id}")
    @HttpHeader("Content-Type", "application/json")
    fun get(@PathParam id: UUID): StoreData

    @POST("{id}")
    @HttpHeader("Content-Type", "application/json")
    fun set(@PathParam id: UUID, @BodyParam data: StoreData)
}
