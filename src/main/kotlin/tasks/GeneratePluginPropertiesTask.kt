package dev.arbjerg.lavalink.gradle.tasks

import dev.arbjerg.lavalink.gradle.extension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.properties
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.div

internal val Project.generatedPluginManifest: Provider<Directory>
    get() = project.layout.buildDirectory.dir("generated/lavalink/main/resources")

abstract class GeneratePluginPropertiesTask : DefaultTask() {

    init {
        group = LifecycleBasePlugin.BUILD_GROUP
        val extension = project.extension
        @Suppress("DEPRECATION")
        inputs.properties(
            "version" to extension.version,
            "name" to extension.name,
            "path" to extension.path,
            "requires" to extension.requires,
            "provider" to extension.provider.orElse(""),
            "license" to extension.license.orElse(""),
        )

        outputs.dir(project.generatedPluginManifest)
    }

    private val extension = project.extension
    private val generatedPluginManifest = project.generatedPluginManifest
    private val dependencies = project.buildDependenciesString()

    @TaskAction
    fun generateTask() {
        val properties = Properties().apply {
            set("plugin.id", extension.name.get())
            set("plugin.version", extension.version.get())
            set("plugin.requires", extension.requires.get())
            if (dependencies.isNotBlank()) {
                set("plugin.dependencies", dependencies)
            }
            setIfPresent("plugin.provider", extension.provider)
            setIfPresent("plugin.license", extension.license)
        }

        val file = generatedPluginManifest.get().asFile.toPath() / "plugin.properties"
        file.parent.createDirectories()
        file.bufferedWriter(options = arrayOf(StandardOpenOption.CREATE)).use { writer ->
            properties.store(writer, null)
        }
    }

}

private fun Project.buildDependenciesString(): String {
    val plugin = configurations.getByName("plugin")
    val optionalPlugin = configurations.getByName("optionalPlugin")

    val required = plugin.allDependencies.map(Dependency::toDependencyString)
    val optional = optionalPlugin.allDependencies.map { it.toDependencyString(true) }

    return (required + optional).joinToString(", ")
}

private fun Dependency.toDependencyString(optional: Boolean = false): String {
    return "$name${if (optional) "?" else ""}@$version"
}

private fun Properties.setIfPresent(name: String, value: Provider<String>) {
    if (value.isPresent) {
        setProperty(name, value.get())
    }
}
