package ru.ttraum.example.controller

import ru.ttraum.example.api.HomePageApi

class HomePageController : HomePageApi {
    override fun helloWorld(): String {
        return "hello world"
    }
}