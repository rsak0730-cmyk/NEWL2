package com.manish.liquidlauncher.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.SystemClock
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.EdgePattern
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.model.TypeTargets
import com.manish.liquidlauncher.notifications.NotificationHub
import com.manish.liquidlauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------------------------

private fun two(value: Long): String = if (value < 10) "0$value" else value.toString()

private fun readMemory(context: Context): Pair<Long, Long> {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val info = ActivityManager.MemoryInfo()
    am.getMemoryInfo(info)
    return (info.totalMem - info.availMem) to info.totalMem
}

private fun readStorage(): Pair<Long, Long> = try {
    val stat = android.os.StatFs(Environment.getDataDirectory().absolutePath)
    val total = stat.blockCountLong * stat.blockSizeLong
    val free = stat.availableBlocksLong * stat.blockSizeLong
    (total - free) to total
} catch (_: Exception) {
    0L to 0L
}

private fun gb(bytes: Long): String =
    String.format(Locale.US, "%.1f", bytes / 1_073_741_824.0)

private fun ago(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < 60_000L -> "now"
        diff < 3_600_000L -> "${diff / 60_000L}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
        else -> "${diff / 86_400_000L}d ago"
    }
}

// ---------------------------------------------------------------------------------------------
// World clock
// ---------------------------------------------------------------------------------------------

private data class Zone(val label: String, val id: String)

private val WorldZones = listOf(
    Zone("India", "Asia/Kolkata"),
    Zone("London", "Europe/London"),
    Zone("New York", "America/New_York"),
    Zone("Tokyo", "Asia/Tokyo"),
    Zone("Dubai", "Asia/Dubai")
)

