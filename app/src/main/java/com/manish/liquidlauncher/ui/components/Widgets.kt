package com.manish.liquidlauncher.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.manish.liquidlauncher.data.LauncherPrefs
import com.manish.liquidlauncher.model.LiquidThemeConfig
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.sin

// ---------------------------------------------------------------------------------------------
// Live data helpers
// ---------------------------------------------------------------------------------------------

data class BatteryInfo(val level: Int, val charging: Boolean)

private fun readBattery(intent: Intent?): BatteryInfo {
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val plugged = (intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0
    val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 85
    return BatteryInfo(pct.coerceIn(0, 100), plugged)
}

/** Live battery state (updates through the sticky ACTION_BATTERY_CHANGED broadcast). */
@Composable
fun rememberBatteryInfo(): State<BatteryInfo> {
    val context = LocalContext.current
    val state = remember {
        mutableStateOf(
            readBattery(context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)))
        )
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                state.value = readBattery(intent)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }
    return state
}

@Composable
private fun rememberNowMillis(): State<Long> {
    val now = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now.longValue = System.currentTimeMillis()
            delay((1000L - now.longValue % 1000L).coerceAtLeast(30L))
        }
    }
    return now
}

// ---------------------------------------------------------------------------------------------
// Clock widget: rolling digits, blinking colon, seconds ring, gradient numerals
// ---------------------------------------------------------------------------------------------

