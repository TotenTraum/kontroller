package ru.ttraum.example.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SelectQuery(val message: String?, val number: Int?)