package ru.ttraum.kontroller.specs

import com.squareup.kotlinpoet.*
import ru.ttraum.kontroller.constant.MemberNames
import ru.ttraum.kontroller.constant.PackageNames
import ru.ttraum.kontroller.model.RouterModel

private val experimentalKtorApi = ClassName(PackageNames.KTOR_UTILS_IO, "ExperimentalKtorApi")

fun generateRouterFile(router: RouterModel): FileSpec {
    val routerSpec = createRouterSpec(router) {
        val routes = router.handlers.map { route ->
            createRouteSpec(router, route)
        }
        addFunctions(routes)
    }
    val funSpec = createClosureRoutesFunction(router)

    return createFileSpec(router, routerSpec) {
        addType(routerSpec)
        addFunction(funSpec)
        addDefaultImports(MemberNames.ktorGetValueOrNull)
        optIn(experimentalKtorApi)
    }
}

private fun createFileSpec(
    routerModel: RouterModel,
    spec: TypeSpec,
    builder: FileSpec.Builder.() -> Unit
): FileSpec =
    FileSpec.builder(routerModel.controller.packageName, spec.name.toString())
        .also(builder)
        .suppressWarnings("REDUNDANT_VISIBILITY_MODIFIER", "RemoveRedundantBackticks")
        .build()

private fun FileSpec.Builder.suppressWarnings(vararg types: String): FileSpec.Builder = apply {
    if (types.isNotEmpty()) {
        val format = types.joinToString(", ") { "%S" }
        addAnnotation(
            AnnotationSpec.builder(Suppress::class)
                .addMember(format, *types)
                .build()
        )
    }
}

private fun FileSpec.Builder.addDefaultImports(vararg members: MemberName): FileSpec.Builder = apply {
    members.forEach {
        this.addImport(it.packageName, it.simpleName)
    }
}

private fun FileSpec.Builder.optIn(vararg classNames: ClassName): FileSpec.Builder = apply {
    if (classNames.isNotEmpty()) {
        val format = classNames.joinToString(", ") { "%T::class" }
        addAnnotation(
            AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                .addMember(format, *classNames)
                .build()
        )
    }
}