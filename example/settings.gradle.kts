pluginManagement {
    includeBuild("..")
    includeBuild("../build-logic")
    include(":gradle-plugin")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("..")
include(":lib")
