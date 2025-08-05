package ru.ttraum.kontroller.model

import com.squareup.kotlinpoet.UNIT

data class TypeModel(
    val packageName: String,
    val className: String,
    val qualifiedName: String,
    val isNullable: Boolean,
    val typeArguments: List<TypeModel>
) {
    fun getSignature(): String {
        val nullSafety = if (isNullable) "?" else ""
        return className + getGenericSignature() + nullSafety
    }

    fun getFullSignature(): String {
        return packageName + "." + getSignature()
    }

    private fun getGenericSignature(): String {
        if (typeArguments.isEmpty()) {
            return ""
        }
        return typeArguments.joinToString(prefix = "<", postfix = ">") { it.getSignature() }
    }
}

val UnitTypeModel = TypeModel(
    UNIT.packageName,
    UNIT.simpleName,
    UNIT.packageName + "." + UNIT.simpleName,
    false,
    listOf()
)
