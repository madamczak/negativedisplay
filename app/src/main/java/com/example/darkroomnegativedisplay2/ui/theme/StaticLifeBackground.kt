package com.example.darkroomnegativedisplay2.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Static Game of Life "snapshot": dim white cells on black. No animation.
 * The pattern is seeded with a fixed value, so it looks the same every time.
 */
@Composable
fun GameOfLifeBackground(
    modifier: Modifier = Modifier,
    cellSize: Int = 14,
    aliveColor: Color = LifeWhite.copy(alpha = 0.22f)
) {
    val cellPx = with(LocalDensity.current) { cellSize.dp.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cols = (size.width / cellPx).toInt() + 1
        val rows = (size.height / cellPx).toInt() + 1
        val random = Random(42)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                if (random.nextFloat() < 0.2f) {
                    drawRect(
                        color = aliveColor,
                        topLeft = Offset(x * cellPx + 1f, y * cellPx + 1f),
                        size = Size(cellPx - 2f, cellPx - 2f)
                    )
                }
            }
        }
    }
}
