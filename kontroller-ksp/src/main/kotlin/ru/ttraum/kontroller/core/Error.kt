package ru.ttraum.kontroller.core

interface Error {
    fun level(): Level = Level.Warning()

    fun error(): String
}