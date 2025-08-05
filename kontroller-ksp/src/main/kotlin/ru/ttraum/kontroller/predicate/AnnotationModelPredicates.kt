package ru.ttraum.kontroller.predicate

import arrow.core.compose
import ru.ttraum.kontroller.model.AnnotationModel

object AnnotationModelPredicates {
    val headerParamAnnotation: (AnnotationModel) -> Boolean =
        TypeModelPredicates.headerAnnotation compose AnnotationModel::type

    val controllerTypeAnnotation: (AnnotationModel) -> Boolean =
        TypeModelPredicates.controllerType compose AnnotationModel::type

    val hasHttpMethodsAnnotation: (AnnotationModel) -> Boolean =
        TypeModelPredicates.httpMethodsType compose AnnotationModel::type

    val pathParamAnnotation: (AnnotationModel) -> Boolean =
        TypeModelPredicates.pathParamAnnotation compose AnnotationModel::type

    val bodyParamAnnotation: (AnnotationModel) -> Boolean =
        TypeModelPredicates.bodyParamAnnotation compose AnnotationModel::type

    val queryParamAnnotation: (AnnotationModel) -> Boolean =
        TypeModelPredicates.queryParamAnnotation compose AnnotationModel::type

    val multipartParamAnnotation: (AnnotationModel) -> Boolean =
        TypeModelPredicates.multipartParamAnnotation compose AnnotationModel::type
}