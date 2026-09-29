package ru.pavlig43.peshehod.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class KoinPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            commonMainDependencies {
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
            }
            androidMainDependencies {
                implementation(libs.koin.android)
            }
        }
    }
}
