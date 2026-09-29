import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.pavlig43.quality) apply false
}

abstract class DetektAllTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val textReports: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun writeFindings() {
        val findings = textReports.files
            .filter { file -> file.isFile }
            .flatMap { file -> file.readLines() }
            .filter(String::isNotBlank)
            .distinct()
            .sorted()

        val report = outputFile.get().asFile
        report.parentFile.mkdirs()
        report.writeText(
            if (findings.isEmpty()) {
                "Detekt: no findings\n"
            } else {
                findings.joinToString(separator = "\n", postfix = "\n")
            },
        )

        if (findings.isEmpty()) {
            logger.lifecycle("Detekt: no findings")
        } else {
            logger.warn("Detekt findings: ${findings.size}")
            findings.forEach(logger::warn)
        }
    }
}

val prepareDetektAllReports = tasks.register<Delete>("prepareDetektAllReports")

val detektAll = tasks.register<DetektAllTask>("detektAll") {
    group = "verification"
    description = "Runs every Detekt check registered in the project."
    dependsOn(prepareDetektAllReports)
    outputFile.set(layout.buildDirectory.file("reports/detekt/detektAll.txt"))
}

val detektAutoFix = tasks.register("detektAutoFix") {
    group = "formatting"
    description = "Runs Detekt auto-correction in every Kotlin module."
}

val qualityPluginId = libs.plugins.pavlig43.quality.get().pluginId

subprojects {
    apply(plugin = qualityPluginId)

    tasks.withType<Detekt>().matching { task ->
        task.name != "detekt" && task.name != "detektAutoFix"
    }.configureEach {
        val detektTask = this
        prepareDetektAllReports.configure {
            delete(detektTask.txtReportFile)
        }
        detektTask.mustRunAfter(prepareDetektAllReports)
        detektAll.configure {
            dependsOn(detektTask)
            textReports.from(detektTask.txtReportFile)
        }
    }

    detektAutoFix.configure {
        dependsOn(
            tasks.withType<Detekt>().matching { task ->
                task.name == "detektAutoFix"
            },
        )
    }
}
