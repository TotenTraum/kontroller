package ru.ttraum.kontroller.model

import com.squareup.kotlinpoet.UNIT

data class TypeModel(
    val packageName: String,
    val className: String,
    val qualifiedName: String,
    val typeArguments: List<TypeModel>
) {
    fun getSignature(): String {
        return className + getGenericSignature()
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
    listOf()
)
