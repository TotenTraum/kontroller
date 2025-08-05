package ru.ttraum.kontroller.predicate

import ru.ttraum.kontroller.constant.Constants
import ru.ttraum.kontroller.core.http.*
import ru.ttraum.kontroller.model.TypeModel

private fun createTypePredicate(types: List<String?>): (TypeModel) -> Boolean = { typeModel ->
    typeModel.qualifiedName in types
}

object TypeModelPredicates {
    val headerAnnotation = createTypePredicate(listOf(HttpHeader::class.qualifiedName))
    val pathParamAnnotation = createTypePredicate(listOf(PathParam::class.qualifiedName))
    val queryParamAnnotation = createTypePredicate(listOf(QueryParam::class.qualifiedName))
    val multipartParamAnnotation = createTypePredicate(listOf(MultipartParam::class.qualifiedName))
    val httpMethodsType = createTypePredicate(Constants.HttpMethodAnnotations)
    val controllerType = createTypePredicate(listOf(Controller::class.qualifiedName))
    val bodyParamAnnotation = createTypePredicate(listOf(BodyParam::class.qualifiedName))
}