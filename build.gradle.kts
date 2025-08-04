plugins {
    kotlin("jvm") version libs.versions.kotlin apply false

    alias(libs.plugins.kspPlugin) apply false
    alias(libs.plugins.publishPlugin) apply false
}