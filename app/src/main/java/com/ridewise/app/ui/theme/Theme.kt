package com.ridewise.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand Colors (Dark Blue & White)
val DarkBlue = Color(0xFF1A2A6C)
val White = Color(0xFFFFFFFF)
val LightBlue = Color(0xFF4A7BC4)

@Composable
fun RideWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = DarkBlue,
            secondary = LightBlue
        )
    } else {
        lightColorScheme(
            primary = DarkBlue,
            secondary = LightBlue,
            background = White,
            surface = White,
            onPrimary = White,
            onBackground = DarkBlue
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

