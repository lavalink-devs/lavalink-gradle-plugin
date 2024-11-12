package dev.arbjerg.lavalink.gradle.tasks

import dev.arbjerg.lavalink.gradle.LavalinkGradlePlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.environment

abstract class RunLavalinkTask : JavaExec() {
    init {
        mainClass = "org.springframework.boot.loader.JarLauncher"
        group = LavalinkGradlePlugin.TASK_GROUP_NAME
        outputs.upToDateWhen { false }
    }

    private val workingDir = project.layout.projectDirectory
    private val lavalinkJar = project.lavalinkJar.map { project.files(it) }

    @TaskAction
    override fun exec() {
        workingDir(workingDir)
        configureClassPath()
        environment("lavalink.plugins.developmentMode" to true)
        super.exec()
    }

    private fun configureClassPath() {
        classpath += objectFactory.fileCollection().from(lavalinkJar)
    }
}
