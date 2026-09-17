package ru.ttraum.kontroller.mapper

import ru.ttraum.kontroller.model.*

fun AnnotationModel.toSecurityConfig(): SecurityConfig = SecurityConfig(
    providers = (fields["providers"] as? List<*>).orEmpty().map { it.toString() },
    nested = fields["nested"] as? Boolean == true
)

fun AnnotationModel.toHttpHeaderConfig(): HttpHeaderConfig = HttpHeaderConfig(
    name = fields["name"] as? String ?: "",
    value = fields["value"] as? String ?: ""
)

fun AnnotationModel.toMultipartConfig(): MultipartConfig = MultipartConfig(
    formFieldLimit = fields["formFieldLimit"] as? Long ?: -1L
)

fun AnnotationModel.toHttpDefinition(): HttpDefinition = HttpDefinition(
    path = fields["path"] as? String ?: "",
    method = fields["method"] as? String ?: type.className
)

fun AnnotationModel.toBasePath(): String = fields["basePath"] as? String ?: ""

private const val SWAGGER_OPERATION = "io.swagger.v3.oas.annotations.Operation"
private const val SWAGGER_API_RESPONSE = "io.swagger.v3.oas.annotations.responses.ApiResponse"
private const val SWAGGER_API_RESPONSES = "io.swagger.v3.oas.annotations.responses.ApiResponses"
private const val SWAGGER_CONTENT = "io.swagger.v3.oas.annotations.media.Content"
private const val SWAGGER_TAG = "io.swagger.v3.oas.annotations.tags.Tag"
private const val SWAGGER_TAGS = "io.swagger.v3.oas.annotations.tags.Tags"
private const val SWAGGER_PARAMETER = "io.swagger.v3.oas.annotations.Parameter"
private const val SWAGGER_REQUEST_BODY = "io.swagger.v3.oas.annotations.parameters.RequestBody"
private const val SWAGGER_NO_IMPLEMENTATION = "java.lang.Void"

/**
 * Reads standard `io.swagger.v3.oas.annotations` annotations, if present. Kontroller's own
 * `@GET`/`@POST`/etc. annotations remain the source of truth for routing structure (path, method,
 * parameter binding); these Swagger annotations only enrich the generated route with
 * human-authored OpenAPI documentation via `Route.describe { ... }`.
 *
 * [requestBody] is decoded separately from the `@BodyParam`-annotated parameter's own annotations
 * (see [toRequestBodyDoc]), since `@RequestBody` is placed on the parameter, not the function.
 */
fun List<AnnotationModel>.toOperationDoc(requestBody: RequestBodyDoc?): OperationDoc? {
    val operation = singleOrNull { it.type.qualifiedName == SWAGGER_OPERATION } ?: return null

    return OperationDoc(
        summary = (operation.fields["summary"] as? String)?.takeIf { it.isNotBlank() },
        description = (operation.fields["description"] as? String)?.takeIf { it.isNotBlank() },
        deprecated = operation.fields["deprecated"] as? Boolean == true,
        tags = extractRepeatable(SWAGGER_TAG, SWAGGER_TAGS).mapNotNull { it.fields["name"] as? String },
        responses = extractRepeatable(SWAGGER_API_RESPONSE, SWAGGER_API_RESPONSES).map { it.toResponseDoc() },
        requestBody = requestBody
    )
}

/** Reads `@Parameter(description = ..., deprecated = ...)` off a kontroller-annotated parameter, if present. */
fun List<AnnotationModel>.toParameterDoc(): ParameterDoc? {
    val parameter = singleOrNull { it.type.qualifiedName == SWAGGER_PARAMETER } ?: return null
    return ParameterDoc(
        description = (parameter.fields["description"] as? String)?.takeIf { it.isNotBlank() },
        deprecated = parameter.fields["deprecated"] as? Boolean == true
    )
}

/** Reads `@RequestBody(description = ..., content = [@Content(...)])` off the `@BodyParam` parameter, if present. */
fun List<AnnotationModel>.toRequestBodyDoc(): RequestBodyDoc? {
    val requestBody = singleOrNull { it.type.qualifiedName == SWAGGER_REQUEST_BODY } ?: return null
    return RequestBodyDoc(
        description = (requestBody.fields["description"] as? String)?.takeIf { it.isNotBlank() },
        content = requestBody.firstContentDoc()
    )
}

/**
 * Swagger annotations like `@ApiResponse`/`@Tag` are `@Repeatable`, so callers may either repeat
 * them directly or wrap them in their container (`@ApiResponses`/`@Tags`, respectively via `value`).
 * Both forms are supported.
 */
private fun List<AnnotationModel>.extractRepeatable(single: String, container: String): List<AnnotationModel> {
    val direct = filter { it.type.qualifiedName == single }
    val wrapped = filter { it.type.qualifiedName == container }
        .flatMap { (it.fields["value"] as? List<*>).orEmpty() }
        .filterIsInstance<AnnotationModel>()
    return direct + wrapped
}

private fun AnnotationModel.toResponseDoc(): ResponseDoc = ResponseDoc(
    code = fields["responseCode"] as? String ?: "200",
    description = fields["description"] as? String ?: "",
    content = firstContentDoc()
)

private fun AnnotationModel.firstContentDoc(): ContentDoc? =
    (fields["content"] as? List<*>)
        ?.filterIsInstance<AnnotationModel>()
        ?.firstOrNull { it.type.qualifiedName == SWAGGER_CONTENT }
        ?.toContentDoc()

private fun AnnotationModel.toContentDoc(): ContentDoc {
    // `@Content.array`/`.schema` are annotation-typed fields, so KSP always resolves *some* instance for
    // them even when the caller never wrote `array = ...` - an unset `@ArraySchema` is indistinguishable
    // from an explicit one at this level. The only reliable signal is whether its nested `schema.implementation`
    // actually points at a real class rather than the swagger-annotations default placeholder (`Void`).
    val arrayImplementation = ((fields["array"] as? AnnotationModel)?.fields?.get("schema") as? AnnotationModel)
        ?.implementationType()

    if (arrayImplementation != null) {
        return ContentDoc(mediaType = mediaType(), schemaType = arrayImplementation, isArray = true)
    }

    val implementation = (fields["schema"] as? AnnotationModel)?.implementationType()
    return ContentDoc(mediaType = mediaType(), schemaType = implementation, isArray = false)
}

private fun AnnotationModel.mediaType(): String = fields["mediaType"] as? String ?: "application/json"

private fun AnnotationModel.implementationType(): TypeModel? =
    (fields["implementation"] as? TypeModel)?.takeIf { it.qualifiedName != SWAGGER_NO_IMPLEMENTATION }
