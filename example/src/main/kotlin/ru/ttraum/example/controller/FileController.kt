package ru.ttraum.example.controller

import io.ktor.http.content.*
import ru.ttraum.example.api.FileApi

class FileController : FileApi {
    override suspend fun file(multipart: MultiPartData): String {
        val list = mutableListOf<String>()
        multipart.forEachPart { part -> list.add(part.name ?: "empty") }
        return list.joinToString("/")
    }
}