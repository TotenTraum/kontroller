package ru.ttraum.kontroller.model

data class QueryParamsModel(
    val name: String,
    val type: TypeModel,
    val params: List<ParameterModel>
)