package com.hermes.jacoco.config

import com.android.build.gradle.BaseExtension
import com.hermes.jacoco.guardian.JacocoExtension
import com.hermes.jacoco.guardian.getFileFilter
import com.hermes.jacoco.guardian.getJacocoVersion
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

/**
 * Dynamic config jacoco task
 */
private fun Project.jacocoDynamicConfigTask() {
    tasks.withType(Test::class.java).configureEach { testTask ->
        testTask.useJUnitPlatform()
        val jacocoTaskExt = testTask.extensions.findByType(JacocoTaskExtension::class.java)
            ?: testTask.extensions.create("jacoco", JacocoTaskExtension::class.java)
        configureTaskJacoco(jacocoTaskExt)
    }
}

private fun configureTaskJacoco(extension: JacocoTaskExtension) {
    extension.apply {
        isIncludeNoLocationClasses = true
        excludes = listOf(
            "jdk.internal.*",
            "**/*\$Parcelable*",
            "**/*\$Companion*",
            "**/ComposableSingletons*",
            "**/*\$lambda-*"
        )
    }
}

/**
 * add module Jacoco Plugin and generate jacoco test report
 * for scene:
 * 1、testUnit for module
 * 2、auto test coverage package for module
 * 3、flavor info for module
 */
fun Project.addModuleJacocoPlugin(jacocoExt: JacocoExtension) {
    plugins.apply(JacocoPlugin::class.java)
    extensions.findByType(BaseExtension::class.java)?.apply {
        // dynamic config
        jacoco.jacocoVersion = getJacocoVersion(jacocoExt)

        buildTypes.configureEach { buildType ->
            // dynamic config
            buildType.isTestCoverageEnabled = when {
                jacocoExt.buildFlavorName.isNullOrEmpty() -> buildType.name == "debug"
                else -> buildType.name in jacocoExt.buildFlavorName!!
            }
        }
    }
    jacocoReportTaskProvider()
    jacocoDynamicConfigTask()
}

/**
 * Jacoco Report Provider Task
 * to: generate sub module jacoco report
 */
private fun Project.jacocoReportTaskProvider() {
    val (sources, classes) = configurableFilesPair()

    println("[jacocoReportTaskProvider] sources: $sources")

    tasks.register("jacocoTestReport", JacocoReport::class.java) {
        it.group = "Reporting"
        it.description = "Generate Jacoco coverage reports"
        dependsUnitTest(it)

        it.reports.xml.required.set(true)
        it.reports.html.required.set(true)

        println("[jacocoReportTaskProvider] buildDir: $buildDir")

        it.sourceDirectories.setFrom(files(sources))
        it.classDirectories.setFrom(files(classes))
        it.executionData.setFrom(
            fileTree("$buildDir").include(
                "outputs/unit_test_code_coverage/*UnitTest/*.exec",
                "outputs/code_coverage/**/*.ec"
            )
        )
    }
}

/**
 * For: Jacoco Init Params
 * configuration sources and classes files path
 */
private fun Project.configurableFilesPair(): Pair<Set<File>?, ConfigurableFileCollection> {
    val jacocoExt = rootProject.extensions.findByType(JacocoExtension::class.java)
    val extension = extensions.findByType(BaseExtension::class.java)
    val sources = extension?.sourceSets?.findByName("main")?.java?.srcDirs

    val javaClassesDir = layout.buildDirectory.dir("intermediates/javac").get().asFile
    val kotlinClassesDir = layout.buildDirectory.dir("tmp/kotlin-classes").get().asFile
    val classes = files(
        fileTree(
            mapOf(
                "dir" to javaClassesDir,
                "exclude" to getFileFilter(jacocoExt)
            )
        ),
        fileTree(
            mapOf(
                "dir" to kotlinClassesDir,
                "exclude" to getFileFilter(jacocoExt)
            )
        )
    )
    return Pair(sources, classes)
}

/**
 * Unit test task
 */
private fun Project.dependsUnitTest(it: JacocoReport) {
    tasks.findByName("testDebugUnitTest")?.also { unitTestTask ->
        it.dependsOn(unitTestTask)
    } ?: {
        logger.warn("Task ${path}:testDebugUnitTest not found. Skipping dependency.")
    }
}

