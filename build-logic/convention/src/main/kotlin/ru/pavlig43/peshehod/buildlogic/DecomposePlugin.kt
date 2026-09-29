package ru.pavlig43.peshehod.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class DecomposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            commonMainDependencies {
                implementation(libs.decompose)
                implementation(libs.decompose.compose)
            }
        }
    }
}
