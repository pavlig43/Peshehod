package ru.pavlig43.peshehod

import androidx.compose.runtime.Composable
import ru.pavlig43.peshehod.core.designsystem.PeshehodTheme
import ru.pavlig43.peshehod.feature.navigation.RootNavigation

@Composable
fun App() {
    PeshehodTheme {
        RootNavigation()
    }
}
