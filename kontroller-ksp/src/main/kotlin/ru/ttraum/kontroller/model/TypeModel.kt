package ru.ttraum.kontroller.model

import com.squareup.kotlinpoet.UNIT

data class TypeModel(
    val packageName: String,
    val className: String,
    val qualifiedName: String,
    val isNullable: Boolean,
    val typeArguments: List<TypeModel>
) {
    val signature: String
        get() = className + genericSignature + (if (isNullable) "?" else "")

    val fullSignature: String
        get() = "$packageName.$signature"

    private val genericSignature: String
        get() = typeArguments
            .takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "<", postfix = ">") { it.signature }
            ?: ""
}

val UnitTypeModel = TypeModel(
    UNIT.packageName,
    UNIT.simpleName,
    UNIT.packageName + "." + UNIT.simpleName,
    false,
    listOf()
)
