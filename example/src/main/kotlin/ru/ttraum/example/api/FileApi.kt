package ru.ttraum.example.api

import io.ktor.http.content.*
import ru.ttraum.kontroller.core.http.Controller
import ru.ttraum.kontroller.core.http.MultipartParam
import ru.ttraum.kontroller.core.http.POST

@Controller
interface FileApi {

    @POST("file")
    suspend fun file(@MultipartParam multipart: MultiPartData): String
}
