package ru.ttraum.kontroller.core.http

@Target(AnnotationTarget.FUNCTION)
annotation class HttpMethod(val path: String = "", val method: String)