package com.busgo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BusGoColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Rose,
    onSecondary = Color.White,
    tertiary = Coral,
    background = Cream,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = Muted,
    outline = Hairline,
    error = Danger,
    onError = Color.White
)

@Composable
fun BusGoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BusGoColors,
        typography = BusGoTypography,
        content = content
    )
}
