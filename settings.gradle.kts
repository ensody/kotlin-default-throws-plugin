pluginManagement {
    includeBuild("build-logic")
    includeBuild("gradle-plugin")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":compiler-plugin")
