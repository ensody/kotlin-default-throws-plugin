import com.ensody.buildlogic.initBuildLogic

buildscript {
    dependencies {
        classpath("com.ensody.kotlindefaultthrows:gradle-plugin:+")
    }
}

plugins {
    id("com.ensody.build-logic")
    alias(libs.plugins.kotlin.cocoapods) apply false
    alias(libs.plugins.detekt)
}

initBuildLogic()
