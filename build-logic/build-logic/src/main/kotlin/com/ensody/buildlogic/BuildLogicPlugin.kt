@file:Suppress("UnstableApiUsage")

package com.ensody.buildlogic

import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories
import java.net.URI

class BuildLogicPlugin : Plugin<Project> {
    override fun apply(target: Project) {}
}

fun Project.initBuildLogic() {
    group = "com.ensody.kotlindefaultthrows"

    initBuildLogicBase()
    allprojects {
        setupBuildLogic()
    }

    if (rootProject.name == "example") return

    configure<NexusPublishExtension> {
        repositories {
            sonatype {
                nexusUrl.set(URI("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(URI("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
                username = System.getenv("PUBLICATION_USERNAME")
                password = System.getenv("PUBLICATION_PASSWORD")
            }
        }
    }
}

fun Project.setupBuildLogic() {
    pluginManager.apply("com.ensody.build-logic")

    repositories {
        google()
        mavenCentral()
    }

    if (isRootProject) return

    setupBuildLogicBase()

    if (rootProject.name == "example") {
        setupKmp {
            jvm()
            allIos()
        }
    } else {
        setupKotlinJvm()

        if (project.name == "gradle-plugin") {
            pluginManager.apply("java-gradle-plugin")
        }

        setupPublication(
            withJavadocJar = true,
            withSources = true,
            signingKeyInfo = SigningKeyInfo.loadFromEnvOrNull(),
        ) {
            pom {
                name = "${rootProject.name}: ${project.name}"
                description = "Kotlin compiler plugin that adds default @Throws annotations"
                url = "https://github.com/ensody/kotlin-default-throws-plugin"
                licenses {
                    apache2()
                }
                scm {
                    url.set(this@pom.url)
                }
                developers {
                    developer {
                        id = "wkornewald"
                        name = "Waldemar Kornewald"
                        url = "https://www.ensody.com"
                        organization = "Ensody GmbH"
                        organizationUrl = url
                    }
                }
            }
        }
    }
    setupKtLint(libs.findLibrary("ktlint-cli").get())
}
