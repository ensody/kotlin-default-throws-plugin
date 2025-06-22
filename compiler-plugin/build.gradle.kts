import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import com.ensody.buildlogic.setupBuildLogic

plugins {
    id("com.ensody.build-logic.jvm")
    id("com.ensody.build-logic.publish")
    kotlin("kapt")
}

setupBuildLogic {
    dependencies {
        compileOnly(libs.google.autoservice)
        kapt(libs.google.autoservice)
        compileOnly(libs.kotlin.compiler.embeddable)
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }

    /*
    ./gradlew clean :lib:compileKotlinJvm --no-daemon -Dorg.gradle.debug=true -Dkotlin.compiler.execution.strategy="in-process" -Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=n"
     */
}
