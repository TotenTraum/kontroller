package ru.ttraum.kontroller.predicate

import com.google.devtools.ksp.symbol.KSAnnotation
import ru.ttraum.kontroller.constant.Constants
import ru.ttraum.kontroller.core.http.QueryModel


private fun createKSAnnotationPredicate(annotations: List<String?>): (KSAnnotation) -> Boolean =
    { it.annotationType.resolve().declaration.qualifiedName?.asString() in annotations }

object KSAnnotationPredicates {
    val httpMethodsAnnotation = createKSAnnotationPredicate(Constants.HttpMethodAnnotations)
    val queryModelAnnotation = createKSAnnotationPredicate(listOf(QueryModel::class.qualifiedName))
}