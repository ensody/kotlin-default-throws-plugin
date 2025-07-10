import com.ensody.buildlogic.initBuildLogic

plugins {
    id("com.ensody.build-logic.base")
    id("com.ensody.build-logic.dokka")
}

// Needed for debugging
System.setProperty("kotlin.compiler.execution.strategy", "in-process")
initBuildLogic()
