package ru.ttraum.example.controller

import ru.ttraum.example.api.HomePageApi
import ru.ttraum.example.api.dto.LockAnswer
import ru.ttraum.example.api.dto.SelectQuery

class HomePageController : HomePageApi {
    override fun helloWorld(): String {
        return "hello world"
    }

    override fun longPath(path: List<String>): String {
        return path.joinToString("/")
    }

    override fun query(names: List<String>): String {
        return names
            .flatMap { it.split(",") }
            .joinToString("/")
    }

    override fun queryModel(model: SelectQuery): String {
        return (model.message ?: "empty") + " and " + (model.number ?: 0).toString()
    }

    override fun header(name: String?): String {
        return name ?: "empty"
    }

    override fun funSecurity(): String {
        return "fun security"
    }

    override fun locks(): List<LockAnswer> {
        return listOf(LockAnswer(entityId = "1", locked = true))
    }
}