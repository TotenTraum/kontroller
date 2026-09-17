plugins {
    kotlin("jvm")

    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.kspPlugin)
    alias(libs.plugins.ktorPlugin)
}

application {
    mainClass = "ru.ttraum.example.AppKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.kontrollerAnnotations)
    implementation(projects.kontrollerCore)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.json)
    implementation(libs.ktor.server.di)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.routing.openapi)
    compileOnly(libs.swagger.annotations)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)

    ksp(projects.kontrollerKsp)
}

