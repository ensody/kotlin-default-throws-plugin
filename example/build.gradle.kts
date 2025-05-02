import com.ensody.buildlogic.initBuildLogic

plugins {
    id("com.ensody.build-logic")
    alias(libs.plugins.kotlin.cocoapods) apply false
}

initBuildLogic()
