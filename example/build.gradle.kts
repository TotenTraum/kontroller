plugins {
    kotlin("jvm")

    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.kspPlugin)
    alias(libs.plugins.ktorPlugin)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.kontrollerAnnotations)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.kotlinPoet)
    implementation(libs.kspApi)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.json)
    implementation(libs.ktor.server.di)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)

    ksp(projects.kontrollerKsp)
}

