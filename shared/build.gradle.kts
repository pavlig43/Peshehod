import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import ru.pavlig43.peshehod.buildlogic.commonMainDependencies

plugins {
    alias(libs.plugins.pavlig43.kmp.library)
    alias(libs.plugins.pavlig43.kmp.compose)
    alias(libs.plugins.pavlig43.coroutines)
    alias(libs.plugins.pavlig43.koin)
    alias(libs.plugins.pavlig43.ktor)
    alias(libs.plugins.pavlig43.room)
    alias(libs.plugins.pavlig43.serialization)
    alias(libs.plugins.pavlig43.testing)
}

kotlin {
    targets.withType(KotlinNativeTarget::class.java).configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}

commonMainDependencies {
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
    implementation(libs.maplibre.compose)
}
