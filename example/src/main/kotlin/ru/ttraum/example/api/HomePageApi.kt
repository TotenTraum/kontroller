package ru.ttraum.example.api

import ru.ttraum.example.api.dto.SelectQuery
import ru.ttraum.kontroller.core.http.*

@Controller
interface HomePageApi {

    @GET
    fun helloWorld(): String

    @GET("long/path/{path...}")
    fun longPath(@PathParam path: List<String>): String

    @GET("query")
    fun query(@QueryParam names: List<String>): String

    @GET("query/model")
    fun queryModel(@QueryModel model: SelectQuery): String
}
