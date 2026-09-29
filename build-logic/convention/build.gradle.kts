import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "ru.pavlig43.peshehod.buildlogic"
val projectJavaVersion = JavaVersion.toVersion(libs.versions.java.get())

java {
    sourceCompatibility = projectJavaVersion
    targetCompatibility = projectJavaVersion
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "peshehod.android.application"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.AndroidApplicationPlugin"
        }
        register("kmpLibrary") {
            id = "peshehod.kmp.library"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.KmpLibraryPlugin"
        }
        register("kmpCompose") {
            id = "peshehod.kmp.compose"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.KmpComposePlugin"
        }
        register("decompose") {
            id = "peshehod.decompose"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.DecomposePlugin"
        }
        register("coroutines") {
            id = "peshehod.coroutines"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.CoroutinesPlugin"
        }
        register("koin") {
            id = "peshehod.koin"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.KoinPlugin"
        }
        register("ktor") {
            id = "peshehod.ktor"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.KtorPlugin"
        }
        register("serialization") {
            id = "peshehod.serialization"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.SerializationPlugin"
        }
        register("testing") {
            id = "peshehod.testing"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.TestingPlugin"
        }
        register("room") {
            id = "peshehod.room"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.RoomPlugin"
        }
        register("quality") {
            id = "peshehod.quality"
            implementationClass = "ru.pavlig43.peshehod.buildlogic.QualityPlugin"
        }
    }
}
