import com.ensody.buildlogic.setupBuildLogic
import com.ensody.buildlogic.withGeneratedBuildFile

plugins {
    id("com.ensody.build-logic.gradle")
    id("com.ensody.build-logic.publish")
}

setupBuildLogic {
    dependencies {
        implementation(libs.gradle.kotlin)
    }

    gradlePlugin {
        plugins {
            create("com.ensody.kotlindefaultthrows") {
                id = name
                implementationClass = "com.ensody.kotlindefaultthrows.gradle.KotlinDefaultThrowsGradleSubplugin"
            }
        }
    }

    withGeneratedBuildFile("buildConfig", "com/ensody/kotlindefaultthrows/gradle/BuildConfig.kt") {
        """
        package com.ensody.kotlindefaultthrows.gradle

        internal object BuildConfig {
            const val VERSION = "$version"
        }
        """
    }
}
