package dev.arbjerg.lavalink.gradle.tasks

import dev.arbjerg.lavalink.gradle.LavalinkGradlePlugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.environment

internal val Project.testServerFolder
    get() = project.layout.buildDirectory.dir("lavalink-test-server-plugins")

abstract class RunLavalinkTask : JavaExec() {
    init {
        mainClass = "org.springframework.boot.loader.JarLauncher"
        group = LavalinkGradlePlugin.TASK_GROUP_NAME
        outputs.upToDateWhen { false }
    }

    private val workingDir = project.rootDir
    private val testServerFolder = project.testServerFolder
    private val lavalinkJar = project.lavalinkJar.map { project.files(it) }

    @TaskAction
    override fun exec() {
        workingDir(workingDir)
        configureClassPath()
        environment("lavalink.pluginsDir" to testServerFolder.get())
        super.exec()
    }

    private fun configureClassPath() {
        classpath += objectFactory.fileCollection().from(lavalinkJar)
    }
}
