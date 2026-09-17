package ru.ttraum.kontroller.specs

import com.squareup.kotlinpoet.CodeBlock

inline fun CodeBlock.Builder.useControlFlow(
    controlFlow: String,
    vararg args: Any?,
    body: CodeBlock.Builder.() -> Unit
) = also {
    beginControlFlow(controlFlow, *args)
    body()
    endControlFlow()
}
