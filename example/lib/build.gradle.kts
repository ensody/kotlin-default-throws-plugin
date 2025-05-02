import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

plugins {
    id("com.ensody.kotlindefaultthrows")
    alias(libs.plugins.kotlin.cocoapods)
}

dependencies {
    commonTestApi(kotlin("reflect"))
    commonTestApi(libs.kotlin.test.main)
    jvmCommonTestApi(libs.kotlin.test.junit)
}

kotlin {
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

fun Framework.config() {
    baseName = "Shared"
    binaryOption("bundleVersion", project.version.toString().split("-")[0])
}
