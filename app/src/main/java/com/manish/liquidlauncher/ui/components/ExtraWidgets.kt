package com.manish.liquidlauncher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.notifications.NotificationHub
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/** Spinning-vinyl music widget with jelly transport buttons (needs notification access). */
@Composable
fun MusicWidget(
    config: LiquidThemeConfig,
    onEnableAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val media by NotificationHub.media
    val connected by NotificationHub.connected
    val accent = config.accentColor()
    val clock = LocalAnimClock.current
    val m = media

    LiquidGlassSurface(config = config, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val art = m?.art
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer { rotationZ = if (m?.playing == true) (clock.value * 28f) % 360f else 0f }
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                if (art != null) {
                    Image(
                        bitmap = art,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Rounded.MusicNote, null, tint = accent, modifier = Modifier.size(28.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                if (m != null) {
                    Text(
                        text = m.title,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = m.artist,
                        color = accent,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (!connected) {
                    Text(
                        text = "Music",
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to allow notification access",
                        color = accent,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onEnableAccess() }
                    )
                } else {
                    Text(
                        text = "Nothing playing",
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp
                    )
                }
            }

            if (m != null) {
                MusicButton(Icons.Rounded.SkipPrevious, config) {
                    NotificationHub.controller?.transportControls?.skipToPrevious()
                }
                MusicButton(if (m.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, config) {
                    val tc = NotificationHub.controller?.transportControls
                    if (m.playing) tc?.pause() else tc?.play()
                }
                MusicButton(Icons.Rounded.SkipNext, config) {
                    NotificationHub.controller?.transportControls?.skipToNext()
                }
            }
        }
    }
}

@Composable
private fun MusicButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    config: LiquidThemeConfig,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .jellyTouch(haptics = config.haptics, pressX = 0.8f, pressY = 0.8f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

private fun hourNow(): Float {
    val c = Calendar.getInstance()
    return c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60f
}

private fun star(i: Int, salt: Int): Float {
    val x = sin(i * 91.7f + salt * 47.3f) * 43758.5453f
    return x - floor(x)
}

/**
 * Animated sky that follows your local time (sun, moon, stars, drifting clouds).
 * It is time-of-day art, not a live weather forecast.
 */
@Composable
fun SkyWidget(config: LiquidThemeConfig, modifier: Modifier = Modifier) {
    val clock = LocalAnimClock.current
    var hour by remember { mutableFloatStateOf(hourNow()) }
    LaunchedEffect(Unit) {
        while (true) {
            hour = hourNow()
            delay(60_000L)
        }
    }
    val isDay = hour in 6f..18f
    val daylight = if (isDay) sin(((hour - 6f) / 12f) * PI.toFloat()).coerceIn(0f, 1f) else 0f
    val twilight = if (hour in 5f..8f || hour in 16.5f..19.5f) 0.55f else 0f
    val top = lerp(Color(0xFF050B1F), Color(0xFF3B82F6), daylight)
    val bottomBase = lerp(Color(0xFF1B1040), Color(0xFF93C5FD), daylight)
    val bottom = lerp(bottomBase, Color(0xFFFB923C), twilight * (1f - daylight * 0.6f))
    val label = when {
        hour in 5f..11f -> "Morning sky"
        hour in 11f..16.5f -> "Afternoon sky"
        hour in 16.5f..20f -> "Evening sky"
        else -> "Night sky"
    }

    LiquidGlassSurface(config = config, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = clock.value
            drawRect(Brush.verticalGradient(listOf(top, bottom)))

            if (daylight < 0.35f) {
                for (i in 0 until 22) {
                    val twinkle = 0.4f + 0.6f * abs(sin(t * (0.6f + star(i, 3)) + i))
                    drawCircle(
                        color = Color.White.copy(alpha = twinkle * (0.55f - daylight)),
                        radius = (0.8f + star(i, 4) * 1.6f).dp.toPx(),
                        center = Offset(w * star(i, 1), h * star(i, 2) * 0.8f)
                    )
                }
            }

            val p = if (isDay) (hour - 6f) / 12f else ((hour + 6f) % 24f) / 12f
            val bx = w * (0.1f + 0.8f * p.coerceIn(0f, 1f))
            val by = h * (0.9f - 0.7f * sin(p.coerceIn(0f, 1f) * PI.toFloat()))
            val body = if (isDay) Color(0xFFFFE082) else Color(0xFFE3EAF7)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(body.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(bx, by),
                    radius = h * 0.55f
                ),
                radius = h * 0.55f,
                center = Offset(bx, by)
            )
            drawCircle(color = body, radius = 13.dp.toPx(), center = Offset(bx, by))

            for (i in 0 until 3) {
                val cx = ((t * (6f + i * 3f) + i * w / 3f) % (w + 140.dp.toPx())) - 70.dp.toPx()
                val cy = h * (0.25f + 0.22f * i) + cos(t * 0.3f + i) * 3.dp.toPx()
                val cw = (60f + i * 22f).dp.toPx()
                drawOval(
                    color = Color.White.copy(alpha = 0.16f + 0.06f * daylight),
                    topLeft = Offset(cx, cy),
                    size = Size(cw, cw * 0.38f)
                )
            }
        }
        Text(
            text = label,
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        )
    }
}
