package ru.ttraum.kontroller.mapper

import com.google.devtools.ksp.symbol.*
import ru.ttraum.kontroller.model.*
import ru.ttraum.kontroller.predicate.AnnotationModelPredicates

fun KSDeclaration.toTypeModel(): TypeModel = TypeModel(
    packageName = packageName.asString(),
    className = simpleName.asString(),
    qualifiedName = qualifiedName?.asString() ?: "",
    false,
    listOf()
)

fun KSTypeReference.toTypeModel(): TypeModel {
    val ref = element
    val decl = resolve().declaration
    val typeArgs = ref?.typeArguments
        ?.mapNotNull { it.type }
        ?.map { it.toTypeModel() }
        ?: listOf()

    val isNullable = (this.resolve().nullability == Nullability.NULLABLE)

    return TypeModel(
        packageName = decl.packageName.asString(),
        className = decl.simpleName.asString(),
        qualifiedName = decl.qualifiedName?.asString() ?: "",
        isNullable = isNullable,
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

fun List<KSValueParameter>.toParameterModels(): List<ParameterModel> =
    map { parameter ->
        val annotations = parameter.annotations.toAnnotationModels().toList()
        ParameterModel(
            name = parameter.name?.asString() ?: "",
            type = parameter.type.toTypeModel(),
            annotations = annotations,
            multipartConfig = annotations
                .singleOrNull(AnnotationModelPredicates.multipartParamAnnotation)
                ?.toMultipartConfig()
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
