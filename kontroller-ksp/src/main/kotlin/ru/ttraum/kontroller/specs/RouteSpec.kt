package ru.ttraum.kontroller.specs

import arrow.core.compose
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import io.ktor.http.*
import io.ktor.server.routing.*
import ru.ttraum.kontroller.constant.Constants
import ru.ttraum.kontroller.constant.MemberNames
import ru.ttraum.kontroller.model.*
import ru.ttraum.kontroller.predicate.AnnotationModelPredicates
import ru.ttraum.kontroller.predicate.ParameterModelPredicates
import ru.ttraum.kontroller.predicate.TypeModelPredicates
import ru.ttraum.kontroller.utils.useControlFlow

fun createRouteSpec(router: RouterModel, route: RouteModel): FunSpec =
    FunSpec.builder(route.name + route.method)
        .receiver(Route::class)
        .addCode(buildRouteCodeBlock(router, route))
        .build()

private fun buildRouteCodeBlock(router: RouterModel, route: RouteModel): CodeBlock =
    CodeBlock.builder()
        .apply {
            defineMethod(router, route) {
                setupHeaders(route.annotations.filter(AnnotationModelPredicates.headerParamAnnotation))
                setupQueryModel(route.queryParamsModels)
                setupQueryParam(route.parameters.filter(ParameterModelPredicates.hasQueryParamAnnotation))
                setupPathParams(route.parameters.filter(ParameterModelPredicates.hasPathParamAnnotation))
                setupBodyParam(route.bodyParam)
                setupMultipartParam(route.multipartParam)
                handleRequest(router, route)
            }
        }
        .build()

private fun CodeBlock.Builder.setupHeaders(headers: List<AnnotationModel>) =
    headers.forEach { header ->
        val name by header.fields
        val value by header.fields
        addStatement("call.response.%M(%S, %S)", MemberNames.ktorHeader, name, value)
    }

private fun CodeBlock.Builder.setupPathParams(pathParams: List<ParameterModel>) =
    pathParams.forEach { param ->
        val statement = "val ${param.name}: ${param.type.getFullSignature()} by call.parameters"
        addStatement(statement)
    }

private fun CodeBlock.Builder.setupBodyParam(bodyParam: ParameterModel?) =
    bodyParam?.let { param ->
        addStatement(
            "val ${param.name} = call.%M<${param.type.getFullSignature()}>()",
            MemberNames.ktorReceive
        )
    }

private fun CodeBlock.Builder.setupMultipartParam(multipartParam: ParameterModel?) =
    multipartParam?.let { param ->
        val annotation =
            param.annotations
                .single(TypeModelPredicates.multipartParamAnnotation compose AnnotationModel::type)

        val formFieldLimit = annotation.fields["formFieldLimit"] as Long

        addStatement(
            "val ${param.name} = call.%M($formFieldLimit)",
            MemberNames.ktorReceiveMultipart
        )
    }

private fun CodeBlock.Builder.setupQueryParam(pathParams: List<ParameterModel>) =
    pathParams.forEach { param ->
        val statement = "val ${param.name}: ${param.type.getFullSignature()} by call.request.queryParameters"
        addStatement(statement)
    }

private fun CodeBlock.Builder.setupQueryModel(queryParamsModels: List<QueryParamsModel>) =
    queryParamsModels.forEach { queryParamsModel ->
        useControlFlow("val ${queryParamsModel.name} = run") {
            queryParamsModel.params.forEach { param ->
                val statement =
                    "val ${param.name}: ${param.type.getFullSignature()} by call.request.queryParameters"
                addStatement(statement)
            }
            val parameterCall = buildParameterCall(queryParamsModel.params)
            addStatement("${queryParamsModel.type.getFullSignature()}($parameterCall)")
        }
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