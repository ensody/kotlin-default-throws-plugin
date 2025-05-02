import com.android.build.gradle.internal.cxx.io.writeTextIfDifferent

dependencies {
    implementation(libs.kotlin.gradle)
}

gradlePlugin {
    plugins {
        create("com.ensody.kotlindefaultthrows") {
            id = name
            implementationClass = "com.ensody.kotlindefaultthrows.gradle.KotlinDefaultThrowsGradleSubplugin"
        }
    }
}

val buildConfig = """
package com.ensody.kotlindefaultthrows.gradle

internal object BuildConfig {
    const val VERSION = "$version"
}
""".trimIndent().trimStart()
val generatedDir = "$projectDir/build/generated/source/buildConfig"
sourceSets["main"].kotlin.srcDir(generatedDir)
file("$generatedDir/com/ensody/kotlindefaultthrows/gradle/BuildConfig.kt").writeTextIfDifferent(buildConfig)
