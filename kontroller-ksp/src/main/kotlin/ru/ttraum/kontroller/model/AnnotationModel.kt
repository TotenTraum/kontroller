package ru.ttraum.kontroller.model

data class AnnotationModel(
    val type: TypeModel,
    val fields: Map<String, Any?>
)