package ru.ttraum.kontroller.specs

import arrow.core.compose
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import io.ktor.http.*
import io.ktor.server.routing.*
import ru.ttraum.kontroller.constant.Constants
import ru.ttraum.kontroller.constant.MemberNames
import ru.ttraum.kontroller.core.http.BodyParam
import ru.ttraum.kontroller.core.http.HttpHeader
import ru.ttraum.kontroller.core.http.PathParam
import ru.ttraum.kontroller.mapper.createTypePredicate
import ru.ttraum.kontroller.model.AnnotationModel
import ru.ttraum.kontroller.model.ParameterModel
import ru.ttraum.kontroller.model.RouteModel
import ru.ttraum.kontroller.model.RouterModel
import ru.ttraum.kontroller.utils.useControlFlow
import java.util.*

private val headerAnnotationPredicate = createTypePredicate(listOf(HttpHeader::class.qualifiedName))
private val pathParamAnnotationPredicate = createTypePredicate(listOf(PathParam::class.qualifiedName))
private val bodyParamAnnotationPredicate = createTypePredicate(listOf(BodyParam::class.qualifiedName))

fun createRouteSpec(router: RouterModel, route: RouteModel): FunSpec =
    FunSpec.builder(route.name + route.method)
        .receiver(Route::class)
        .addCode(buildRouteCodeBlock(router, route))
        .build()

private fun buildRouteCodeBlock(router: RouterModel, route: RouteModel): CodeBlock =
    CodeBlock.builder()
        .apply {
            defineMethod(router, route) {
                setupHeaders(route.annotations.filter(headerAnnotationPredicate compose AnnotationModel::type))
                setupPathParams(route.parameters.filter(hasPathParamAnnotation))
                setupBodyParam(route.parameters.filter(hasBodyParamAnnotation))
                handleRequest(router, route)
            }
        }
        .build()

private val hasPathParamAnnotation: (ParameterModel) -> Boolean =
    { param -> param.annotations.any(pathParamAnnotationPredicate compose AnnotationModel::type) }

private val hasBodyParamAnnotation: (ParameterModel) -> Boolean =
    { param -> param.annotations.any(bodyParamAnnotationPredicate compose AnnotationModel::type) }

private fun CodeBlock.Builder.setupHeaders(headers: List<AnnotationModel>) =
    headers.forEach { header ->
        val name by header.fields
        val value by header.fields
        addStatement("call.response.%M(%S, %S)", MemberNames.ktorHeader, name, value)
    }

private fun CodeBlock.Builder.setupPathParams(pathParams: List<ParameterModel>) =
    pathParams.forEach { param ->
        val extractParam = "requireNotNull(call.parameters[\"${param.name}\"])"
        val statement = "val ${param.name} ="
        when (param.type.qualifiedName) {
            Int::class.qualifiedName -> addStatement("$statement.toInt()")
            Long::class.qualifiedName -> addStatement("$statement.toLong()")
            Float::class.qualifiedName -> addStatement("$statement.toFloat()")
            Double::class.qualifiedName -> addStatement("$statement.toDouble()")
            Boolean::class.qualifiedName -> addStatement("$statement.toBoolean()")
            UUID::class.qualifiedName -> addStatement("$statement %T.fromString($extractParam)", UUID::class)
            else -> addStatement(statement)
        }
    }

private fun CodeBlock.Builder.setupBodyParam(bodyParams: List<ParameterModel>) =
    bodyParams.singleOrNull()?.let { param ->
        addStatement(
            "val ${param.name} = call.%M<${param.type.getFullSignature()}>()",
            MemberNames.ktorReceive
        )
    }

private fun CodeBlock.Builder.handleRequest(router: RouterModel, route: RouteModel) {
    val parameterCall = buildParameterCall(route.parameters)
    addStatement("val result = this@${router.name}.controller.${route.name}($parameterCall)")
    addStatement("call.%M(result)", MemberNames.ktorRespond)
}

private fun buildParameterCall(parameters: List<ParameterModel>): String =
    parameters.joinToString(", ") { "${it.name} = ${it.name}" }

private fun CodeBlock.Builder.defineMethod(
    router: RouterModel,
    route: RouteModel,
    body: CodeBlock.Builder.() -> Unit
) {
    val normalizedPath = normalizePath(router.path + "/" + route.path)
    when {
        route.method in Constants.HttpMethods -> handleStandardHttpMethod(normalizedPath, route, body)
        else -> handleCustomHttpMethod(normalizedPath, route, body)
    }
}

private fun CodeBlock.Builder.handleCustomHttpMethod(
    path: String,
    route: RouteModel,
    body: CodeBlock.Builder.() -> Unit
) = useControlFlow(
    "this.%M(%S, %T.parse(%S))",
    MemberNames.ktorRoute,
    path,
    HttpMethod::class,
    route.method
) {
    useControlFlow("handle") { body() }
}

private fun CodeBlock.Builder.handleStandardHttpMethod(
    path: String,
    route: RouteModel,
    body: CodeBlock.Builder.() -> Unit
) = Constants.httpMethodToMemberName[route.method]?.let { methodName ->
    useControlFlow("this.%M(%S)", methodName, path) { body() }
}

private fun normalizePath(path: String): String =
    path.replace("/{2,}".toRegex(), "/").removeSuffix("/").removePrefix("/")