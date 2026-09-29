package ru.pavlig43.peshehod.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

expect fun platformHttpClient(): HttpClient

@Serializable
data class HealthResponse(
    val status: String,
)

class HealthClient(
    private val environment: AppEnvironment,
    private val client: HttpClient,
) {
    val healthUrl: String = "${environment.apiBaseUrl}/actuator/health"

    suspend fun health(): Result<HealthResponse> = runCatching {
        client.get(healthUrl).body()
    }
}
