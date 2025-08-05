package ru.ttraum.kontroller.constant

import com.squareup.kotlinpoet.MemberName

object MemberNames {

    val ktorGet = MemberName(PackageNames.KTOR_SERVER_ROUTING, "get")
    val ktorPost = MemberName(PackageNames.KTOR_SERVER_ROUTING, "post")
    val ktorPut = MemberName(PackageNames.KTOR_SERVER_ROUTING, "put")
    val ktorPatch = MemberName(PackageNames.KTOR_SERVER_ROUTING, "patch")
    val ktorRoute = MemberName(PackageNames.KTOR_SERVER_ROUTING, "route")
    val ktorDelete = MemberName(PackageNames.KTOR_SERVER_ROUTING, "delete")

    val ktorReceive = MemberName(PackageNames.KTOR_SERVER_RECEIVE, "receive")
    val ktorReceiveMultipart = MemberName(PackageNames.KTOR_SERVER_RECEIVE, "receiveMultipart")
    val ktorGetValueOrNull = MemberName(PackageNames.KONTROLLER_UTIL, "getValue")

    val ktorHeader = MemberName(PackageNames.KTOR_SERVER_RESPONSE, "header")
    val ktorRespond = MemberName(PackageNames.KTOR_SERVER_RESPONSE, "respond")
}