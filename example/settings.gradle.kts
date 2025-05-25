pluginManagement {
    includeBuild("../build-logic")
    repositories {
        maven {
            url = java.net.URI("file://$rootDir/../build/localmaven")
        }
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":lib")
