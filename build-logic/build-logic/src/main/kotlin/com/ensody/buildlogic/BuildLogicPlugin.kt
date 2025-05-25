@file:Suppress("UnstableApiUsage")

package com.ensody.buildlogic

import com.android.build.gradle.internal.tasks.factory.dependsOn
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
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
                    pluginManager.apply("io.gitlab.arturbosch.detekt")
                }
            } else if (!isRootProject) {
                pluginManager.apply("org.jetbrains.kotlin.jvm")
                if ("gradle-plugin" in project.name) {
                    pluginManager.apply("java-gradle-plugin")
                }
                pluginManager.apply("io.gitlab.arturbosch.detekt")
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

        forwardTaskToExampleProject("assemble", "build") {
            doFirst {
                shell("./gradlew publishAllPublicationsToLocalMavenRepository", inheritIO = true)
            }
        }
        forwardTaskToExampleProject("check", "verification")
        forwardTaskToExampleProject("test", "verification")
        forwardTaskToExampleProject("allTests", "verification")
        forwardTaskToExampleProject("testAll", "verification")
        forwardTaskToExampleProject("ktlint", "verification")
        forwardTaskToExampleProject("ktlintFormat", "formatting")
        forwardTaskToExampleProject("forwardDetekt", "other", "detekt")
        tasks.getByName("detekt").dependsOn("forwardDetekt")

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

fun Project.forwardTaskToExampleProject(
    name: String,
    group: String?,
    targetName: String = name,
    block: Task.() -> Unit = {},
) {
    tasks.register(name) {
        group?.let { this.group = it }
        doLast {
            shell(
                "./gradlew --no-daemon $targetName",
                workingDir = file("example"),
                env = mapOf("_LOCAL_PLUGIN_VERSION" to version.toString()),
                inheritIO = true,
            )
        }
        block()
    }
}

fun Project.setupRepositories() {
    repositories {
        if (rootProject.name == "example") {
            maven {
                url = URI("file://$rootDir/../build/localmaven")
            }
        }
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
        if (extensions.findByType<DetektExtension>() != null) {
            setupDetekt()
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
            configure<PublishingExtension> {
                // Local publication for the example project
                repositories {
                    maven {
                        name = "localMaven"
                        url = URI("file://$rootDir/build/localmaven")
                    }
                }
            }
        }
        block()
    }
}
