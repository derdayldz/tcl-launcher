package com.derda.tclauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.derda.tclauncher.ui.HomeScreen
import com.derda.tclauncher.ui.SearchScreen
import com.derda.tclauncher.ui.SettingsScreen
import com.derda.tclauncher.ui.theme.TclLauncherTheme
import com.derda.tclauncher.ui.theme.parseHexColor

sealed class Screen {
    object Home : Screen()
    object Settings : Screen()
    object Search : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TvLauncherApp

        setContent {
            val settings by app.repository.settingsFlow.collectAsState(initial = null)
            var screen by remember { mutableStateOf<Screen>(Screen.Home) }

            val currentSettings = settings
            TclLauncherTheme(
                darkTheme = currentSettings?.darkTheme ?: true,
                accentColor = parseHexColor(currentSettings?.accentColorHex ?: "#1565C0")
            ) {
                when (screen) {
                    is Screen.Home -> HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        repository = app.repository,
                        onOpenSettings = { screen = Screen.Settings },
                        onOpenSearch = { screen = Screen.Search }
                    )
                    is Screen.Settings -> SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        repository = app.repository,
                        onBack = { screen = Screen.Home }
                    )
                    is Screen.Search -> SearchScreen(
                        modifier = Modifier.fillMaxSize(),
                        repository = app.repository,
                        onBack = { screen = Screen.Home }
                    )
                }
            }
        }
    }

    // Kullanıcı gerçek TV Home tuşuna bastığında Android bu Activity'yi zaten
    // ön plana getirir (HOME intent-filter sayesinde) - ekstra kod gerekmiyor.
}