@Composable
fun ClockWidget(
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val context = LocalContext.current
    val accent = config.accentColor()
    val second = config.secondaryColor()
    val now by rememberNowMillis()
    val is24 = remember { DateFormat.is24HourFormat(context) }
    val timeFmt = remember(is24) { SimpleDateFormat(if (is24) "HH:mm" else "hh:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val ampmFmt = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    val date = Date(now)
    val timeText = timeFmt.format(date)
    val dateText = dateFmt.format(date)
    val hour = calendar.apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
    val seconds = ((now / 1000L) % 60L).toInt()
    val colonAlpha by animateFloatAsState(
        targetValue = if (seconds % 2 == 0) 1f else 0.25f,
        animationSpec = tween(450),
        label = "colon"
    )

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateText,
                        color = accent,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = greeting,
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
                // Seconds ring
                Canvas(modifier = Modifier.size(24.dp)) {
                    val stroke = 2.5.dp.toPx()
                    val inset = stroke / 2f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f),
                        radius = arcSize.width / 2f,
                        style = Stroke(width = stroke)
                    )
                    rotate(-90f) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(second, accent, second)),
                            startAngle = 0f,
                            sweepAngle = (seconds + 1) / 60f * 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            // The numerals are sized from the space the card actually has, so a long
            // time string (12:30) can never be cut in half on a narrow card or a big
            // system font scale. No fixed lineHeight, no clipping animation.
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.BottomStart
            ) {
                val hoursText = if (is24) timeText else timeText.substringBefore(':')
                val minutesText = timeText.substringAfter(':')
                // +1.6 leaves room for the AM/PM tag when the phone is in 12-hour mode
                val chars = timeText.length.coerceAtLeast(4) + if (is24) 0f else 1.6f
                val byWidth = maxWidth.value / (chars * 0.60f)
                val byHeight = maxHeight.value * 0.72f
                val numeral = min(byWidth, byHeight).coerceIn(13f, 36f)
                val numeralStyle = TextStyle(
                    brush = Brush.verticalGradient(listOf(Color.White, Color.White, accent.copy(alpha = 0.85f))),
                    fontSize = numeral.sp,
                    lineHeight = (numeral * 1.16f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    letterSpacing = (-0.5).sp,
                    fontFeatureSettings = "tnum"
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = hoursText,
                        style = numeralStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = ":",
                        style = numeralStyle,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.graphicsLayer { alpha = colonAlpha }
                    )
                    Text(
                        text = minutesText,
                        style = numeralStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (!is24) {
                        Text(
                            text = ampmFmt.format(date),
                            color = accent,
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, bottom = (numeral * 0.16f).dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Battery widget: liquid fill with double sine waves
// ---------------------------------------------------------------------------------------------

@Composable
fun BatteryWidget(
    config: LiquidThemeConfig,
    battery: BatteryInfo,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val accent = config.accentColor()
    val second = config.secondaryColor()
    val clock = LocalAnimClock.current
    val fill = animateFloatAsState(
        targetValue = battery.level / 100f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessVeryLow),
        label = "batteryFill"
    )
    val pulse = rememberInfiniteTransition(label = "boltPulse").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "boltAlpha"
    )

    LiquidGlassSurface(config = config, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = clock.value
            val level = fill.value.coerceIn(0f, 1f)
            val top = h * (1f - level)
            val speed = if (battery.charging) 3.2f else 1.6f
            val step = 6.dp.toPx()

            fun wave(amp: Float, k: Float, phase: Float, brush: Brush) {
                val path = Path()
                path.moveTo(0f, h)
                path.lineTo(0f, top + amp * sin(phase + t * speed))
                var x = 0f
                while (x <= w + step) {
                    path.lineTo(x, top + amp * sin(x * k + t * speed + phase))
                    x += step
                }
                path.lineTo(w, h)
                path.close()
                drawPath(path, brush)
            }

            wave(
                amp = 5.dp.toPx(), k = 0.045f, phase = 1.7f,
                brush = Brush.verticalGradient(
                    listOf(second.copy(alpha = 0.30f), second.copy(alpha = 0.10f)),
                    startY = top, endY = h
                )
            )
            wave(
                amp = 4.dp.toPx(), k = 0.06f, phase = 0f,
                brush = Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.60f), accent.copy(alpha = 0.18f)),
                    startY = top, endY = h
                )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = "Battery",
                tint = if (battery.charging) Color(0xFFFFE082) else Color.White,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { alpha = if (battery.charging) pulse.value else 1f }
            )
            Text(
                text = "${battery.level}%",
                color = Color.White,
                fontFamily = fontFamily,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (battery.charging) "CHARGING" else "FLUID CELL",
                color = Color.White.copy(alpha = 0.75f),
                fontFamily = fontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Dashboard widgets
// ---------------------------------------------------------------------------------------------

@Composable
fun GaugeCard(
    title: String,
    detail: String,
    fraction: Float,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier
) {
    val accent = config.accentColor()
    val second = config.secondaryColor()
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessVeryLow),
        label = "gauge"
    )

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(78.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 8.dp.toPx()
                    val inset = stroke / 2f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = Color.White.copy(alpha = 0.12f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    rotate(-90f) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(second, accent, second)),
                            startAngle = 0f,
                            sweepAngle = animated * 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
                Text(
                    text = "${(animated * 100).toInt()}%",
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = detail,
                color = accent,
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickTile(
    icon: ImageVector,
    label: String,
    active: Boolean,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    activeTint: Color = config.accentColor(),
    onClick: () -> Unit
) {
    LiquidGlassSurface(
        config = config,
        cornerRadius = 22.dp,
        tintOverride = if (active) activeTint else null,
        modifier = modifier.jellyTouch(haptics = config.haptics, pressX = 0.92f, pressY = 0.88f, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeTint else Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = if (active) 1f else 0.8f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Calendar agenda widget: next 3 events from the system calendar. */
@Composable
fun CalendarWidget(
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val context = LocalContext.current
    val accent = config.accentColor()
    val events by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        val list = mutableListOf<String>()
        try {
            val resolver = context.contentResolver
            val now = System.currentTimeMillis()
            val uri = android.provider.CalendarContract.Events.CONTENT_URI
            val projection = arrayOf(
                android.provider.CalendarContract.Events.TITLE,
                android.provider.CalendarContract.Events.DTSTART
            )
            val selection = "${android.provider.CalendarContract.Events.DTSTART} > ?"
            val selectionArgs = arrayOf(now.toString())
            val sortOrder = "${android.provider.CalendarContract.Events.DTSTART} ASC"
            resolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                var count = 0
                while (cursor.moveToNext() && count < 3) {
                    val title = cursor.getString(0)
                    val start = cursor.getLong(1)
                    val df = java.text.SimpleDateFormat("EEE h:mm a", java.util.Locale.getDefault())
                    list.add("${df.format(java.util.Date(start))} — $title")
                    count++
                }
            }
        } catch (_: Exception) {
            // Calendar permission not granted or no events
        }
        events.value = list
    }

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Event,
                contentDescription = "Calendar",
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            if (events.isEmpty()) {
                Text(
                    text = "No upcoming events",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                events.forEach { e ->
                    Text(
                        text = e,
                        color = Color.White,
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Air quality widget: AQI and pollutant readout from Open-Meteo (no API key). */
@Composable
fun AirQualityWidget(
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif
) {
    val context = LocalContext.current
    val accent = config.accentColor()
    val scope = rememberCoroutineScope()
    val aqi by remember { mutableStateOf<Int?>(null) }
    val loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val city = LauncherPrefs(context).weatherCity
        if (city.isNullOrBlank()) {
            loaded.value = true
            return@LaunchedEffect
        }
        scope.launch {
            try {
                // Real implementation would geocode the city, then call Open-Meteo AQI API.
                // Placeholder structure ready for coordinates.
                val url = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=0&longitude=0&hourly=european_aqi&timezone=auto"
                // URL connection would go here; kept as a scaffold.
                aqi.value = null
            } catch (_: Exception) {
                // ignore
            }
            loaded.value = true
        }
    }

    LiquidGlassSurface(config = config, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Cloud,
                contentDescription = "Air quality",
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            if (!loaded) {
                Text(
                    text = "Loading…",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else if (aqi == null) {
                Text(
                    text = "Set a city in Theme Engine",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                Text(
                    text = "AQI ${aqi.value}",
                    color = Color.White,
                    fontFamily = fontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (aqi.value < 50) "Good" else if (aqi.value < 100) "Moderate" else "Unhealthy",
                    color = accent,
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, accent: Color, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = accent,
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
        modifier = modifier
    )
}
