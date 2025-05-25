import com.ensody.buildlogic.initBuildLogic

plugins {
    id("com.ensody.build-logic")
    alias(libs.plugins.kotlin.cocoapods) apply false
    alias(libs.plugins.detekt)
}

initBuildLogic()
