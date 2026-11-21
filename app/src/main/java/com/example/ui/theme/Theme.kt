package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalIsDarkTheme = staticCompositionLocalOf { true }

private val LightColorScheme = lightColorScheme(
    primary = Sky600,
    onPrimary = PureWhite,
    primaryContainer = Sky100,
    onPrimaryContainer = Sky950,
    secondary = Emerald600,
    onSecondary = PureWhite,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Emerald950,
    tertiary = Amber600,
    onTertiary = PureWhite,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber950,
    background = Alabaster50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Alabaster100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200.copy(alpha = 0.85f),
    error = Rose700,
    errorContainer = Q1ContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Sky400,
    onPrimary = Obsidian950,
    primaryContainer = Sky400.copy(alpha = 0.14f),
    onPrimaryContainer = Sky400,
    secondary = Emerald400,
    onSecondary = Obsidian950,
    secondaryContainer = Emerald400.copy(alpha = 0.15f),
    onSecondaryContainer = Emerald300,
    tertiary = Amber400,
    onTertiary = Obsidian950,
    tertiaryContainer = Amber400.copy(alpha = 0.15f),
    onTertiaryContainer = Amber300,
    background = Obsidian950,
    onBackground = PureWhite,
    surface = Obsidian850,
    onSurface = Slate100,
    surfaceVariant = Obsidian800,
    onSurfaceVariant = Slate400,
    outline = Obsidian700,
    outlineVariant = Color.White.copy(alpha = 0.08f),
    error = Rose400,
    errorContainer = Q1ContainerDark
)

val TaskFlowShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TaskFlowTypography,
            shapes = TaskFlowShapes,
            content = content
        )
    }
}
