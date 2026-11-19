package com.manish.liquidlauncher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

/** One recorded pointer position, with the time it happened. */
data class TrailPoint(val position: Offset, val at: Long)

/**
 * Liquid gesture trail.
 *
 * The home screen feeds this list from a non-consuming pointer observer on the root box,
 * so every touch leaves a fading ribbon of droplets behind it. Drawing is one Canvas, so
 * the cost is a handful of circles per frame.
 */
@Composable
fun GestureTrail(
    points: List<TrailPoint>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (points.isEmpty()) return@Canvas
        val life = 700f
        for (i in points.indices) {
            val point = points[i]
            val age = (now - point.at).toFloat()
            if (age > life) continue
            val freshness = (1f - age / life).coerceIn(0f, 1f)
            val fraction = i.toFloat() / points.size
            val radius = (3f + 9f * freshness) * density
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.35f * freshness * fraction),
                        Color.Transparent
                    ),
                    center = point.position,
                    radius = radius * 2.2f
                ),
                radius = radius * 2.2f,
                center = point.position
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.22f * freshness * fraction),
                radius = radius * 0.45f,
                center = point.position
            )
        }
    }
}
