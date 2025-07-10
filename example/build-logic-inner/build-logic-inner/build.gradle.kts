import java.net.URI

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

repositories {
    maven {
        url = URI("file://$rootDir/../../build/localmaven")
    }
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    api(libs.gradle.kotlin)
    api(libs.gradle.kotlin.jvm)
    api("com.ensody.kotlindefaultthrows:gradle-plugin:${System.getenv("_LOCAL_PLUGIN_VERSION")}")
}

val autoDetectPluginRegex = Regex("""^(?:public\s+)?class\s+(\w+)BuildLogicPlugin\s*:.*$""", RegexOption.MULTILINE)
val autoDetectedPlugins = file("src").walkBottomUp().filter { it.extension == "kt" }.flatMap { file ->
    autoDetectPluginRegex.findAll(file.readText()).map { it.groupValues[1] }
}.toList()

gradlePlugin {
    plugins {
        autoDetectedPlugins.forEach {  variant ->
            create("com.ensody.build-logic.${variant.lowercase()}") {
                id = name
                implementationClass = "com.ensody.buildlogic.${variant}BuildLogicPlugin"
            }
        }
    }
}
