import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import com.ensody.buildlogic.setupBuildLogic

plugins {
    id("com.ensody.build-logic.kmp")
    id("com.ensody.build-logic.cocoapods")
    id("com.ensody.build-logic.defaultthrows")
}

setupBuildLogic {
    kotlin {
        sourceSets.commonTest.dependencies {
            api(kotlin("reflect"))
            api(libs.kotlin.test.main)
        }
        sourceSets["jvmCommonTest"].dependencies {
            api(libs.kotlin.test.junit)
        }
        cocoapods {
            name = "Shared"
            summary = "Test for Kotlin compiler plugin that adds default @Throws annotations"
            homepage = "https://github.com/ensody/kotlin-default-throws-plugin"
            license = "Apache 2.0"

            ios.deploymentTarget = "15.0"

            framework {
                config()
            }
        }
    }
}

fun Framework.config() {
    baseName = "Shared"
    binaryOption("bundleVersion", project.version.toString().split("-")[0])
}
