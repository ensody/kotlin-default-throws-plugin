@file:Suppress("UnstableApiUsage")

package com.ensody.buildlogic

import com.android.build.gradle.internal.tasks.factory.dependsOn
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.net.URI

class BuildLogicPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            pluginManager.apply("com.ensody.build-logic-base")
            if (rootProject.name == "example") {
                if (!isRootProject) {
                    pluginManager.apply("org.jetbrains.kotlin.multiplatform")
                }
            } else if (!isRootProject) {
                pluginManager.apply("org.jetbrains.kotlin.jvm")
                if ("gradle-plugin" in project.name) {
                    pluginManager.apply("java-gradle-plugin")
                }
                pluginManager.apply("maven-publish")
            }
        }
    }
}

fun Project.initBuildLogic() {
    group = "com.ensody.kotlindefaultthrows"

    initBuildLogicBase {
        setupRepositories()

        if (rootProject.name == "example") return@initBuildLogicBase

        forwardTaskToExampleProject("assemble", "build")
        forwardTaskToExampleProject("check", "verification")
        forwardTaskToExampleProject("test", "verification")
        forwardTaskToExampleProject("allTests", "verification")
        forwardTaskToExampleProject("testAll", "verification")
        forwardTaskToExampleProject("ktlint", "verification")
        forwardTaskToExampleProject("ktlintFormat", "formatting")

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
}

fun Project.forwardTaskToExampleProject(name: String, group: String?) {
    tasks.register(name) {
        group?.let { this.group = it }
        doLast {
            shell("./gradlew $name", workingDir = file("example"), inheritIO = true)
        }
    }
}

fun Project.setupRepositories() {
    repositories {
        google()
        mavenCentral()
        if (System.getenv("RUNNING_ON_CI") != "true") {
            mavenLocal()
        }
    }
}

fun Project.setupBuildLogic(block: Project.() -> Unit) {
    setupBuildLogicBase {
        setupRepositories()
        if (extensions.findByType<KotlinMultiplatformExtension>() != null) {
            setupKmp {
                jvm()
                allIos()
            }
            tasks.register("testAll").dependsOn("jvmTest")
        }
        if (extensions.findByType<KotlinBaseExtension>() != null) {
            setupKtLint(libs.findLibrary("ktlint-cli").get())
        }
        if (extensions.findByType<KotlinJvmExtension>() != null) {
            setupKotlinJvm()
        }
        if (extensions.findByType<PublishingExtension>() != null) {
            setupPublication(
                withJavadocJar = true,
                withSources = true,
                signingKeyInfo = SigningKeyInfo.loadFromEnvOrNull(),
            ) {
                pom {
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
        block()
    }
}
