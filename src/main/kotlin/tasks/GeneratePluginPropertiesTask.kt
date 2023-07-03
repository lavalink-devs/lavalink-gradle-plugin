package dev.arbjerg.lavalink.gradle.tasks

import dev.arbjerg.lavalink.gradle.extension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.properties
import java.nio.file.Path
import java.util.*
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div

internal val Project.generatedPluginManifest: Provider<Path>
    get() = extension.name.map { name ->
        project.buildDir.toPath() / "generated" / "lavalink" / "main" / "resources"
    }

abstract class GeneratePluginPropertiesTask : DefaultTask() {

    init {
        val extension = project.extension
        inputs.properties(
            "version" to extension.version,
            "name" to extension.name,
            "path" to extension.path,
        )

        outputs.dir(project.generatedPluginManifest)
    }

    @TaskAction
    fun generateTask() {
        val extension = project.extension
        val properties = Properties().apply {
            set("version", extension.version.get())
            set("name", extension.name.get())
            set("path", extension.path.get())
        }

        val file = project.generatedPluginManifest.get() / "lavalink-plugins" / "${extension.name.get()}.properties"
        file.bufferedWriter().use { writer ->
            properties.store(writer, null)
        }
    }
}
