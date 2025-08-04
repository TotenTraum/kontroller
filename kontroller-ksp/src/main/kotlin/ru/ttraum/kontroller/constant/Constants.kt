package ru.ttraum.kontroller.constant

import ru.ttraum.kontroller.core.http.*

object Constants {
    val HttpMethodAnnotations = listOf(
        GET::class,
        POST::class,
        PUT::class,
        DELETE::class,
        HttpMethod::class
    ).map { it.qualifiedName }

    val HttpMethods = listOf(
        "GET",
        "POST",
        "PUT",
        "PATCH",
        "DELETE"
    )

    val httpMethodToMemberName = mutableMapOf(
        "GET" to MemberNames.ktorGet,
        "POST" to MemberNames.ktorPost,
        "PUT" to MemberNames.ktorPut,
        "DELETE" to MemberNames.ktorDelete,
        "PATCH" to MemberNames.ktorPatch
    )
}

