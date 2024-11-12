package dev.arbjerg.lavalink.gradle

import dev.arbjerg.lavalink.gradle.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

private const val lavalinkExtensionName = "lavalinkPlugin"

internal val Project.extension
    get() = extensions.getByName<LavalinkExtension>(lavalinkExtensionName)

class LavalinkGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            check(plugins.hasPlugin("org.gradle.java")) { "Please apply the Java/Kotlin plugin before Lavalink" }
            val extension = configureExtension()
            configurePublishing()
            val serverDependency = configureDependencies()
            configureTasks(extension, serverDependency)
            configureSourceSets()
        }
    }

    companion object {
        const val TASK_GROUP_NAME = "lavalink"
    }
}

private fun Project.configureExtension(): LavalinkExtension {
    @Suppress("DEPRECATION")
    return extensions.create<LavalinkExtension>(lavalinkExtensionName).apply {
        version.convention(provider { project.version.toString() })
        name.convention(project.name)
        path.convention(provider { project.group.toString() })
        serverVersion.convention(apiVersion)
        configurePublishing.convention(true)
        requires.convention(serverVersion)
    }
}

private fun Project.configureDependencies(): Provider<Dependency> {
    project.repositories {
        mavenCentral()
        maven("https://jitpack.io")
        // Required for runtime
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")

    }

    dependencies {
        add("compileOnly", lavalink("plugin-api"))
    }

    return extension.serverVersion.map { serverVersion ->
        project.dependencies.create("dev.arbjerg.lavalink:Lavalink-Server:$serverVersion@jar") {
            // we only care about the full executable jar here, so no dependencies required
            isTransitive = false
        }
    }
}

private fun Project.configurePublishing() {
    afterEvaluate {
        if (extension.configurePublishing.get()) {
            apply<MavenPublishPlugin>()
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                        artifact(tasks.named("assemblePlugin"))
                    }
                }
            }
        }
    }
}

private fun Project.configureSourceSets() {
    configure<SourceSetContainer> {
        named("main") {
            resources {
                srcDir(project.generatedPluginManifest)
            }
        }
    }
}

private fun Project.configureTasks(extension: LavalinkExtension, serverDependency: Provider<Dependency>) {
    tasks {
        val generatePluginProperties by registering(GeneratePluginPropertiesTask::class)
        named("processResources") {
            dependsOn(generatePluginProperties)
        }

        val jar by getting(Jar::class)
        val collectPluginDependencies by registering(Copy::class) {
            val destinationDirectory = layout.buildDirectory.dir("dependencies")
            delete(destinationDirectory) // Delete old data

            from({
                val dependency =
                    dependencies.create("dev.arbjerg.lavalink:Lavalink-Server:${extension.serverVersion.get()}") {
                        // Old sedmelluq artifacts are still referenced at some places
                        // but do not resolve anymore since jcenter is dead
                        exclude(group = "com.sedmelluq")
                    }

                // Collect all dependencies lavalink depends on
                val serverDependencies = configurations
                    .detachedConfiguration(dependency)
                    .resolvedConfiguration
                    .resolvedArtifacts
                    .map { it.moduleVersion.id.dependencyNotation }

                // Remove them from the jar, to avoid conflicts
                configurations.getByName("runtimeClasspath")
                    .resolvedConfiguration
                    .resolvedArtifacts
                    .asSequence()
                    .filter { it.moduleVersion.id.dependencyNotation !in serverDependencies }
                    .mapNotNull { it.file }
                    .toList()

            })
            into(destinationDirectory)
        }

        register<Zip>("assemblePlugin") {
            group = LifecycleBasePlugin.BUILD_GROUP
            destinationDirectory = layout.buildDirectory.dir("distributions")
            archiveBaseName = extension.name.map { "plugin-$it" }
            archiveVersion = extension.version

            dependsOn(jar)

            into("classes") {
                with(jar)
                exclude("plugin.properties")
                // Do not include legacy manifest
                exclude("lavalink-plugins/**")
            }

            into("lib") {
                from(collectPluginDependencies)
            }

            from(generatePluginProperties)
        }

        val downloadLavalink by registering(DownloadLavalinkTask::class) {
            dependencyProvider = serverDependency
        }

        val classes by existing
        val processResources by existing

        register<RunLavalinkTask>("runLavaLink") {
            dependsOn(downloadLavalink, classes, processResources)
        }
    }
}

val ModuleVersionIdentifier.dependencyNotation: String
    get() = "$group:$name"
