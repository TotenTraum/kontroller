package ru.ttraum.kontroller.mapper

import arrow.core.*
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.option
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import ru.ttraum.kontroller.constant.Constants
import ru.ttraum.kontroller.core.http.BodyParam
import ru.ttraum.kontroller.core.http.Controller
import ru.ttraum.kontroller.model.*
import ru.ttraum.kontroller.utils.*

private val httpMethodAnnotations = Constants.HttpMethodAnnotations
private val controllerAnnotation = Controller::class.qualifiedName

val httpMethodsAnnotationPredicate = createAnnotationPredicate(httpMethodAnnotations)
val httpMethodsTypePredicate = createTypePredicate(httpMethodAnnotations)
val controllerTypePredicate = createTypePredicate(listOf(controllerAnnotation))
val bodyParamAnnotationPredicate = createTypePredicate(listOf(BodyParam::class.qualifiedName))

private fun createAnnotationPredicate(annotations: List<String?>): (KSAnnotation) -> Boolean = { annotation ->
    annotation.annotationType.resolve().declaration.qualifiedName?.asString() in annotations
}

fun createTypePredicate(types: List<String?>): (TypeModel) -> Boolean = { typeModel ->
    typeModel.qualifiedName in types
}

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
        .first { controllerTypePredicate(it.type) }
        .let { it.fields["basePath"] as String }
}

private fun extractHandlers(controllerClass: KSClassDeclaration): EitherNel<MapperError, List<RouteModel>> =
    controllerClass.getAllFunctions()
        .filter { it.hasHttpMethodAnnotation() }
        .mapOrAccumulate { mapHandler(it).bind() }

private fun mapHandler(function: KSFunctionDeclaration): Either<MapperError, RouteModel> = either {
    val handlerName = function.qualifiedName?.getShortName() ?: ""
    val annotations = function.annotations.toAnnotationModels().toList()
    val parameters = function.parameters.extractParams()
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
        annotations = annotations
    )
}

data class HttpDefinition(val path: String, val method: String)

private fun extractHttpMethod(annotations: List<AnnotationModel>): Either<MapperError, HttpDefinition> = either {
    val httpMethods = annotations.filter { httpMethodsTypePredicate(it.type) }

    ensure(httpMethods.size == 1) { MapperError.ManyHttpMethods() }

    val method = httpMethods.first()

    val path = method.fields["path"] as? String ?: ""
    val httpMethod = method.fields["method"] as? String ?: method.type.className

    HttpDefinition(path, httpMethod)
}

private fun List<KSValueParameter>.extractParams(): Either<MapperError, List<ParameterModel>> = either {
    val params = this@extractParams.toParameterModels()

    val bodyParams = params.filter(hasBodyParamAnnotation)
    ensure(bodyParams.size <= 1) { MapperError.ManyBodyParams() }

    params
}

private fun generateRouterName(controllerName: String): String =
    controllerName.removeSuffix("Controller") + "Router"

private val hasBodyParamAnnotation: (ParameterModel) -> Boolean =
    { param -> param.annotations.any(bodyParamAnnotationPredicate compose AnnotationModel::type) }
