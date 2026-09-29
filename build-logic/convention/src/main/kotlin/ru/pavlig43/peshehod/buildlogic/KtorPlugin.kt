package ru.pavlig43.peshehod.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class KtorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            commonMainDependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.json)
            }
            androidMainDependencies {
                implementation(libs.ktor.client.okhttp)
            }
            iosMainDependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}
