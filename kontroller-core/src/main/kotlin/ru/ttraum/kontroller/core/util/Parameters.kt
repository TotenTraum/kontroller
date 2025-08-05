package ru.ttraum.kontroller.core.util

import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.util.converters.*
import io.ktor.util.reflect.*
import kotlin.reflect.KProperty


inline operator fun <reified R : Any?> Parameters.getValue(thisRef: Any?, property: KProperty<*>): R {
    return getOrNull<R>(property.name)
}

inline fun <reified R : Any?> Parameters.getOrNull(name: String): R {
    return getOrNullImpl(name, typeInfo<R>())
}

@PublishedApi
internal fun <R : Any?> Parameters.getOrNullImpl(name: String, typeInfo: TypeInfo): R {
    val values = getAll(name) ?: listOf()
    return try {
        @Suppress("UNCHECKED_CAST")
        DefaultConversionService.fromValues(values, typeInfo) as R
    } catch (cause: Exception) {
        throw ParameterConversionException(name, typeInfo.type.simpleName ?: typeInfo.type.toString(), cause)
    }
}
