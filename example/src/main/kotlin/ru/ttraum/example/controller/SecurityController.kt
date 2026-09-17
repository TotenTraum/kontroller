package ru.ttraum.example.controller

import ru.ttraum.example.api.SecurityApi

class SecurityController : SecurityApi {
    override fun classSecurity(): String {
        return "class security"
    }

    override fun classAndFunSecurity(): String {
        return "class and fun security"
    }

    override fun nestedFunSecurity(): String {
        return "nested fun security"
    }
}
