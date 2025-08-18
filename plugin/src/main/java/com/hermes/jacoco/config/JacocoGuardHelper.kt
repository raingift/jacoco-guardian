package com.hermes.jacoco.config

import com.hermes.jacoco.const.RELATIVE_LOCAL_DIR_PATH
import com.hermes.jacoco.guardian.JacocoExtension
import com.hermes.jacoco.guardian.enableAutoCoverage
import com.hermes.jacoco.guardian.enableDebug
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Print jacoco coverage inputs and outputs paths
 *
 * test function
 */
fun Project.addTestJacocoClassPaths(jacocoExt: JacocoExtension) {
    val enable = enableDebug(jacocoExt)
    println("[testJacocoClassPaths] enableTestJacocoClassPaths: $enable")
    if (!enable) {
        return
    }
    tasks.register("testJacocoClassPaths") {
        it.group = "Reporting"
        it.doLast {
            val paths = listOf(
                "intermediates/javac/*/classes/**",
                "tmp/kotlin-classes/**",
                "classes",
                "outputs/unit_test_code_coverage/*UnitTest/*.exec",
                "outputs/code-coverage/*.ec"
            )
            val projectFiles = fileTree("$buildDir").include(paths)
            files(projectFiles).forEach { file ->
                println("${if (file.exists()) "✅" else "❌"} ${file.absolutePath}")
            }
        }
    }
}

/**
 * After packaging and building, quickly package source and class files
 */
fun Project.addJacocoResourcePackage(jacocoExt: JacocoExtension) {
    val enable = enableAutoCoverage(jacocoExt)
    println("[packageJacocoResource] enablePackageJacocoResource: $enable")
    if (!enable) {
        return
    }
    tasks.register("packageJacocoResource", Zip::class.java) {
        it.group = "Reporting"
        it.description = "jacoco resource package"
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
        it.archiveFileName.set("project_bundle_${timestamp}.zip")
        it.destinationDirectory.set(rootProject.layout.buildDirectory.dir("outputs/tmp"))

        subprojects.mapNotNull { project ->
            println("projectDir:${project.projectDir} >>> buildDir: ${project.buildDir}")
            it.from(project.buildDir) { copy ->
                copy.include(
                    "intermediates/javac/**",
                    "tmp/kotlin-classes/**",
                    "classes/**"
                )
                copy.into("$name/build/")
            }

            it.from(project.projectDir) { copy ->
                copy.include("src/main/java/**")
                copy.into("$name/")
            }
        }

        var startTime = 0L
        it.doFirst {
            startTime = getCurrentTime()
        }

        it.doLast {
            println("execute package files cost time: ${getCurrentTime() - startTime}ms")
        }
    }
}

fun defaultGenerateLocalDir(target: Project) {
    val localDirPath = "${target.rootProject.buildDir}/$RELATIVE_LOCAL_DIR_PATH"
    println("projectsEvaluated: $localDirPath")
    target.gradle.projectsEvaluated {
        val file = File(localDirPath)
        if (!file.exists()) {
            file.mkdirs()
        }
    }
}

private fun getCurrentTime(): Long = System.currentTimeMillis()

