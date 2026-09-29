package ru.pavlig43.peshehod.buildlogic

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

internal fun Project.withPlugin(plugin: Provider<PluginDependency>, block: Project.() -> Unit) {
    pluginManager.withPlugin(plugin.get().pluginId) {
        block()
    }
}

internal fun Project.kotlinMultiplatformConfig(block: KotlinMultiplatformExtension.() -> Unit) {
    withPlugin(libs.plugins.kotlin.multiplatform) {
        extensions.configure<KotlinMultiplatformExtension>(block)
    }
}

fun Project.commonMainDependencies(block: KotlinDependencyHandler.() -> Unit) {
    kotlinMultiplatformConfig {
        sourceSets.commonMain.dependencies(block)
    }
}

fun Project.commonTestDependencies(block: KotlinDependencyHandler.() -> Unit) {
    kotlinMultiplatformConfig {
        sourceSets.commonTest.dependencies(block)
    }
}

fun Project.androidMainDependencies(block: KotlinDependencyHandler.() -> Unit) {
    kotlinMultiplatformConfig {
        // Android target may not exist in desktop-only modules
        sourceSets.findByName("androidMain")?.dependencies(block)
    }
}

fun Project.androidDebugDependencies(block: KotlinDependencyHandler.() -> Unit) {
    kotlinMultiplatformConfig {
        // Android debug variant may not exist in all modules
        sourceSets.findByName("androidDebug")?.dependencies(block)
    }
}

fun Project.androidInstrumentedTestDependencies(block: KotlinDependencyHandler.() -> Unit) {
    kotlinMultiplatformConfig {
        // Android instrumented tests may not exist in all modules
        sourceSets.findByName("androidInstrumentedTest")?.dependencies(block)
    }
}

fun Project.androidDeviceTestDependencies(block: KotlinDependencyHandler.() -> Unit) {
    kotlinMultiplatformConfig {
        sourceSets.findByName("androidDeviceTest")?.dependencies(block)
    }
}

fun Project.iosMainDependencies(block: KotlinDependencyHandler.() -> Unit) {
    kotlinMultiplatformConfig {
        sourceSets.iosMain.dependencies(block)
    }
}
