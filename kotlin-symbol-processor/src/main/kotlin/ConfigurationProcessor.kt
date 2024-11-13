package dev.arbjerg.lavalink.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class ConfigurationProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val configurationClasses = mutableListOf<String>()
    override fun process(resolver: Resolver): List<KSAnnotated> {
        configurationClasses += resolver.getSymbolsWithAnnotation(SPRING_CONFIGURATION)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { it.qualifiedName?.toString() }
            .onEach {
                environment.logger.logging("Found configuration class: $it")
            }

        return emptyList()
    }

    override fun finish() {
        environment.logger.logging("Received finish signal, writing ${configurationClasses.size} to fs")
        environment.codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "META-INF",
            fileName = "configuration-classes",
            extensionName = "idx"
        ).bufferedWriter().use { writer ->
            configurationClasses.forEach {
                writer.write(it)
                writer.newLine()
            }
        }
    }

    companion object Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
            ConfigurationProcessor(environment)
    }
}
