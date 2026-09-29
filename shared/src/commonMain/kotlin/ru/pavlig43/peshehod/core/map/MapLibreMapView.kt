package ru.pavlig43.peshehod.core.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

class MapLibreMapView(
    private val styleProvider: MapStyleProvider,
) : MapView {
    @Composable
    @UiComposable
    @Suppress("COMPOSABLE_APPLIER_CALL_MISMATCH")
    override fun Content(modifier: Modifier) {
        MaplibreMap(
            modifier = modifier,
            baseStyle = BaseStyle.Uri(styleProvider.debugStyleUri()),
        )
    }
}

class DebugMapStyleProvider : MapStyleProvider {
    override fun debugStyleUri(): String =
        "https://tiles.openfreemap.org/styles/liberty"
}
