package ru.ttraum.kontroller.core.http

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MultipartParam(val formFieldLimit: Long = -1)