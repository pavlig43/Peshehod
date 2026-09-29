package ru.pavlig43.peshehod.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class SerializationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.plugins.kotlinx.serialization.get().pluginId)

            commonMainDependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
