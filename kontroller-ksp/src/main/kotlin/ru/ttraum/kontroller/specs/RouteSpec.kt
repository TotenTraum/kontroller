package ru.ttraum.kontroller.specs

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import io.ktor.http.*
import io.ktor.server.routing.*
import ru.ttraum.kontroller.constant.Constants
import ru.ttraum.kontroller.constant.MemberNames
import ru.ttraum.kontroller.constant.PackageNames
import ru.ttraum.kontroller.model.*
import ru.ttraum.kontroller.predicate.ParameterModelPredicates

private val authenticationStrategy =
    ClassName(PackageNames.KTOR_SERVER_AUTH, "AuthenticationStrategy")
private val listClassName = ClassName("kotlin.collections", "List")

fun createRouteSpec(router: RouterModel, route: RouteModel): FunSpec =
    FunSpec.builder(route.name + route.method)
        .receiver(Route::class)
        .addCode(buildRouteCodeBlock(router, route))
        .build()

private fun buildRouteCodeBlock(router: RouterModel, route: RouteModel): CodeBlock =
    CodeBlock.builder()
        .apply {
            defineMethod(router, route) {
                setupHeaders(route.headers)
                setupQueryModel(route.queryParamsModels)
                setupQueryParam(route.parameters.filter(ParameterModelPredicates.hasQueryParamAnnotation))
                setupPathParams(route.parameters.filter(ParameterModelPredicates.hasPathParamAnnotation))
                setupHeaderParams(route.parameters.filter(ParameterModelPredicates.hasHeaderParamAnnotation))
                setupBodyParam(route.bodyParam)
                setupMultipartParam(route.multipartParam)
                handleRequest(router, route)
            }
        }
        .build()

private fun CodeBlock.Builder.setupHeaders(headers: List<HttpHeaderConfig>) =
    headers.forEach { header ->
        addStatement("call.response.%M(%S, %S)", MemberNames.ktorHeader, header.name, header.value)
    }

private fun CodeBlock.Builder.setupPathParams(pathParams: List<ParameterModel>) =
    pathParams.forEach { param ->
        val statement = "val ${param.name}: ${param.type.fullSignature} by call.parameters"
        addStatement(statement)
    }

private fun CodeBlock.Builder.setupBodyParam(bodyParam: ParameterModel?) =
    bodyParam?.let { param ->
        addStatement(
            "val ${param.name} = call.%M<${param.type.fullSignature}>()",
            MemberNames.ktorReceive
        )
    }

private fun CodeBlock.Builder.setupMultipartParam(multipartParam: ParameterModel?) =
    multipartParam?.let { param ->
        val formFieldLimit = param.multipartConfig?.formFieldLimit ?: -1L

        addStatement(
            "val ${param.name} = call.%M($formFieldLimit)",
            MemberNames.ktorReceiveMultipart
        )
    }

private fun CodeBlock.Builder.setupQueryParam(pathParams: List<ParameterModel>) =
    pathParams.forEach { param ->
        val statement =
            "val ${param.name}: ${param.type.fullSignature} by call.request.queryParameters"
        addStatement(statement)
    }

private fun CodeBlock.Builder.setupHeaderParams(headerParams: List<ParameterModel>) =
    headerParams.forEach { param ->
        val statement = "val ${param.name}: ${param.type.fullSignature} by call.request.headers"
        addStatement(statement)
    }

