import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    kotlin("jvm") version libs.versions.kotlin apply false

    alias(libs.plugins.kspPlugin) apply false
    alias(libs.plugins.publishPlugin) apply false
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        val publishGroup = providers.gradleProperty("buildInfo.group").get()
        val publishVersion = providers.gradleProperty("buildInfo.version").get()

        extensions.configure<MavenPublishBaseExtension> {
            coordinates(publishGroup, project.name, publishVersion)

            pom {
                name = "kontroller"
                description.set(providers.provider { project.description })
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
    }
}
