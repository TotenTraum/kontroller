package ru.ttraum.kontroller.model

data class ContentDoc(
    val mediaType: String,
    val schemaType: TypeModel?,
    val isArray: Boolean
)

data class ResponseDoc(
    val code: String,
    val description: String,
    val content: ContentDoc?
)

data class RequestBodyDoc(
    val description: String?,
    val content: ContentDoc?
)

data class OperationDoc(
    val summary: String?,
    val description: String?,
    val deprecated: Boolean,
    val tags: List<String>,
    val responses: List<ResponseDoc>,
    val requestBody: RequestBodyDoc?
)

data class ParameterDoc(
    val description: String?,
    val deprecated: Boolean
)
