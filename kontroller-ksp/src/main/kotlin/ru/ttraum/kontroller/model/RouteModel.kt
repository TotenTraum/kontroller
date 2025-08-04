package ru.ttraum.kontroller.model

data class RouteModel(
    val name: String,
    val path: String,
    val method: String,
    val parameters: List<ParameterModel>,
    val returnType: ResultModel,
    val annotations: List<AnnotationModel>
)