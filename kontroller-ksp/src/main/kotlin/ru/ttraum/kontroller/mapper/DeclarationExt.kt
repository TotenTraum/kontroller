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

fun Sequence<KSAnnotation>.toAnnotationModels(): Sequence<AnnotationModel> = map { it.toAnnotationModel() }

fun KSAnnotation.toAnnotationModel(): AnnotationModel = AnnotationModel(
    type = annotationType.resolve().declaration.toTypeModel(),
    fields = arguments.associate { arg -> arg.name!!.asString() to arg.value.toFieldValue() }
)

/**
 * Recursively resolves annotation argument values so nested annotations (e.g. `@ApiResponses(value = [@ApiResponse(...)])`)
 * decode into [AnnotationModel] and class-literal arguments (e.g. `@Schema(implementation = Foo::class)`) decode into
 * [TypeModel], instead of leaking raw KSP types into [AnnotationModel.fields].
 */
private fun Any?.toFieldValue(): Any? = when (this) {
    is KSAnnotation -> toAnnotationModel()
    is KSType -> declaration.toTypeModel()
    is List<*> -> map { it.toFieldValue() }
    else -> this
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
                ?.toMultipartConfig(),
            parameterDoc = annotations.toParameterDoc()
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
