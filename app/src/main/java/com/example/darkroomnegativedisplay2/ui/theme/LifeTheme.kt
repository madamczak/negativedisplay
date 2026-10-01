package com.example.darkroomnegativedisplay2.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LifeColorScheme = darkColorScheme(
    primary = LifeWhite,
    onPrimary = LifeBlack,
    secondary = LifeGreyLight,
    onSecondary = LifeBlack,
    tertiary = LifeGreyLight,
    onTertiary = LifeBlack,
    background = LifeBlack,
    onBackground = LifeWhite,
    surface = LifeBlack,
    onSurface = LifeWhite,
    surfaceVariant = LifeGrey,
    onSurfaceVariant = LifeWhite,
    secondaryContainer = LifeGreyDark,
    onSecondaryContainer = LifeWhite,
    outline = LifeWhite,
    outlineVariant = LifeGreyDark
)

// Square "cells"
private val LifeShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun DarkroomNegativeDisplay2Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LifeColorScheme,
        typography = Typography,
        shapes = LifeShapes
    ) {
        Surface(color = LifeBlack, content = content)
    }
}
