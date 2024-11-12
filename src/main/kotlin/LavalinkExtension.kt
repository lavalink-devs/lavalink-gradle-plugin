package dev.arbjerg.lavalink.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible

/**
 * Lavalink specific configuration options.
 */
interface LavalinkExtension {
    /**
     * The version of the API.
     *
     * [Check here for versions](https://github.com/lavalink-devs/Lavalink)
     */
    val apiVersion: Property<String>

    /**
     * The version of the server (Can be checked [here](https://maven.arbjerg.dev/)).
     *
     * @see gitHash
     *
     * defaults to [apiVersion].
     */
    val serverVersion: Property<String>

    /**
     * The plugin version (if different to [Project.getVersion])
     */
    val version: Property<String>

    /**
     * The plugin name (if different to [Project.getName])
     */
    val name: Property<String>

    /**
     * The plugins root package (if different to [Project.getGroup]).
     */
    @Deprecated("This property is no longer required")
    val path: Property<String>

    /**
     * The version of Lavalink this plugin requires (if different to [apiVersion]).
     */
    val requires: Property<String>

    /**
     * An optional description of the plugin.
     */
    val description: Property<String>

    /**
     * An optional mention of the plugin's author.
     */
    val provider: Property<String>

    /**
     * An optional license of the plugin.
     */
    val license: Property<String>

    /**
     * Whether to configure publishing automatically or nor.
     */
    val configurePublishing: Property<Boolean>

    /**
     * Creates a Lavalink version for [gitHash].
     */
    fun gitHash(gitHash: String) = "$gitHash-SNAPSHOT"

    /**
     * Creates a Lavalink version for [gitHash].
     */
    fun gitHash(gitHash: Provider<String>) = gitHash.map { gitHash(it) }

    /**
     * Creates a Lavalink version for [gitHash].
     */
    fun gitHash(gitHash: ProviderConvertible<String>) = gitHash(gitHash.asProvider())
}
