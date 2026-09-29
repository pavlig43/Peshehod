package ru.pavlig43.peshehod.core.network

import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.pavlig43.peshehod.core.map.DebugMapStyleProvider
import ru.pavlig43.peshehod.core.map.MapStyleProvider

private var koinStarted = false

fun initKoin(environment: AppEnvironment) {
    if (koinStarted) return

    startKoin {
        modules(
            module {
                single<AppEnvironment> { environment }
                single<MapStyleProvider> { DebugMapStyleProvider() }
                single { platformHttpClient() }
                single { HealthClient(get(), get()) }
            },
        )
    }
    koinStarted = true
}
