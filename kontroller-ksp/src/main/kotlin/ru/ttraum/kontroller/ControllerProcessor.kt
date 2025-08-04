package ru.ttraum.kontroller

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import ru.ttraum.kontroller.core.Error
import ru.ttraum.kontroller.core.Level
import ru.ttraum.kontroller.core.http.Controller
import ru.ttraum.kontroller.mapper.controllerModelFromKSClass
import ru.ttraum.kontroller.specs.generateRouterFile

class ControllerProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {

        resolver
            .getSymbolsWithAnnotation(Controller::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull {
                controllerModelFromKSClass(it)
                    .onLeft(errorLogger)
                    .getOrNull()
            }.map { router ->
                generateRouterFile(router)
            }.forEach { spec ->
                writeFileSpecs(spec, environment, resolver)
            }

        return emptyList()
    }

    private fun writeFileSpecs(spec: FileSpec, environment: SymbolProcessorEnvironment, resolver: Resolver) {
        val dependencies = Dependencies(true, *resolver.getAllFiles().toList().toTypedArray())

        environment.codeGenerator.createNewFile(dependencies, spec.packageName, spec.name)
            .use { it.write(spec.toString().toByteArray()) }
    }

    private val errorLogger: (Error) -> Unit = {
        when (it.level()) {
            is Level.Warning -> environment.logger.warn(it.error())
        }
    }
}
