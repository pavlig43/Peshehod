package ru.pavlig43.peshehod.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.plugins.android.application.get().pluginId)
            apply(plugin = libs.plugins.compose.compiler.get().pluginId)

            val projectJavaVersion = JavaVersion.toVersion(libs.versions.java.get())

            extensions.configure<ApplicationExtension> {
                namespace = "ru.pavlig43.peshehod"
                compileSdk {
                    version = release(libs.versions.androidCompileSdk.get().toInt())
                }

                defaultConfig {
                    applicationId = "ru.pavlig43.peshehod"
                    minSdk = libs.versions.androidMinSdk.get().toInt()
                    targetSdk = libs.versions.androidTargetSdk.get().toInt()
                    versionCode = libs.versions.version.code.get().toInt()
                    versionName = libs.versions.version.name.get()
                }

                buildFeatures.compose = true

                compileOptions {
                    sourceCompatibility = projectJavaVersion
                    targetCompatibility = projectJavaVersion
                }

                buildTypes {
                    getByName("debug") {
                        applicationIdSuffix = ".debug"
                        versionNameSuffix = "-debug"
                    }
                    getByName("release") {
                        optimization {
                            enable = true
                        }
                    }
                }

                packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }
}
