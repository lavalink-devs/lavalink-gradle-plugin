package dev.arbjerg.lavalink.gradle.tasks

import dev.arbjerg.lavalink.gradle.LavalinkGradlePlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.div

internal val Project.testServerFolder
    get() = project.buildDir.toPath() / "lavalink-test-server-plugins"

abstract class RunLavalinkTask : JavaExec() {
    init {
        mainClass = "org.springframework.boot.loader.JarLauncher"
        group = LavalinkGradlePlugin.TASK_GROUP_NAME
        outputs.upToDateWhen { false }
    }

    @TaskAction
    override fun exec() {
        workingDir(project.rootDir)
        configureClassPath()
        environment("lavalink.pluginsDir" to project.testServerFolder)
        super.exec()
    }

    private fun configureClassPath() {
        classpath += objectFactory.fileCollection().from(project.lavalinkJar.map { project.files(it) })
    }
}
