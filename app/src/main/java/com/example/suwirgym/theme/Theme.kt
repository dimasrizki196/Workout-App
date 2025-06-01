// File: Theme.kt
package com.example.suwirgym.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = Color.Black,
    onSurface = Color.Black,
    // Kamu bisa tambahkan warna lainnya jika perlu, contoh:
    // primaryContainer = PrimaryVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun SuwirGymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
