package ru.ttraum.example.controller

import ru.ttraum.example.api.StoreApi
import ru.ttraum.example.api.dto.StoreData
import ru.ttraum.example.controller.exception.NotFoundException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StoreController : StoreApi {
    private val cache: MutableMap<UUID, StoreData> = ConcurrentHashMap()

    override fun get(id: UUID): StoreData {
        return cache[id] ?: throw NotFoundException()
    }

    override fun set(id: UUID, data: StoreData) {
        cache[id] = data
    }
}