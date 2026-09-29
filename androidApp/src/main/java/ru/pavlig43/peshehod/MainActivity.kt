package ru.pavlig43.peshehod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.pavlig43.peshehod.core.network.AndroidDebugEnvironment
import ru.pavlig43.peshehod.core.network.initKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initKoin(AndroidDebugEnvironment)
        setContent { App() }
    }
}
