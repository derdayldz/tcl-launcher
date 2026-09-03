package com.derda.tclauncher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

@Composable
fun TclLauncherTheme(
    darkTheme: Boolean = true,
    accentColor: Color = Color(0xFF1565C0),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            background = LauncherBlack,
            surface = LauncherSurface,
            onBackground = LauncherWhite,
            onSurface = LauncherWhite
        )
    } else {
        lightColorScheme(
            primary = accentColor
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

fun parseHexColor(hex: String, fallback: Color = Color(0xFF1565C0)): Color {
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(fallback)
}
