package dev.arbjerg.lavalink.gradle.tasks

import dev.arbjerg.lavalink.gradle.LavalinkGradlePlugin
import dev.arbjerg.lavalink.gradle.extension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.io.path.div

internal val Project.lavalinkJar: Provider<Path>
    get() = provider {
        project.gradle.gradleUserHomeDir.toPath() / "lavalink-versions" / extension.serverVersion.get() / "Lavalink.jar"
    }

abstract class DownloadLavalinkTask : DefaultTask() {
    @get:Internal
    internal abstract val dependencyProvider: Property<Dependency>

    @Suppress("unused") // only exists for input snapshotting
    @get:Input
    val version: Provider<String>
        get() = dependencyProvider.map { it.version!! }

    init {
        group = LavalinkGradlePlugin.TASK_GROUP_NAME
        outputs.dir(project.gradle.gradleUserHomeDir.toPath() / "lavalink-versions")
    }

    @TaskAction
    fun download() {
        val dependency = dependencyProvider.get()
        val configuration = project.configurations.detachedConfiguration(dependency)
            .markResolvable()

        val files = configuration.resolve()
        logger.debug("Resolved Lavalink dependencies to: {}", files)
        val archive = files.single {
            it.name.endsWith(".jar")
                    && "plain" !in it.name && "sources" !in it.name && "javadoc" !in it.name
        }
        logger.debug("Resolved lavalink binary to: {}", archive.name)
        val path = project.gradle.gradleUserHomeDir.toPath() / "lavalink-versions" / dependency.version!!

        didWork = project.copy {
            from(archive)
            rename { "Lavalink.jar" }
            into(path)
        }.didWork
    }
}

internal fun Configuration.markResolvable(): Configuration = apply {
    this.isCanBeConsumed = false
    this.isCanBeResolved = true
}

