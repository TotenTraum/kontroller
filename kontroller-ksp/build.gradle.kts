plugins {
    kotlin("jvm")
    alias(libs.plugins.kspPlugin)
    alias(libs.plugins.publishPlugin)
}

description = "A library for generating controller routing using ksp for ktor"

dependencies {
    implementation(projects.kontrollerAnnotations)
    implementation(libs.kotlinPoet)
    implementation(libs.arrow.core)
    implementation(libs.arrow.functions)
    implementation(libs.kspApi)
    implementation(libs.ktor.server.core)
}