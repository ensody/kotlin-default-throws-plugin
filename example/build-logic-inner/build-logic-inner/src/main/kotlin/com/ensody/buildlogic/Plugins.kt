@file:Suppress("UnstableApiUsage")

package com.ensody.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class InnerBuildLogicPlugin : Plugin<Project> {
    override fun apply(target: Project) {
    }
}

class DefaultThrowsBuildLogicPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.ensody.kotlindefaultthrows")
    }
}
