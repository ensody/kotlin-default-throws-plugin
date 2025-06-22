@file:Suppress("UnstableApiUsage")

package com.ensody.buildlogic

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlatformExtension
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.net.URI

/** Base setup. */
class BaseBuildLogicPlugin : Plugin<Project> {
    override fun apply(target: Project) {}
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
        forwardTaskToExampleProject("detekt", "verification")
    }
}

fun Project.forwardTaskToExampleProject(
    name: String,
    group: String?,
    targetName: String = name,
    block: Task.() -> Unit = {},
) {
    tasks.findByName(name)?.let {
        forwardTaskToExampleProject("forward$name", group, name, block)
        it.dependsOn("forward$name")
        return
    }
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
        if (extensions.findByType<JavaPlatformExtension>() != null) {
            setupPlatformProject()
        }
        if (extensions.findByType<BaseExtension>() != null) {
            setupAndroid(coreLibraryDesugaring = libs.findLibrary("desugarJdkLibs").get())
        }
        if (extensions.findByType<KotlinMultiplatformExtension>() != null) {
            setupKmp {
                androidTarget {
                    publishLibraryVariants("release")
                }
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
        if (extensions.findByType<DokkaExtension>() != null) {
            setupDokka(copyright = "Ensody GmbH")
        }
        if (extensions.findByType<CatalogPluginExtension>() != null) {
            setupVersionCatalog()
        }
        extensions.findByType<MavenPublishBaseExtension>()?.apply {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
            if (System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")?.isNotBlank() == true) {
                signAllPublications()
            }
            pom {
                description = project.description?.takeIf { it.isNotBlank() }
                    ?: "Kotlin compiler plugin that adds default @Throws annotations"
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
