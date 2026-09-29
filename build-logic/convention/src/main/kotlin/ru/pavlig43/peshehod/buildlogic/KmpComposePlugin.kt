package ru.pavlig43.peshehod.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KmpComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.plugins.compose.multiplatform.get().pluginId)
            apply(plugin = libs.plugins.compose.compiler.get().pluginId)

            commonMainDependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.components.resources)
            }

            androidMainDependencies {
                runtimeOnly(libs.compose.ui.tooling)
            }
        }
    }
}
