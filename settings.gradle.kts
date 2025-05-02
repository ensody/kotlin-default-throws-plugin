pluginManagement {
    includeBuild("build-logic")
    includeBuild("example")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":compiler-plugin")
include(":gradle-plugin")
