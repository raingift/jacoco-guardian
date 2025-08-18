package com.hermes.jacoco.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class JacocoInitTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        if (!outputDir.get().asFile.exists()) {
            outputDir.get().asFile.mkdirs()
        }
        logger.lifecycle("Task completed! Output directory: ${outputDir.get()}")
    }
}