package dev.arbjerg.lavalink.gradle.tasks

import dev.arbjerg.lavalink.gradle.extension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.div

internal val Project.lavalinkJar: Provider<Path>
    get() = provider {
        project.gradle.gradleUserHomeDir.toPath() / "lavalink-versions" / extension.serverVersion.get() / "Lavalink.jar"
    }

abstract class DownloadLavalinkTask @Inject constructor(private val dependencyProvider: Provider<Dependency>) :
    DefaultTask() {

    init {
        inputs.property("dependency", dependencyProvider.map { it.version.toString() })
        outputs.dir(project.gradle.gradleUserHomeDir.toPath() / "lavalink-versions")
    }

    @TaskAction
    fun download() {
        val dependency = dependencyProvider.get()
        val configuration = project.configurations.detachedConfiguration(dependency)
            .markResolvable()

        val archive = configuration.resolve().single {
            it.name.endsWith(".jar")
                    && "plain" !in it.name && "sources" !in it.name && "javadoc" !in it.name
        }
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

