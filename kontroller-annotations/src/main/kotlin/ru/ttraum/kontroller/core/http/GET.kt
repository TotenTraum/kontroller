package ru.ttraum.kontroller.core.http

@Target(AnnotationTarget.FUNCTION)
annotation class GET(val path: String = "")