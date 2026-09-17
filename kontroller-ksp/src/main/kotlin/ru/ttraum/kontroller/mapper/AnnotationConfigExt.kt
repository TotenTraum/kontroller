package ru.ttraum.kontroller.mapper

import ru.ttraum.kontroller.model.*

fun AnnotationModel.toSecurityConfig(): SecurityConfig = SecurityConfig(
    providers = (fields["providers"] as? List<*>).orEmpty().map { it.toString() },
    nested = fields["nested"] as? Boolean == true
)

fun AnnotationModel.toHttpHeaderConfig(): HttpHeaderConfig = HttpHeaderConfig(
    name = fields["name"] as? String ?: "",
    value = fields["value"] as? String ?: ""
)

fun AnnotationModel.toMultipartConfig(): MultipartConfig = MultipartConfig(
    formFieldLimit = fields["formFieldLimit"] as? Long ?: -1L
)

fun AnnotationModel.toHttpDefinition(): HttpDefinition = HttpDefinition(
    path = fields["path"] as? String ?: "",
    method = fields["method"] as? String ?: type.className
)

fun AnnotationModel.toBasePath(): String = fields["basePath"] as? String ?: ""
