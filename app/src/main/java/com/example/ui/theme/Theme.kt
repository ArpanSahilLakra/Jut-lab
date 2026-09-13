package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NeoBrutalistColorScheme =
  lightColorScheme(
    primary = Ink,
    onPrimary = OffWhite,
    secondary = SafeGreen,
    onSecondary = OffWhite,
    tertiary = DangerRed,
    background = OffWhite,
    onBackground = Ink,
    surface = CardBackground,
    onSurface = Ink,
    error = DangerRed,
    onError = OffWhite
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NeoBrutalistColorScheme,
    typography = Typography,
    content = content
  )
}


