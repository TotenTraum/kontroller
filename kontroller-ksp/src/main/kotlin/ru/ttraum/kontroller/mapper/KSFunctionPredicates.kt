package ru.ttraum.kontroller.mapper

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

object KSFunctionPredicates {
    val hasHttpMethodAnnotation: (KSFunctionDeclaration) -> Boolean =
        { it.annotations.any(KSAnnotationPredicates.httpMethodsAnnotation) }
}
