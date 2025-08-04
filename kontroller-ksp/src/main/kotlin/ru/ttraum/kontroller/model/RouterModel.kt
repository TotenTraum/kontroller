package ru.ttraum.kontroller.model

data class RouterModel(
    val name: String,
    val handlers: List<RouteModel>,
    val path: String,
    val controller: TypeModel,
    val annotations: List<AnnotationModel>
)

