package com.example.darkroomnegativedisplay2.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val GLIDER_STEPS = 20
private const val STEP_DELAY_MS = 125L

// The 4 phases of a glider. Each fits in the same 3x3 box, so the box never moves.
private val GLIDER_PHASES: List<List<Pair<Int, Int>>> = listOf(
    listOf(1 to 0, 2 to 1, 0 to 2, 1 to 2, 2 to 2),
    listOf(0 to 0, 2 to 0, 1 to 1, 2 to 1, 1 to 2),
    listOf(2 to 0, 0 to 1, 2 to 1, 1 to 2, 2 to 2),
    listOf(0 to 0, 1 to 1, 2 to 1, 0 to 2, 1 to 2)
)

/**
 * Loading-wheel style intro: the glider is fixed in the middle of the screen and only
 * its cells change, cycling through its 4 phases, then [onFinished] is called.
 */
@Composable
fun GliderIntro(onFinished: () -> Unit) {
    val cellPx = with(LocalDensity.current) { 28.dp.toPx() }
    var step by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(500)
        repeat(GLIDER_STEPS) {
            step++
            delay(STEP_DELAY_MS)
        }
        delay(300)
        onFinished()
    }

    Box(Modifier.fillMaxSize().background(LifeBlack)) {
        Canvas(Modifier.fillMaxSize()) {
            val cells = GLIDER_PHASES[step % 4]
            val minX = cells.minOf { it.first }
            val minY = cells.minOf { it.second }
            val originX = (size.width - 3 * cellPx) / 2f
            val originY = (size.height - 3 * cellPx) / 2f
            for ((x, y) in cells) {
                drawRect(
                    color = LifeWhite,
                    topLeft = Offset(originX + (x - minX) * cellPx + 1f, originY + (y - minY) * cellPx + 1f),
                    size = Size(cellPx - 2f, cellPx - 2f)
                )
            }
        }
    }
}
