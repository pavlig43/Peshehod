package ru.pavlig43.peshehod.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class CoroutinesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            commonMainDependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
            commonTestDependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
            androidMainDependencies {
                implementation(libs.kotlinx.coroutines.android)
            }
        }
    }
}
