package ru.ttraum.kontroller.mapper

import arrow.core.*
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.option
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import io.ktor.http.content.*
import ru.ttraum.kontroller.model.*
import ru.ttraum.kontroller.predicate.AnnotationModelPredicates
import ru.ttraum.kontroller.predicate.KSAnnotationPredicates
import ru.ttraum.kontroller.predicate.KSFunctionPredicates
import ru.ttraum.kontroller.predicate.ParameterModelPredicates
import ru.ttraum.kontroller.utils.toAnnotationModels
import ru.ttraum.kontroller.utils.toParameterModels
import ru.ttraum.kontroller.utils.toResultModel
import ru.ttraum.kontroller.utils.toTypeModel

fun controllerModelFromKSClass(controllerClass: KSClassDeclaration): Either<MapperError, RouterModel> = either {
    val controller = controllerClass.toTypeModel()
    val routerName = generateRouterName(controller.className)
    val annotations = controllerClass.annotations.toAnnotationModels().toList()
    val handlers = extractHandlers(controllerClass)
        .mapLeft { MapperError.RouterError(routerName, it) }
        .bind()

    val basePath = extractBasePath(annotations).getOrElse { "" }

    RouterModel(routerName, handlers, basePath, controller, annotations)
}

fun extractBasePath(annotations: List<AnnotationModel>): Option<String> = option {
    annotations
        .first(AnnotationModelPredicates.controllerTypeAnnotation)
        .let { it.fields["basePath"] as String }
}

private fun extractHandlers(controllerClass: KSClassDeclaration): EitherNel<MapperError, List<RouteModel>> =
    controllerClass.getAllFunctions()
        .filter(KSFunctionPredicates.hasHttpMethodAnnotation)
        .mapOrAccumulate { mapHandler(it).bind() }

private fun mapHandler(function: KSFunctionDeclaration): Either<MapperError, RouteModel> = either {
    val handlerName = function.qualifiedName?.getShortName() ?: ""
    val annotations = function.annotations.toAnnotationModels().toList()
    val parameters = function.parameters.toParameterModels().toList()

    val queryParams = function.parameters.extractQueryParamsModel()
        .mapLeft { MapperError.RouteError(handlerName, listOf(it)) }
        .bind()

    val multipartParam = function.parameters.extractMultipart()
        .mapLeft { MapperError.RouteError(handlerName, listOf(it)) }
        .bind()

    val bodyParam = function.parameters.extractBodyParams()
        .mapLeft { MapperError.RouteError(handlerName, listOf(it)) }
        .bind()

    val definition = extractHttpMethod(annotations)
        .mapLeft { MapperError.RouteError(handlerName, listOf(it)) }
        .bind()

    val returnType = function.returnType.toResultModel()

    RouteModel(
        name = handlerName,
        path = definition.path,
        method = definition.method,
        parameters = parameters,
        returnType = returnType,
        annotations = annotations,
        queryParamsModels = queryParams,
        multipartParam = multipartParam,
        bodyParam = bodyParam,
    )
}

private fun extractHttpMethod(annotations: List<AnnotationModel>): Either<MapperError, HttpDefinition> = either {
    val httpMethods = annotations.filter(AnnotationModelPredicates.hasHttpMethodsAnnotation)

    ensure(httpMethods.size == 1) { MapperError.ManyHttpMethods() }

    val method = httpMethods.first()

    val path = method.fields["path"] as? String ?: ""
    val httpMethod = method.fields["method"] as? String ?: method.type.className

    HttpDefinition(path, httpMethod)
}

private fun List<KSValueParameter>.extractQueryParamsModel(): Either<MapperError, List<QueryParamsModel>> = either {
    this@extractQueryParamsModel
        .filter { it.annotations.any(KSAnnotationPredicates.queryModelAnnotation) }
        .filter { it.type.resolve().declaration is KSClassDeclaration }
        .map {
            val classDecl = it.type.resolve().declaration as KSClassDeclaration
            val params: List<ParameterModel> =
                classDecl
                    .primaryConstructor
                    ?.parameters
                    ?.toParameterModels()
                    ?: listOf()

            QueryParamsModel(
                it.name!!.asString(),
                it.type.toTypeModel(),
                params
            )
        }
}

private fun List<KSValueParameter>.extractBodyParams(): Either<MapperError, ParameterModel?> = either {
    val params = this@extractBodyParams.toParameterModels()

    val bodyParams = params.filter(ParameterModelPredicates.hasBodyParamAnnotation)
    ensure(bodyParams.size <= 1) { MapperError.ManyBodyParams() }

    bodyParams.singleOrNull()
}

private fun List<KSValueParameter>.extractMultipart(): Either<MapperError, ParameterModel?> = either {
    val multipartParam = this@extractMultipart
        .toParameterModels()
        .filter(ParameterModelPredicates.hasMultipartParamAnnotation)

    ensure(multipartParam.size <= 1) { MapperError.ManyMultipartParams() }

    val multipart = multipartParam.singleOrNull()

    if (multipart != null) {
        ensure(multipart.type.qualifiedName == MultiPartData::class.qualifiedName) { MapperError.InvalidMultipartParamType() }
    }

    multipart
}

private fun generateRouterName(controllerName: String): String =
    controllerName.removeSuffix("Controller") + "Router"