@Composable
fun WorldClockWidget(
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val accent = config.accentColor()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "WORLD CLOCK",
                color = accent,
                fontFamily = fontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            WorldZones.forEach { zone ->
                val fmt = remember(zone.id) {
                    SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = TimeZone.getTimeZone(zone.id) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = zone.label,
                        color = Color.White.copy(alpha = 0.72f),
                        fontFamily = fontFamily,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                    Text(
                        text = fmt.format(Date(now)),
                        color = Color.White,
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Countdown
// ---------------------------------------------------------------------------------------------

@Composable
fun CountdownWidget(
    viewModel: LauncherViewModel,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val accent = config.accentColor()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000L)
        }
    }
    val target = viewModel.countdownTarget
    val left = (target - now).coerceAtLeast(0L)
    val days = left / 86_400_000L
    val hours = (left % 86_400_000L) / 3_600_000L
    val minutes = (left % 3_600_000L) / 60_000L

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = viewModel.countdownLabel.uppercase(),
                color = accent,
                fontFamily = fontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (target <= 0L) {
                Text(
                    text = "Set a date in Theme Engine › Widgets",
                    color = Color.White.copy(alpha = 0.65f),
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    maxLines = 3
                )
            } else {
                Text(
                    text = "$days",
                    color = Color.White,
                    fontFamily = fontFamily,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "days  ${two(hours)}h ${two(minutes)}m",
                    color = Color.White.copy(alpha = 0.75f),
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Timer / stopwatch
// ---------------------------------------------------------------------------------------------

@Composable
fun TimerWidget(
    viewModel: LauncherViewModel,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val accent = config.accentColor()
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(viewModel.timerRunning) {
        while (viewModel.timerRunning) {
            tick++
            delay(100L)
        }
    }
    val elapsed = remember(tick, viewModel.timerAccumMs, viewModel.timerStartedAt) {
        viewModel.elapsedTimerMs()
    }
    val totalSeconds = elapsed / 1000L
    val text = "${two(totalSeconds / 60L)}:${two(totalSeconds % 60L)}"

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timer, null, tint = accent, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "TIMER",
                    color = accent,
                    fontFamily = fontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
            Text(
                text = text,
                color = Color.White,
                fontFamily = fontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.toggleTimer() }, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = if (viewModel.timerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (viewModel.timerRunning) "Pause" else "Start",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { viewModel.resetTimer() }, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reset",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Weather (Open-Meteo, keyless)
// ---------------------------------------------------------------------------------------------

@Composable
fun WeatherWidget(
    viewModel: LauncherViewModel,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val accent = config.accentColor()
    val info = viewModel.weather
    LaunchedEffect(Unit) {
        if (viewModel.weather == null) viewModel.refreshWeather()
    }

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (info?.isDay == false) Icons.Rounded.Cloud else Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
                IconButton(onClick = { viewModel.refreshWeather() }, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh weather",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            if (info == null) {
                Text(
                    text = if (viewModel.weatherBusy) "Loading…" else "Tap refresh",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            } else {
                Text(
                    text = "${info.temperatureC.roundToInt()}°C",
                    color = Color.White,
                    fontFamily = fontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Column {
                    Text(
                        text = info.label,
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = fontFamily,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = info.city,
                        color = accent,
                        fontFamily = fontFamily,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Activity timeline
// ---------------------------------------------------------------------------------------------

@Composable
fun ActivityTimelineWidget(
    viewModel: LauncherViewModel,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val accent = config.accentColor()
    val history = viewModel.history
    val labels = remember(viewModel.allApps) { viewModel.allApps.associate { it.packageName to it.label } }

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "ACTIVITY",
                color = accent,
                fontFamily = fontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            if (history.isEmpty()) {
                Text(
                    text = "Apps you open will show up here.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = fontFamily,
                    fontSize = 10.sp
                )
            } else {
                history.take(4).forEach { event ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = labels[event.packageName] ?: event.packageName.substringAfterLast('.'),
                            color = Color.White.copy(alpha = 0.85f),
                            fontFamily = fontFamily,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ago(event.atMillis),
                            color = accent.copy(alpha = 0.85f),
                            fontFamily = fontFamily,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Device monitor
// ---------------------------------------------------------------------------------------------

@Composable
fun DeviceMonitorWidget(
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val context = LocalContext.current
    val accent = config.accentColor()
    var memory by remember { mutableStateOf(readMemory(context)) }
    var storage by remember { mutableStateOf(readStorage()) }
    var uptime by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    LaunchedEffect(Unit) {
        while (true) {
            memory = readMemory(context)
            storage = readStorage()
            uptime = SystemClock.elapsedRealtime()
            delay(5_000L)
        }
    }

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "DEVICE",
                color = accent,
                fontFamily = fontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            MonitorRow("RAM", "${gb(memory.first)} / ${gb(memory.second)} GB", fontFamily)
            MonitorRow("Storage", "${gb(storage.first)} / ${gb(storage.second)} GB", fontFamily)
            val hours = uptime / 3_600_000L
            MonitorRow("Uptime", "${hours}h ${(uptime % 3_600_000L) / 60_000L}m", fontFamily)
        }
    }
}

@Composable
private fun MonitorRow(label: String, value: String, fontFamily: FontFamily) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.65f), fontFamily = fontFamily, fontSize = 10.sp, maxLines = 1)
        Text(text = value, color = Color.White, fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

// ---------------------------------------------------------------------------------------------
// Interactive widget stack (tap to cycle)
// ---------------------------------------------------------------------------------------------

@Composable
fun WidgetStack(
    viewModel: LauncherViewModel,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier
) {
    var index by remember { mutableIntStateOf(0) }
    val accent = config.accentColor()
    val pages = 6
    val clockFont = remember(viewModel.typeRevision, viewModel.typeMap) {
        viewModel.typeFamily(TypeTargets.CLOCK)
    }
    val homeFont = remember(viewModel.typeRevision, viewModel.typeMap) {
        viewModel.typeFamily(TypeTargets.HOME)
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { index = (index + 1) % pages }
                    )
                }
        ) {
            when (index) {
                0 -> ClockWidget(config, Modifier.fillMaxSize(), clockFont)
                1 -> WeatherWidget(viewModel, config, Modifier.fillMaxSize(), homeFont)
                2 -> MusicWidget(
                    config = config,
                    onEnableAccess = { viewModel.isThemeEngineOpen = true },
                    modifier = Modifier.fillMaxSize()
                )
                3 -> TimerWidget(viewModel, config, Modifier.fillMaxSize(), homeFont)
                4 -> WorldClockWidget(config, Modifier.fillMaxSize(), homeFont)
                else -> CountdownWidget(viewModel, config, Modifier.fillMaxSize(), homeFont)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(if (i == index) 6.dp else 4.dp)
                        .background(
                            if (i == index) accent else Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Live activity island
// ---------------------------------------------------------------------------------------------

@Composable
fun LiveActivityIsland(viewModel: LauncherViewModel, config: LiquidThemeConfig) {
    val media by NotificationHub.media
    val accent = config.accentColor()
    val controller = NotificationHub.controller
    val m = media
    val font = remember(viewModel.typeRevision, viewModel.typeMap) {
        viewModel.typeFamily(TypeTargets.MUSIC)
    }

    if (m == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 44.dp, vertical = 2.dp)
    ) {
        LiquidGlassSurface(
            config = config,
            cornerRadius = 40.dp,
            modifier = Modifier.fillMaxWidth().height(38.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (m.playing) accent else Color.White.copy(alpha = 0.5f), CircleShape)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = m.title,
                        color = Color.White,
                        fontFamily = font,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = m.artist,
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = font,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = { controller?.transportControls?.skipToPrevious() },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Rounded.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = {
                        val controls = controller?.transportControls
                        if (m.playing) controls?.pause() else controls?.play()
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = if (m.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (m.playing) "Pause" else "Play",
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { controller?.transportControls?.skipToNext() },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Rounded.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Edge lighting (charging / active)
// ---------------------------------------------------------------------------------------------

@Composable
fun EdgeGlow(config: LiquidThemeConfig, active: Boolean) {
    if (!active) return
    val accent = config.accentColor()
    val second = config.secondaryColor()
    val clock = LocalAnimClock.current
    val t = clock.value

    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 10.dp.toPx()
        val inset = stroke / 2f
        val size = Size(size.width - stroke, size.height - stroke)

        when (config.edgeLightingPattern) {
            EdgePattern.Sweep -> {
                val pulse = (sin(t * 1.5f) + 1f) / 2f
                val alpha = 0.25f + 0.6f * pulse
                drawRoundRect(
                    brush = Brush.sweepGradient(listOf(accent, second, accent)),
                    topLeft = Offset(inset, inset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                    alpha = alpha,
                    style = Stroke(width = stroke)
                )
            }
            EdgePattern.Breathe -> {
                val pulse = (sin(t * 0.8f) + 1f) / 2f
                val alpha = 0.3f + 0.5f * pulse
                drawRoundRect(
                    brush = Brush.sweepGradient(listOf(accent.copy(alpha = 0.6f), second.copy(alpha = 0.6f), accent.copy(alpha = 0.6f))),
                    topLeft = Offset(inset, inset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                    alpha = alpha,
                    style = Stroke(width = stroke)
                )
            }
            EdgePattern.Pulse -> {
                val pulse = (sin(t * 4f).coerceIn(0f, 1f))
                val alpha = 0.15f + 0.7f * pulse
                drawRoundRect(
                    brush = Brush.sweepGradient(listOf(accent, second, accent)),
                    topLeft = Offset(inset, inset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                    alpha = alpha,
                    style = Stroke(width = stroke)
                )
            }
            EdgePattern.Rainbow -> {
                val hue = (t * 30f) % 360f
                val c1 = Color.hsv(hue, 0.8f, 1f)
                val c2 = Color.hsv((hue + 120f) % 360f, 0.8f, 1f)
                val c3 = Color.hsv((hue + 240f) % 360f, 0.8f, 1f)
                drawRoundRect(
                    brush = Brush.sweepGradient(listOf(c1, c2, c3, c1)),
                    topLeft = Offset(inset, inset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                    alpha = 0.7f,
                    style = Stroke(width = stroke)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Floating shortcut bubble
// ---------------------------------------------------------------------------------------------

@Composable
fun FloatingShortcutBubble(viewModel: LauncherViewModel, config: LiquidThemeConfig) {
    val accent = config.accentColor()
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 150.dp)
                .size(52.dp)
                .graphicsLayerOffset(offsetX, offsetY)
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        offsetX += drag.x
                        offsetY += drag.y
                    }
                }
        ) {
            LiquidGlassSurface(
                config = config,
                cornerRadius = 26.dp,
                tintOverride = accent,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { viewModel.openDrawer() },
                                onLongPress = { viewModel.toggleEditMode() }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✳",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** Small helper so the bubble can be moved without re-measuring. */
private fun Modifier.graphicsLayerOffset(x: Float, y: Float): Modifier =
    this.then(
        Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }
    )
