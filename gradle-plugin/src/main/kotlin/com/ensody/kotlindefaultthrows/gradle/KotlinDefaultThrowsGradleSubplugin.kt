package com.ensody.kotlindefaultthrows.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class KotlinDefaultThrowsGradleSubplugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider {
            emptyList()
        }

    override fun getCompilerPluginId(): String = "kotlinDefaultThrowsPlugin"

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        true

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.ensody.kotlindefaultthrows",
        artifactId = "compiler-plugin",
        version = BuildConfig.VERSION,
    )
}
