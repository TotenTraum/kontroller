package ru.ttraum.kontroller.specs

import com.squareup.kotlinpoet.*
import io.ktor.server.routing.*
import ru.ttraum.kontroller.model.RouterModel
import ru.ttraum.kontroller.utils.useControlFlow

fun createRouterSpec(routerModel: RouterModel, applier: TypeSpec.Builder.() -> Unit): TypeSpec =
    TypeSpec.classBuilder(routerModel.name)
        .primaryConstructor(createConstructorSpec(routerModel))
        .addProperty(createControllerProperty(routerModel))
        .apply(applier)
        .build()

private fun createConstructorSpec(routerModel: RouterModel): FunSpec {
    val className = ClassName(
        routerModel.controller.packageName,
        routerModel.controller.className
    )
    return FunSpec.constructorBuilder()
        .addParameter("controller", className)
        .build()
}

private fun createControllerProperty(routerModel: RouterModel): PropertySpec {
    val className = ClassName(
        routerModel.controller.packageName,
        routerModel.controller.className
    )
    return PropertySpec.builder("controller", className)
        .initializer("controller")
        .addModifiers(KModifier.PRIVATE)
        .build()
}

fun createClosureRoutesFunction(routerModel: RouterModel): FunSpec {
    return FunSpec.builder("routes")
        .addParameter(
            "router",
            ClassName(routerModel.controller.packageName, routerModel.name)
        )
        .receiver(Route::class)
        .apply {
            addCode(
                CodeBlock.builder()
                    .useControlFlow("with(router)") {
                        routerModel.handlers.forEach { handler ->
                            addStatement("${handler.name + handler.method}()")
                        }
                    }
                    .build()
            )
        }
        .build()
}