package ru.ttraum.kontroller.predicate

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

object KSFunctionPredicates {
    val hasHttpMethodAnnotation: (KSFunctionDeclaration) -> Boolean =
        { it.annotations.any(KSAnnotationPredicates.httpMethodsAnnotation) }
}