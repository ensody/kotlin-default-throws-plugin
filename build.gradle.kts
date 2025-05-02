import com.ensody.buildlogic.initBuildLogic
import org.gradle.kotlin.dsl.libs

plugins {
    id("com.ensody.build-logic")
    alias(libs.plugins.nexusPublish)
}

// Needed for debugging
System.setProperty("kotlin.compiler.execution.strategy", "in-process")
initBuildLogic()
