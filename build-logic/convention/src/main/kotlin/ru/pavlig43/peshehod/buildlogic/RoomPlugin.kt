package ru.pavlig43.peshehod.buildlogic

import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.plugins.ksp.get().pluginId)
            apply(plugin = libs.plugins.room.get().pluginId)

            extensions.configure<KspExtension> {
                arg("room.generateKotlin", "true")
            }
            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }
            commonMainDependencies {
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
            dependencies {
                add("kspAndroid", libs.androidx.room.compiler)
                add("kspIosArm64", libs.androidx.room.compiler)
                add("kspIosSimulatorArm64", libs.androidx.room.compiler)
            }
        }
    }
}
