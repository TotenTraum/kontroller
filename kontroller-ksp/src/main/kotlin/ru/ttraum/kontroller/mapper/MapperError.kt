package ru.ttraum.kontroller.mapper

import ru.ttraum.kontroller.core.Error

sealed interface MapperError : Error {

    class ManyHttpMethods() : MapperError {
        override fun error() = "Multiple annotation methods were found"
    }

    class ManyBodyParams() : MapperError {
        override fun error() = "Multiple BodyParams annotation were found"
    }

    class InvalidMultipartParamType() : MapperError {
        override fun error() = "Invalid multipart parameter type were found"
    }

    class ManyMultipartParams() : MapperError {
        override fun error() = "Multiple MultipartParam annotation were found"
    }

    class RouteError(val route: String, val routes: List<MapperError>) : MapperError {
        override fun error(): String {
            return "route $route:\n\t" + routes.joinToString { it.error() }
        }
    }

    class RouterError(val controllerName: String, val routes: List<MapperError>) : MapperError {
        override fun error(): String {
            return "router $controllerName:\n\t" + routes.joinToString { it.error() }
        }
    }
}