package ru.ttraum.kontroller.utils

import com.google.devtools.ksp.symbol.KSValueArgument

inline fun <reified T : Any> List<KSValueArgument>.getValue(name: String): T? {
    val value = firstOrNull { it.name?.asString() == name }?.value
    if (value == null || value !is T) {
        return null
    }
    return value
}
