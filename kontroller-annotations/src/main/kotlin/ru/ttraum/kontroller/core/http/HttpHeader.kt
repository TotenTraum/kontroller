package ru.ttraum.kontroller.core.http

@Target(AnnotationTarget.FUNCTION)
annotation class HttpHeader(val name: String, val value: String, vararg val values: String)
