package com.hermes.jacoco.guardian

import com.android.build.gradle.AppExtension
import com.hermes.jacoco.config.addJacocoInitTask
import com.hermes.jacoco.config.addJacocoResourcePackage
import com.hermes.jacoco.config.addModuleJacocoPlugin
import com.hermes.jacoco.config.addTestJacocoClassPaths
import com.hermes.jacoco.config.addRootJacocoConfigTask
import com.hermes.jacoco.config.defaultGenerateLocalDir
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

/**
 * Jacoco Guard to local and devices jacoco coverage
 */
class JacocoGuardPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("jacocoConfig", JacocoExtension::class.java)
        println("JacocoPlugin Start Loaded!")

        target.allprojects.forEach { project ->
            project.afterEvaluate {
                if (extension.enableJacoco == false) {
                    return@afterEvaluate
                }
                if (project == project.rootProject) {
                    configRootJacoco(it, extension)
                } else {
                    addJacocoPlugin(it, extension)
                }
            }
        }
    }

    private fun configRootJacoco(
        target: Project,
        extension: JacocoExtension,
    ) {
        if (!enableFullCoverage(extension)) {
            return
        }
        println("current project eq root project: ${target == target.rootProject}")

        target.addJacocoInitTask(extension)
        target.addRootJacocoConfigTask(extension)
        target.addTestJacocoClassPaths(extension)
        target.addJacocoResourcePackage(extension)

        defaultGenerateLocalDir(target)
    }

    private fun addJacocoPlugin(
        project: Project,
        jacocoExt: JacocoExtension,
    ) {
        val isAndroidApplication = project.plugins.hasPlugin("com.android.application")
        val isAndroidLibrary = project.plugins.hasPlugin("com.android.library")
        if (isAndroidApplication || isAndroidLibrary) {
            val isIncluded = includeProjects(jacocoExt, project.path)
            if (isIncluded) {
                project.addModuleJacocoPlugin(jacocoExt)
                project.addTestJacocoClassPaths(jacocoExt)
            }
        }

        if (isAndroidApplication) {
            addPackageDepends(jacocoExt, project)
        }
    }

    private fun addPackageDepends(
        jacocoExt: JacocoExtension,
        project: Project,
    ) {
        val enable = enableAutoCoverage(jacocoExt)
        if (!enable) {
            return
        }
        val android = project.extensions.findByType(AppExtension::class.java)
        android?.applicationVariants?.all { variant ->
            val assembleTask = variant.assembleProvider.get()
            val packageJacocoResource =
                (project.rootProject.tasks.findByName("packageJacocoResource") as? Zip)

            packageJacocoResource?.let {
                assembleTask.finalizedBy(it)
            }

            assembleTask.doLast {
                variant.outputs.all { output ->
                    val outputFileName = output.outputFile.name
                    val archiveFileName = outputFileName.removeSuffix(".apk")
                    println("outputFile fileName: $archiveFileName")
                    packageJacocoResource?.archiveFileName?.set(archiveFileName.removeSuffix(".zip"))
                }
            }
        }
    }

    private fun includeProjects(jacocoExt: JacocoExtension, path: String): Boolean =
        jacocoExt.includeProjects?.any { includeName ->
            path == includeName ||
                    path.startsWith("$includeName:")
        } ?: true
}


