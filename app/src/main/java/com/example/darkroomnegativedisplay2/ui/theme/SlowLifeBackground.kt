package com.example.darkroomnegativedisplay2.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val BACKGROUND_STEP_MS = 5000L

/**
 * Very slow Game of Life background: one generation every 5 seconds.
 * Starts from a fixed seed, wraps around the screen edges, and reseeds if it dies out.
 */
@Composable
fun GameOfLifeBackground(
    modifier: Modifier = Modifier,
    cellSize: Int = 14,
    aliveColor: Color = LifeWhite.copy(alpha = 0.22f)
) {
    val cellPx = with(LocalDensity.current) { cellSize.dp.toPx() }
    var cols by remember { mutableStateOf(0) }
    var rows by remember { mutableStateOf(0) }
    var grid by remember { mutableStateOf(BooleanArray(0)) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val c = (size.width / cellPx).toInt() + 1
        val r = (size.height / cellPx).toInt() + 1
        if (c != cols || r != rows) {
            val random = Random(42)
            cols = c
            rows = r
            grid = BooleanArray(c * r) { random.nextFloat() < 0.2f }
        }
        val g = grid
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                if (g.getOrElse(y * cols + x) { false }) {
                    drawRect(
                        color = aliveColor,
                        topLeft = Offset(x * cellPx + 1f, y * cellPx + 1f),
                        size = Size(cellPx - 2f, cellPx - 2f)
                    )
                }
            }
        }
    }

    LaunchedEffect(cols, rows) {
        while (cols > 0 && rows > 0) {
            delay(BACKGROUND_STEP_MS)
            val c = cols
            val r = rows
            val old = grid
            if (old.size != c * r) continue
            val next = BooleanArray(c * r)
            var alive = 0
            for (y in 0 until r) {
                for (x in 0 until c) {
                    var n = 0
                    for (dy in -1..1) for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        if (old[((y + dy + r) % r) * c + (x + dx + c) % c]) n++
                    }
                    val live = old[y * c + x]
                    val state = if (live) n == 2 || n == 3 else n == 3
                    next[y * c + x] = state
                    if (state) alive++
                }
            }
            if (alive < (c * r) / 40) {
                for (i in next.indices) if (Random.nextFloat() < 0.2f) next[i] = true
            }
            grid = next
        }
    }
}
