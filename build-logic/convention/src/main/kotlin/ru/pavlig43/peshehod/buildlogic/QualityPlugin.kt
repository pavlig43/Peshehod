package ru.pavlig43.peshehod.buildlogic

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.DetektGenerateConfigTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

class QualityPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withType(KotlinBasePlugin::class.java) {
            with(target) {
                apply(plugin = libs.plugins.detekt.get().pluginId)

                configurations.matching { configuration ->
                    configuration.name == "androidCompileClasspath"
                }.configureEach {
                    attributes.attribute(
                        Attribute.of("artifactType", String::class.java),
                        "android-classes-jar",
                    )
                }

                extensions.configure<DetektExtension> {
                    config.setFrom(rootProject.files("default-detekt-config.yml"))
                    buildUponDefaultConfig = false
                    autoCorrect = false
                    parallel = true
                    ignoreFailures = true
                }

                tasks.register<Detekt>("detektAutoFix") {
                    group = "formatting"
                    description = "Runs Detekt auto-correction for this project."
                    source = target.fileTree(target.projectDir) {
                        include("src/**/*.kt")
                        include("*.gradle.kts")
                        exclude("**/build/**")
                    }
                    config.setFrom(rootProject.files("default-detekt-config.yml"))
                    buildUponDefaultConfig = false
                    autoCorrect = true
                    parallel = true
                    ignoreFailures = true
                }

                tasks.withType<Detekt>().configureEach {
                    val reportName = name

                    exclude { element ->
                        val path = element.file.invariantSeparatorsPath
                        path.contains("/build/") ||
                            path.contains("/generated/") ||
                            path.contains("/ksp/") ||
                            path.contains("/room/")
                    }

                    reports {
                        html.required.set(true)
                        md.required.set(true)
                        txt.required.set(true)
                        txt.outputLocation.set(
                            target.layout.buildDirectory.file("reports/detekt/$reportName.txt"),
                        )
                    }

                    jvmTarget = libs.versions.java.get()
                    ignoreFailures = true
                    if (name != "detektAutoFix") {
                        autoCorrect = false
                    }
                }

                tasks.withType<DetektGenerateConfigTask>().configureEach {
                    enabled = false
                }
                tasks.withType<DetektCreateBaselineTask>().configureEach {
                    enabled = false
                }

                dependencies {
                    detektPlugins(libs.detekt.formatting)
                }
                withPlugin(libs.plugins.pavlig43.kmp.compose) {
                    dependencies {
                        detektPlugins(libs.detekt.compose)
                    }
                }
                withPlugin(libs.plugins.pavlig43.decompose) {
                    dependencies {
                        detektPlugins(libs.detekt.decompose)
                    }
                }
            }
        }
    }
}

private fun DependencyHandlerScope.detektPlugins(
    dependency: Provider<MinimalExternalModuleDependency>,
) {
    add("detektPlugins", dependency)
}
