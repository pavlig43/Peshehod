package ru.pavlig43.peshehod.core.network

interface AppEnvironment {
    val apiBaseUrl: String
    val debug: Boolean
}

object AndroidDebugEnvironment : AppEnvironment {
    override val apiBaseUrl = "http://10.0.2.2:8080"
    override val debug = true
}

object IosDebugEnvironment : AppEnvironment {
    override val apiBaseUrl = "http://localhost:8080"
    override val debug = true
}
