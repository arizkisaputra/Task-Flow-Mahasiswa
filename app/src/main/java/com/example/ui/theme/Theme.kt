package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = CalmPrimaryLight,
    onPrimary = CalmOnPrimaryLight,
    primaryContainer = CalmPrimaryContainerLight,
    onPrimaryContainer = CalmOnPrimaryContainerLight,
    secondary = SageSecondaryLight,
    onSecondary = SageOnSecondaryLight,
    secondaryContainer = SageSecondaryContainerLight,
    onSecondaryContainer = SageOnSecondaryContainerLight,
    tertiary = SandTertiaryLight,
    onTertiary = SandOnTertiaryLight,
    tertiaryContainer = SandTertiaryContainerLight,
    onTertiaryContainer = SandOnTertiaryContainerLight,
    background = CalmBgLight,
    onBackground = CalmTextPrimaryLight,
    surface = CalmSurfaceLight,
    onSurface = CalmTextPrimaryLight,
    surfaceVariant = CalmSurfaceVariantLight,
    onSurfaceVariant = CalmTextSecondaryLight,
    outline = CalmOutlineLight,
    outlineVariant = CalmOutlineVariantLight,
    error = Q1AccentLight,
    errorContainer = Q1ContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CalmPrimaryDark,
    onPrimary = CalmOnPrimaryDark,
    primaryContainer = CalmPrimaryContainerDark,
    onPrimaryContainer = CalmOnPrimaryContainerDark,
    secondary = SageSecondaryDark,
    onSecondary = SageOnSecondaryDark,
    secondaryContainer = SageSecondaryContainerDark,
    onSecondaryContainer = SageOnSecondaryContainerDark,
    tertiary = SandTertiaryDark,
    onTertiary = SandOnTertiaryDark,
    tertiaryContainer = SandTertiaryContainerDark,
    onTertiaryContainer = SandOnTertiaryContainerDark,
    background = CalmBgDark,
    onBackground = CalmTextPrimaryDark,
    surface = CalmSurfaceDark,
    onSurface = CalmTextPrimaryDark,
    surfaceVariant = CalmSurfaceVariantDark,
    onSurfaceVariant = CalmTextSecondaryDark,
    outline = CalmOutlineDark,
    outlineVariant = CalmOutlineVariantDark,
    error = Q1AccentDark,
    errorContainer = Q1ContainerDark
)

val TaskFlowShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TaskFlowTypography,
        shapes = TaskFlowShapes,
        content = content
    )
}
