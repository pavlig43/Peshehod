package ru.pavlig43.peshehod.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

class KmpLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.plugins.kotlin.multiplatform.get().pluginId)
            apply(plugin = libs.plugins.android.kmp.library.get().pluginId)

            val compileSdkApiLevel = libs.versions.androidCompileSdk.get().toInt()
            val jvmTarget = JvmTarget.fromTarget(libs.versions.java.get())

            kotlinMultiplatformConfig {
                compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
                iosArm64()
                iosSimulatorArm64()

                targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                    namespace = androidNamespace()
                    compileSdk {
                        version = release(compileSdkApiLevel)
                    }
                    minSdk = libs.versions.androidMinSdk.get().toInt()
                    androidResources.enable = true
                    compilerOptions.jvmTarget.set(jvmTarget)
                }
            }
        }
    }
}

/**
 * Строит Android namespace из пути Gradle-проекта с префиксом `ru.pavlig43.peshehod`.
 * Части пути разделяет точками и приводит к допустимому виду через [asJavaIdentifier].
 * Для корневого проекта возвращает только префикс.
 *
 * Примеры:
 * - `:shared:feature-auth` → `ru.pavlig43.peshehod.shared.feature_auth`.
 * - `:` → `ru.pavlig43.peshehod`.
 */
private fun Project.androidNamespace(): String {
    val suffix = path
        .split(":")
        .filter { segment -> segment.isNotBlank() }
        .joinToString(".") { segment -> segment.asJavaIdentifier() }

    if (suffix.isBlank()) {
        return "ru.pavlig43.peshehod"
    }
    return "ru.pavlig43.peshehod.$suffix"
}

/**
 * Заменяет все символы, кроме латинских букв, цифр и `_`, на `_`.
 * Если строка пуста или начинается с цифры, добавляет `_` в начало.
 * Ключевые слова Java не проверяет.
 *
 * Примеры:
 * ```kotlin
 * "feature-auth".asJavaIdentifier() // "feature_auth"
 * "123core".asJavaIdentifier() // "_123core"
 * "".asJavaIdentifier() // "_"
 * ```
 */
private fun String.asJavaIdentifier(): String {
    val normalized = replace(Regex("[^A-Za-z0-9_]"), "_")

    if (normalized.firstOrNull()?.let(Character::isJavaIdentifierStart) == true) {
        return normalized
    }
    return "_$normalized"
}
