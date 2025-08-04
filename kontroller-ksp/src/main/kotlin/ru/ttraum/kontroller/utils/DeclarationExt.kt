package ru.ttraum.kontroller.utils

import com.google.devtools.ksp.symbol.*
import ru.ttraum.kontroller.mapper.httpMethodsAnnotationPredicate
import ru.ttraum.kontroller.model.*

fun KSClassDeclaration.toTypeModel(): TypeModel = TypeModel(
    packageName = packageName.asString(),
    className = simpleName.asString(),
    qualifiedName = qualifiedName?.asString() ?: "",
    listOf()
)

fun KSDeclaration.toTypeModel(): TypeModel = TypeModel(
    packageName = packageName.asString(),
    className = simpleName.asString(),
    qualifiedName = qualifiedName?.asString() ?: "",
    listOf()
)

fun KSTypeReference.toTypeModel(): TypeModel {
    val ref = element
    val decl = resolve().declaration
    val typeArgs = ref?.typeArguments
        ?.mapNotNull { it.type }
        ?.map { it.toTypeModel() }
        ?: listOf()

    return TypeModel(
        packageName = decl.packageName.asString(),
        className = decl.simpleName.asString(),
        qualifiedName = decl.qualifiedName?.asString() ?: "",
        typeArgs
    )
}

fun Sequence<KSAnnotation>.toAnnotationModels(): Sequence<AnnotationModel> =
    map { annotation ->
        AnnotationModel(
            type = annotation.annotationType.resolve().declaration.toTypeModel(),
            fields = annotation.arguments.associate { arg ->
                arg.name!!.asString() to arg.value
            }
        )
    }

fun KSFunctionDeclaration.hasHttpMethodAnnotation(): Boolean =
    annotations.any(httpMethodsAnnotationPredicate)

fun List<KSValueParameter>.toParameterModels(): List<ParameterModel> =
    map { parameter ->
        ParameterModel(
            name = parameter.name?.asString() ?: "",
            type = parameter.type.toTypeModel(),
            annotations = parameter.annotations.toAnnotationModels().toList()
        )
    }

fun KSTypeReference?.toResultModel(): ResultModel =
    this?.let { typeRef ->
        ResultModel(
            type = typeRef.resolve().declaration.toTypeModel(),
            annotations = typeRef.annotations.toAnnotationModels().toList()
        )
    } ?: ResultModel(
        UnitTypeModel,
        listOf()
    )