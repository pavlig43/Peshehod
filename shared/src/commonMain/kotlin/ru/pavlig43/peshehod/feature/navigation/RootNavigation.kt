package ru.pavlig43.peshehod.feature.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import ru.pavlig43.peshehod.feature.routes.DebugMapScreen

@Serializable
data object DebugMapRoute

@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = DebugMapRoute,
    ) {
        composable<DebugMapRoute> {
            DebugMapScreen()
        }
    }
}
