package ru.pavlig43.peshehod.feature.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ru.pavlig43.peshehod.core.map.MapLibreMapView
import ru.pavlig43.peshehod.core.map.MapStyleProvider
import ru.pavlig43.peshehod.core.network.HealthClient

@Composable
fun DebugMapScreen(
    styleProvider: MapStyleProvider = koinInject(),
    healthClient: HealthClient = koinInject(),
) {
    val mapView = remember(styleProvider) { MapLibreMapView(styleProvider) }
    var serverStatus by remember { mutableStateOf("CHECKING") }

    LaunchedEffect(healthClient) {
        serverStatus = healthClient.health().fold(
            onSuccess = { it.status },
            onFailure = { "OFFLINE" },
        )
    }

    Box(Modifier.fillMaxSize()) {
        mapView.Content(Modifier.fillMaxSize())
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            tonalElevation = 6.dp,
        ) {
            Text(
                text = "Peshehod · debug map · API $serverStatus\n${healthClient.healthUrl}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
