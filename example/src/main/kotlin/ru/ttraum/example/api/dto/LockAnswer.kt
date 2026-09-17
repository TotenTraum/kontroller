package ru.ttraum.example.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LockAnswer(val entityId: String, val locked: Boolean)
