import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm")
    alias(libs.plugins.publishPlugin)
}

mavenPublishing {
    val group = providers.gradleProperty("buildInfo.group").get()
    val version = providers.gradleProperty("buildInfo.version").get()
    coordinates(group, "kontroller-annotations", version)

    pom {
        name = "kontroller"
        description = "Annotation library for generating controller routing for ktor"
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