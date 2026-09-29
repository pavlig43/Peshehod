package ru.pavlig43.peshehod.core.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable

interface MapView {
    @Composable
    @UiComposable
    fun Content(modifier: Modifier = Modifier)
}

interface OfflineMapStore {
    suspend fun download(regionId: String)
    suspend fun remove(regionId: String)
    suspend fun storedRegionIds(): Set<String>
}

interface MapStyleProvider {
    fun debugStyleUri(): String
}
