package ru.ttraum.example.api

import ru.ttraum.kontroller.core.http.Controller
import ru.ttraum.kontroller.core.http.GET

@Controller
interface HomePageApi {

    @GET
    fun helloWorld(): String
}
