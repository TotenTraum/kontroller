package ru.ttraum.kontroller.predicate

import ru.ttraum.kontroller.constant.Constants
import ru.ttraum.kontroller.core.http.*
import ru.ttraum.kontroller.model.AnnotationModel

private fun createAnnotationPredicate(types: List<String?>): (AnnotationModel) -> Boolean = {
    it.type.qualifiedName in types
}

object AnnotationModelPredicates {
    val httpHeaderAnnotation = createAnnotationPredicate(listOf(HttpHeader::class.qualifiedName))
    val controllerTypeAnnotation = createAnnotationPredicate(listOf(Controller::class.qualifiedName))
    val hasHttpMethodsAnnotation = createAnnotationPredicate(Constants.HttpMethodAnnotations)
    val pathParamAnnotation = createAnnotationPredicate(listOf(PathParam::class.qualifiedName))
    val bodyParamAnnotation = createAnnotationPredicate(listOf(BodyParam::class.qualifiedName))
    val queryParamAnnotation = createAnnotationPredicate(listOf(QueryParam::class.qualifiedName))
    val multipartParamAnnotation = createAnnotationPredicate(listOf(MultipartParam::class.qualifiedName))
    val headerParamAnnotation = createAnnotationPredicate(listOf(HeaderParam::class.qualifiedName))
    val securityAnnotation = createAnnotationPredicate(listOf(Security::class.qualifiedName))
}
