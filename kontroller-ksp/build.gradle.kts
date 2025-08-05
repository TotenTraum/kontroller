import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm")
    alias(libs.plugins.kspPlugin)
    alias(libs.plugins.publishPlugin)
}

dependencies {
    implementation(projects.kontrollerAnnotations)
    implementation(libs.kotlinPoet)
    implementation(libs.arrow.core)
    implementation(libs.arrow.functions)
    implementation(libs.kspApi)
    implementation(libs.ktor.server.core)
}

mavenPublishing {
    val group = providers.gradleProperty("buildInfo.group").get()
    val version = providers.gradleProperty("buildInfo.version").get()
    coordinates(group, "kontroller-ksp", version)

    pom {
        name = "kontroller"
        description = "A library for generating controller routing using ksp for ktor"
        url = "https://github.com/TotenTraum/kontroller"

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        scm {
            url = "https://github.com/TotenTraum/kontroller"
            connection = "scm:git:git://github.com/TotenTraum/kontroller.git"
            developerConnection = "scm:git:ssh://git@github.com/TotenTraum/kontroller.git"
        }

        developers {
            developer {
                id = "ttraum"
                name = "Azim Usmanov"
                url = "https://github.com/ttraum/"
            }
        }
    }

    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = true,
        )
    )

    publishToMavenCentral()
    signAllPublications()
}