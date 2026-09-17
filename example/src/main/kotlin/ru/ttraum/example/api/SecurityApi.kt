package ru.ttraum.example.api

import ru.ttraum.kontroller.core.http.Controller
import ru.ttraum.kontroller.core.http.GET
import ru.ttraum.kontroller.core.http.Security

@Controller
@Security("auth-a")
interface SecurityApi {

    @GET("security/class")
    fun classSecurity(): String

    @Security("auth-b")
    @GET("security/class-and-fun")
    fun classAndFunSecurity(): String

    @Security("auth-с", nested = true)
    @GET("security/nested-fun")
    fun nestedFunSecurity(): String
}
