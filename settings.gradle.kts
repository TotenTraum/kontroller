pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    dependencyResolutionManagement {
        repositories {
            mavenCentral()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kontroller"



enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":kontroller-annotations")
include(":kontroller-core")
include(":kontroller-ksp")
include(":example")
