package com.valeno.shaktisetu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandText,
    secondary = NavActive,
    tertiary = Pink40,
    background = BgStart,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = BrandText,
    onSurface = BrandText
)

@Composable
fun ShaktiSetuTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // App is now locked to Light Mode as per user request
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}