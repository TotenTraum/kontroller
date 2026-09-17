package ru.ttraum.kontroller.core.http

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Security(vararg val providers: String, val nested: Boolean = false)
