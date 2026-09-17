plugins {
    kotlin("jvm")
    alias(libs.plugins.publishPlugin)
}

description = "Core library for generating controller routing for ktor"

dependencies {
    implementation(libs.ktor.server.core)
}