package dev.arbjerg.lavalink.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible

/**
 * Creates a Lavalink dependency with name [module].
 */
fun Project.lavalink(module: String, group: String = "dev.arbjerg.lavalink"): Provider<Dependency> = extension.apiVersion.map { version ->
    dependencies.create("$group:$module:$version")
}

/**
 * Specifies the correct Lavalink version for [dependency].
 */
fun Project.lavalink(dependency: MinimalExternalModuleDependency): Provider<Dependency> {
    val module = dependency.module
    return lavalink(module.group, module.name)
}

/**
 * Specifies the correct Lavalink version for [dependency].
 */
fun Project.lavalink(dependency: Provider<MinimalExternalModuleDependency>) =
    dependency.flatMap { lavalink(it) }

/**
 * Specifies the correct Lavalink version for [dependency].
 */
fun Project.lavalink(dependency: ProviderConvertible<MinimalExternalModuleDependency>) =
    lavalink(dependency.asProvider())
