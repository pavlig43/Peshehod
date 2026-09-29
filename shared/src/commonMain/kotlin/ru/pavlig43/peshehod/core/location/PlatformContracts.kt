package ru.pavlig43.peshehod.core.location

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val accuracyMeters: Double,
    val recordedAt: Instant,
)

interface LocationProvider {
    val locations: Flow<LocationSample>
    suspend fun current(): LocationSample
}

interface BackgroundTracker {
    val isRecording: Flow<Boolean>
    suspend fun start()
    suspend fun stop()
}

enum class AppPermission { Location, BackgroundLocation, PhotoLibrary }

interface PermissionController {
    suspend fun request(permission: AppPermission): Boolean
    suspend fun isGranted(permission: AppPermission): Boolean
}
