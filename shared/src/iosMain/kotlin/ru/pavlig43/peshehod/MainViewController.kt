package ru.pavlig43.peshehod

import androidx.compose.ui.window.ComposeUIViewController
import ru.pavlig43.peshehod.core.network.IosDebugEnvironment
import ru.pavlig43.peshehod.core.network.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin(IosDebugEnvironment)
    App()
}
