package ru.ttraum.example.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val code: Int, val message: String)