private fun CodeBlock.Builder.setupQueryModel(queryParamsModels: List<QueryParamsModel>) =
    queryParamsModels.forEach { queryParamsModel ->
        useControlFlow("val ${queryParamsModel.name} = run") {
            queryParamsModel.params.forEach { param ->
                val statement =
                    "val ${param.name}: ${param.type.fullSignature} by call.request.queryParameters"
                addStatement(statement)
            }
            val parameterCall = buildParameterCall(queryParamsModel.params)
            addStatement("${queryParamsModel.type.fullSignature}($parameterCall)")
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
    val defineHttpMethod: CodeBlock.Builder.() -> Unit = {
        when {
            route.method in Constants.HttpMethods -> handleStandardHttpMethod(
                normalizedPath,
                route,
                body
            )

            else -> handleCustomHttpMethod(normalizedPath, route, body)
        }
    }

    setupSecurity(router, route, defineHttpMethod)
}

private fun CodeBlock.Builder.setupSecurity(
    router: RouterModel,
    route: RouteModel,
    body: CodeBlock.Builder.() -> Unit
) {
    val classSecurity = router.securityConfig
    val functionSecurity = route.securityConfig

    when {
        classSecurity == null && functionSecurity == null ->
            body()

        classSecurity != null && functionSecurity != null && (classSecurity.nested || functionSecurity.nested) ->
            setupAuthenticate(classSecurity.providers, required = true) {
                setupAuthenticate(functionSecurity.providers, required = true, body)
            }

        else -> {
            val providers =
                (classSecurity?.providers.orEmpty() + functionSecurity?.providers.orEmpty()).distinct()
            setupAuthenticate(providers, required = false, body)
        }
    }
}

private fun CodeBlock.Builder.setupAuthenticate(
    providers: List<String>,
    required: Boolean,
    body: CodeBlock.Builder.() -> Unit
) {
    val placeholders = providers.joinToString(", ") { "%S" }
    if (required) {
        useControlFlow(
            "this.%M($placeholders, strategy = %T.Required)",
            MemberNames.ktorAuthenticate,
            *providers.toTypedArray(),
            authenticationStrategy
        ) { body() }
    } else {
        useControlFlow(
            "this.%M($placeholders)",
            MemberNames.ktorAuthenticate,
            *providers.toTypedArray()
        ) { body() }
    }
}

private fun CodeBlock.Builder.handleCustomHttpMethod(
    path: String,
    route: RouteModel,
    body: CodeBlock.Builder.() -> Unit
) {
    beginControlFlow("this.%M(%S, %T.parse(%S))", MemberNames.ktorRoute, path, HttpMethod::class, route.method)
    useControlFlow("handle") { body() }
    describeOperation(path, route)
}

private fun CodeBlock.Builder.handleStandardHttpMethod(
    path: String,
    route: RouteModel,
    body: CodeBlock.Builder.() -> Unit
) {
    val methodName = Constants.httpMethodToMemberName[route.method] ?: return
    beginControlFlow("this.%M(%S)", methodName, path)
    body()
    describeOperation(path, route)
}

private val tailcardSegmentRegex = Regex("""\{(\w+)\.\.\.}""")

private fun CodeBlock.Builder.describeOperation(path: String, route: RouteModel) {
    nextControlFlow(".%M", MemberNames.ktorDescribe)
    addStatement("operationId = %S", route.name)
    route.operationDoc?.summary?.let { addStatement("summary = %S", it) }
    route.operationDoc?.description?.let { addStatement("description = %S", it) }
    if (route.operationDoc?.deprecated == true) addStatement("deprecated = true")
    route.operationDoc?.tags?.forEach { addStatement("tag(%S)", it) }

    // Ktor's OpenAPI generator renders a tailcard segment ({name...}) as the literal
    // path template "{**}", discarding the segment name. "**" is not a valid RFC 6570
    // template variable name (the trailing "*" is stripped as an explode modifier, leaving
    // no usable name), so no path(...) declaration can ever satisfy oas3 path-params
    // validation for it. Ktor's own doc generator doesn't document these either; match that.
    val tailcardParamNames = tailcardSegmentRegex.findAll(path).map { it.groupValues[1] }.toSet()

    val describedParams = route.parameters.filter {
        (ParameterModelPredicates.hasPathParamAnnotation(it) && it.name !in tailcardParamNames) ||
            ParameterModelPredicates.hasQueryParamAnnotation(it) ||
            ParameterModelPredicates.hasHeaderParamAnnotation(it)
    }
    if (describedParams.isNotEmpty()) {
        useControlFlow("parameters") {
            describedParams.forEach { param -> describeParameter(param) }
        }
    }

    route.operationDoc?.requestBody?.let { requestBodyDoc ->
        useControlFlow("requestBody") {
            requestBodyDoc.description?.let { addStatement("description = %S", it) }
            requestBodyDoc.content?.let { describeContent(it) }
        }
    }

    val responses = route.operationDoc?.responses?.takeIf { it.isNotEmpty() }
        ?: listOf(ResponseDoc("200", "OK", content = null))
    useControlFlow("responses") {
        responses.forEach { response -> describeResponse(response) }
    }

    endControlFlow()
}

private fun CodeBlock.Builder.describeParameter(param: ParameterModel) {
    val location = when {
        ParameterModelPredicates.hasPathParamAnnotation(param) -> "path"
        ParameterModelPredicates.hasQueryParamAnnotation(param) -> "query"
        else -> "header"
    }
    val doc = param.parameterDoc

    if (doc == null || (doc.description == null && !doc.deprecated)) {
        addStatement("$location(%S)", param.name)
    } else {
        useControlFlow("$location(%S)", param.name) {
            doc.description?.let { addStatement("description = %S", it) }
            if (doc.deprecated) addStatement("deprecated = true")
        }
    }
}

private fun CodeBlock.Builder.describeResponse(response: ResponseDoc) {
    val statusCode = response.code.toIntOrNull()
    val header = if (statusCode != null) "response($statusCode)" else "default"

    useControlFlow(header) {
        addStatement("description = %S", response.description)
        response.content?.let { describeContent(it) }
    }
}

private fun CodeBlock.Builder.describeContent(content: ContentDoc) {
    val schemaType = content.schemaType ?: return
    val typeName = ClassName(schemaType.packageName, schemaType.className)

    useControlFlow("%T.parse(%S).invoke", ContentType::class, content.mediaType) {
        if (content.isArray) {
            addStatement("schema = %M<%T<%T>>()", MemberNames.ktorJsonSchema, listClassName, typeName)
        } else {
            addStatement("schema = %M<%T>()", MemberNames.ktorJsonSchema, typeName)
        }
    }
}

private fun normalizePath(path: String): String {
    val normalized = path.replace("/{2,}".toRegex(), "/").removeSuffix("/").removePrefix("/")
    return normalized.ifEmpty { "/" }
}