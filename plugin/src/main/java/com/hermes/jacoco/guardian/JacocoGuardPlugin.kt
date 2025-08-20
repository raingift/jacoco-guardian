package com.hermes.jacoco.guardian

import com.hermes.jacoco.config.addJacocoInitTask
import com.hermes.jacoco.config.addJacocoResourcePackage
import com.hermes.jacoco.config.addModuleJacocoPlugin
import com.hermes.jacoco.config.addTestJacocoClassPaths
import com.hermes.jacoco.config.addRootJacocoConfigTask
import com.hermes.jacoco.config.defaultGenerateLocalDir
import org.gradle.api.Plugin
import org.gradle.api.Project

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
        val isAndroidModule = project.plugins.hasPlugin("com.android.application")
                || project.plugins.hasPlugin("com.android.library")
        if (isAndroidModule) {
            val isIncluded = includeProjects(jacocoExt, project.path)
            if (isIncluded) {
                project.addModuleJacocoPlugin(jacocoExt)
                project.addTestJacocoClassPaths(jacocoExt)
            }
        }
    }

    private fun includeProjects(jacocoExt: JacocoExtension, path: String): Boolean =
        jacocoExt.includeProjects?.any { includeName ->
            path == includeName ||
                    path.startsWith("$includeName:")
        } ?: true
}


