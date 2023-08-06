package dev.arbjerg.lavalink.gradle

import org.gradle.api.provider.Property
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

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
    val path: Property<String>

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
