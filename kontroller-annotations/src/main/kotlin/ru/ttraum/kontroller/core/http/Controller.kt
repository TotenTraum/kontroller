@file:Suppress("unused")

package ru.ttraum.kontroller.core.http

@Target(AnnotationTarget.CLASS)
annotation class Controller(val basePath: String = "")
