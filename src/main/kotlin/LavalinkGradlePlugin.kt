package dev.arbjerg.lavalink.gradle

import dev.arbjerg.lavalink.gradle.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

private const val lavalinkExtensionName = "lavalinkPlugin"

internal val Project.extension
    get() = extensions.getByName<LavalinkExtension>(lavalinkExtensionName)

class LavalinkGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            check(plugins.hasPlugin("org.gradle.java")) { "Please apply the Java/Kotlin plugin before Lavalink" }
            configureExtension()
            configurePublishing()
            val serverDependency = configureDependencies()
            configureTasks(serverDependency)
            configureSourceSets()
        }
    }

    companion object {
        const val TASK_GROUP_NAME = "lavalink"
    }
}

private fun Project.configureExtension(): LavalinkExtension {
    return extensions.create<LavalinkExtension>(lavalinkExtensionName).apply {
        version.convention(provider { project.version.toString() })
        name.convention(project.name)
        path.convention(provider { project.group.toString() })
        serverVersion.convention(apiVersion)
        configurePublishing.convention(true)
    }
}

private fun Project.configureDependencies(): Provider<Dependency> {
    project.repositories {
        mavenCentral()
        maven("https://jitpack.io")
        // Required for runtime
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")
        // Required for Lavalink Dependencies
        @Suppress("DEPRECATION")
        jcenter()
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

private fun Project.configureTasks(serverDependency: Provider<Dependency>) {
    tasks {
        val generatePluginProperties by registering(GeneratePluginPropertiesTask::class)
        named("processResources") {
            dependsOn(generatePluginProperties)
        }

        val jar = named<Jar>("jar") {
            configurations.getByName("runtimeClasspath")
                .fileCollection {
                    it !is ProjectDependency
                }
                .forEach {
                    from(zipTree(it)) {
                        exclude("META-INF/**")
                    }
                }

            configurations.getByName("runtimeClasspath")
                .allDependencies
                .filterIsInstance<ProjectDependency>()
                .forEach { dependency ->
                    val project = dependency.dependencyProject
                    if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                        dependsOn(project.tasks.named("jvmMainClasses"))
                        from(project.layout.buildDirectory.file("classes/kotlin/jvm/main")) {
                            include("**/*.class")
                        }
                    } else {
                        dependsOn(project.tasks.named("classes"))
                        from(project.layout.buildDirectory.dir("classes")) {
                            include("**/main/**/*.class")
                            eachFile {
                                path = path.substringAfter("main/")
                            }
                        }
                    }
                }
        }

        val installPlugin by registering(Copy::class) {
            from(jar)
            into(project.testServerFolder)
            // This always deletes old versions of the plugin in the test server
            // So we don't install the same plugin twice
            rename { "plugin.jar" }
        }

        val downloadLavalink by registering(DownloadLavalinkTask::class) {
            dependencyProvider = serverDependency
        }

        register<RunLavalinkTask>("runLavaLink") {
            dependsOn(installPlugin, downloadLavalink)
        }
    }
}
