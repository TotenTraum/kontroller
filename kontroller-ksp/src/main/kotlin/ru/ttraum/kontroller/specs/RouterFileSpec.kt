package ru.ttraum.kontroller.specs

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import ru.ttraum.kontroller.constant.MemberNames
import ru.ttraum.kontroller.model.RouterModel

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