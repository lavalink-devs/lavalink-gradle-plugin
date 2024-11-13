package dev.arbjerg.lavalink.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class ExtensionProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val metaAnnotations = mutableListOf<String>()
    private val extensions = mutableListOf<String>()

    companion object Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = ExtensionProcessor(environment)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        metaAnnotations += resolver.getSymbolsWithAnnotation(PF4J_EXTENSION)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ANNOTATION_CLASS }
            .onEach { environment.logger.logging("Found meta annotation: ${it.qualifiedName?.asString()}") }
            .map { it.qualifiedName.toString() }

        return resolver.getNewFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                if (it.qualifiedName != null && it.annotations.any { annotation -> annotation.shortName.asString() in metaAnnotations }) {
                    extensions.add(it.qualifiedName!!.toString())
                    environment.logger.logging("Found extension: ${it.qualifiedName!!.asString()}")
                    false
                } else {
                    true
                }
            }
            .toList()
    }
}