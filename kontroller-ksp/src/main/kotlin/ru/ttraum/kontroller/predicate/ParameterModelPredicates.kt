package ru.ttraum.kontroller.predicate

import ru.ttraum.kontroller.model.ParameterModel

object ParameterModelPredicates {

    val hasPathParamAnnotation: (ParameterModel) -> Boolean =
        { it.annotations.any(AnnotationModelPredicates.pathParamAnnotation) }

    val hasQueryParamAnnotation: (ParameterModel) -> Boolean =
        { it.annotations.any(AnnotationModelPredicates.queryParamAnnotation) }

    val hasBodyParamAnnotation: (ParameterModel) -> Boolean =
        { it.annotations.any(AnnotationModelPredicates.bodyParamAnnotation) }

    val hasMultipartParamAnnotation: (ParameterModel) -> Boolean =
        { it.annotations.any(AnnotationModelPredicates.multipartParamAnnotation) }
}