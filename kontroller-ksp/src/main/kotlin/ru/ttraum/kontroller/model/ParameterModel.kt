package ru.ttraum.kontroller.model

data class ParameterModel(
    val name: String,
    val type: TypeModel,
    val annotations: List<AnnotationModel>
)