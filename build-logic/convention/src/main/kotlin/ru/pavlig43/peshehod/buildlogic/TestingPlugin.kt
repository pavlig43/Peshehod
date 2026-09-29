package ru.pavlig43.peshehod.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

class TestingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            kotlinMultiplatformConfig {
                targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                    withHostTestBuilder {}.configure {}
                    withDeviceTestBuilder {
                        sourceSetTreeName = "test"
                    }.configure {
                        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    }
                }
            }

            commonTestDependencies {
                implementation(libs.kotlin.test)
                implementation(libs.turbine)
                implementation(libs.koin.test)
            }
            androidDeviceTestDependencies {
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.runner)
            }

            withPlugin(libs.plugins.pavlig43.kmp.compose) {
                commonTestDependencies {
                    implementation(libs.compose.ui.test)
                }
            }

            withPlugin(libs.plugins.pavlig43.room) {
                androidDeviceTestDependencies {
                    implementation(libs.androidx.room.testing)
                }
            }

            tasks.withType<Test>().configureEach {
                testLogging {
                    events("failed", "passed", "skipped")
                    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                }
            }
        }
    }
}
