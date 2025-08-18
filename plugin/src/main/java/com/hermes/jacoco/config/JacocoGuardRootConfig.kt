package com.hermes.jacoco.config

import com.android.build.gradle.BaseExtension
import com.hermes.jacoco.const.RELATIVE_LOCAL_DIR_PATH
import com.hermes.jacoco.guardian.JacocoExtension
import com.hermes.jacoco.guardian.enableAutoCoverage
import com.hermes.jacoco.guardian.enableFullCoverage
import com.hermes.jacoco.guardian.getFileFilter
import com.hermes.jacoco.guardian.getJacocoVersion
import com.hermes.jacoco.tasks.JacocoInitTask
import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * add jacoco init task
 * generate dir: code_coverage ...
 * manual and auto multi module to config
 * just to used as a standalone task
 */
fun Project.addJacocoInitTask(jacocoExt: JacocoExtension) {
    println("[jacocoInitTask] enableFullCoverage: ${enableFullCoverage(jacocoExt)}")
    if (!enableFullCoverage(jacocoExt)) {
        return
    }

    tasks.register("jacocoInit", JacocoInitTask::class.java) {
        it.group = "Reporting"
        it.description = "Initialize Jacoco report directories"
        it.outputDir.set(layout.buildDirectory.dir(RELATIVE_LOCAL_DIR_PATH))
    }
}

/**
 * add root jacoco task
 * for: devices unit test: *.ec to code_coverage
 */
fun Project.addRootJacocoConfigTask(jacocoExt: JacocoExtension) {
    if (!enableFullCoverage(jacocoExt)) {
        return
    }

    val enableAutoCoverage = enableAutoCoverage(jacocoExt)
    val fileFilter = getFileFilter(jacocoExt)
    println("[jacocoRootConfigTask] enableAutoCoverage: $enableAutoCoverage")

    configJacocoAnt(jacocoExt)

    jacocoRootReportTaskProvider(enableAutoCoverage, fileFilter)
}

/**
 * config jacoco ant
 */
private fun Project.configJacocoAnt(jacocoExt: JacocoExtension) {
    configurations.run {
        maybeCreate("jacocoAnt")
    }

    dependencies.run {
        add("jacocoAnt", "org.jacoco:org.jacoco.ant:${getJacocoVersion(jacocoExt)}")
    }
}


/**
 * root jacoco report provider task
 * to:: generate whole module jacoco report
 * from: 1/sub module ; 2/devices sub module
 *
 * local unit test: depends on
 * devices unit test: *.ec to code_coverage??
 *
 * root project
 */
private fun Project.jacocoRootReportTaskProvider(
    enableAutoCoverage: Boolean,
    fileFilter: List<String>,
) {
    tasks.register("jacocoRootReport", JacocoReport::class.java) {
        it.group = "Reporting"
        it.description = "Generate Jacoco Root coverage reports"
        dependsReportTask(it, enableAutoCoverage)

        it.jacocoClasspath = configurations.getByName("jacocoAnt")

        it.reports.run {
            xml.required.set(true)
            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
            xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoFullReport.xml"))
        }

        val sources = subprojects.map { project ->
            project.extensions.findByType(BaseExtension::class.java)
                ?.sourceSets?.findByName("main")?.java?.srcDirs
        }

        val classes = subprojects.map { project ->
            val javaClassesDir =
                project.layout.buildDirectory.dir("intermediates/javac").get().asFile
            val kotlinClassesDir =
                project.layout.buildDirectory.dir("tmp/kotlin-classes").get().asFile
            // 单独的 java library 可以在设备自动化能力场景中支持
            val javaLibClassesDir = project.layout.buildDirectory.dir("classes").get().asFile

            files(
                fileTree(
                    mapOf(
                        "dir" to javaClassesDir,
                        "exclude" to fileFilter
                    )
                ),
                fileTree(
                    mapOf(
                        "dir" to kotlinClassesDir,
                        "exclude" to fileFilter
                    )
                ),
                fileTree(
                    mapOf(
                        "dir" to javaLibClassesDir,
                        "exclude" to fileFilter
                    )
                )
            )
        }

        it.sourceDirectories.setFrom(files(sources))
        it.classDirectories.setFrom(files(classes))

        // 遍历所有 build文件夹下的jacoco产物;; 需要处理 手动还是自动收集
        it.executionData.from(
            fileTree("$rootDir").include(
                "**/outputs/unit_test_code_coverage/*UnitTest/*.exec",
                "**/outputs/code_coverage/**/*.ec"
            )
        )
    }
}

private fun Project.dependsReportTask(it: JacocoReport, enableAutoCoverage: Boolean) {
    if (enableAutoCoverage) {
        return
    }
    it.dependsOn(
        subprojects.mapNotNull { project ->
            project.tasks.findByName("jacocoTestReport")
        }
    )
}

private fun Project.dependsJacocoInitTask(it: JacocoReport) {
    tasks.findByName("jacocoInit")?.also { initTask ->
        it.dependsOn(initTask)
    }
}
