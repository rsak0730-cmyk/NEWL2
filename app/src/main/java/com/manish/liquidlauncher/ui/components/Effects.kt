package com.manish.liquidlauncher.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.manish.liquidlauncher.model.LiquidThemeConfig
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/** Shared, frame-synced time (seconds). Read it ONLY inside draw / graphicsLayer lambdas. */
val LocalAnimClock = staticCompositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }

/** Smoothed device tilt in -1..1 (x = left/right, y = up/down). Read only inside draw lambdas. */
val LocalTilt = staticCompositionLocalOf<State<Offset>> { mutableStateOf(Offset.Zero) }

/** True when the device requests reduced rendering work (for example, Battery Saver). */
val LocalReducedEffects = staticCompositionLocalOf { false }

@Composable
fun rememberPowerSaveMode(): State<Boolean> {
    val context = LocalContext.current
    val state = remember {
        mutableStateOf(
            (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isPowerSaveMode == true
        )
    }
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: android.content.Intent?) {
                state.value =
                    (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isPowerSaveMode == true
            }
        }
        val filter = android.content.IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: IllegalArgumentException) { }
        }
    }
    return state
}

@Composable
fun rememberAnimClock(running: Boolean): State<Float> {
    val time = remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val powerSave by rememberPowerSaveMode()
    var foreground by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            foreground = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val shouldRun = running && foreground
    LaunchedEffect(shouldRun, powerSave) {
        if (!shouldRun) return@LaunchedEffect
        var lastUpdate = withFrameNanos { it }
        // Keep the frame clock synchronized, but avoid invalidating the whole wallpaper
        // composition at the display's full refresh rate. Battery Saver uses a gentler rate.
        val minimumFrameNanos = if (powerSave) 50_000_000L else 33_333_333L
        while (true) {
            val now = withFrameNanos { it }
            val elapsed = now - lastUpdate
            if (elapsed >= minimumFrameNanos) {
                val dt = (elapsed / 1_000_000_000f).coerceIn(0f, 0.15f)
                time.floatValue = (time.floatValue + dt) % 100000f
                lastUpdate = now
            }
        }
    }
    return time
}

@Composable
fun rememberTilt(enabled: Boolean): State<Offset> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val tilt = remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(enabled, lifecycleOwner, context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var fx = 0f
        var fy = 0f
        var registered = false
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val nx = (-event.values[0] / 9.81f).coerceIn(-1f, 1f)
                val ny = (event.values[1] / 9.81f).coerceIn(-1f, 1f)
                fx += (nx - fx) * 0.08f
                fy += (ny - fy) * 0.08f
                tilt.value = Offset(fx, fy)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        fun updateRegistration() {
            val shouldRegister = enabled &&
                lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) &&
                manager != null && sensor != null
            if (shouldRegister && !registered) {
                registered = manager!!.registerListener(listener, sensor!!, SensorManager.SENSOR_DELAY_UI)
            } else if (!shouldRegister && registered) {
                manager?.unregisterListener(listener)
                registered = false
            }
            if (!enabled) tilt.value = Offset.Zero
        }
        val observer = LifecycleEventObserver { _, _ -> updateRegistration() }
        lifecycleOwner.lifecycle.addObserver(observer)
        updateRegistration()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (registered) manager?.unregisterListener(listener)
            tilt.value = Offset.Zero
        }
    }
    return tilt
}

private fun hash01(n: Int, salt: Int): Float {
    val x = sin(n * 127.1f + salt * 311.7f) * 43758.5453f
    return x - floor(x)
}

/**
 * Living wallpaper: drifting aurora blobs + tech dot-grid + scan line + rising bubbles,
 * with gyro/page parallax. Falls back to this when no custom image is set; a custom image
 * gets parallax + the same overlay for a consistent techy feel.
 */
@Composable
fun LiquidWallpaper(
    config: LiquidThemeConfig,
    imageUri: Uri?,
    pageOffset: () -> Float,
    modifier: Modifier = Modifier
) {
    val clock = LocalAnimClock.current
    val tilt = LocalTilt.current
    val reducedEffects = LocalReducedEffects.current
    val accent = config.accentColor()
    val second = config.secondaryColor()

    Box(modifier = modifier.background(Color(0xFF030712))) {
        // 1) Aurora / metaball blobs (always beneath, also acts as fallback if image fails to load)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = clock.value
            val parX = pageOffset() * w * -0.05f + tilt.value.x * w * 0.04f
            val parY = tilt.value.y * h * 0.025f

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF030712), Color(0xFF071427), Color(0xFF030712))
                )
            )
            for (i in 0 until 5) {
                val a = t * (0.07f + i * 0.023f) + i * 1.7f
                val cx = w * (0.5f + 0.42f * sin(a * 1.1f + i)) + parX * (0.6f + i * 0.35f)
                val cy = h * (0.5f + 0.38f * cos(a * 0.9f + i * 2.1f)) + parY * (1f + i * 0.4f)
                val r = w * (0.55f + 0.12f * sin(a * 2f))
                val c = lerp(accent, second, i / 4f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c.copy(alpha = 0.40f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                    blendMode = BlendMode.Screen
                )
            }
        }

        // 2) Optional custom wallpaper with parallax
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.14f
                        scaleY = 1.14f
                        translationX = -pageOffset() * size.width * 0.03f + tilt.value.x * size.width * 0.02f
                        translationY = tilt.value.y * size.height * 0.012f
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.38f),
                                Color.Black.copy(alpha = 0.14f),
                                Color.Black.copy(alpha = 0.52f)
                            )
                        )
                    )
            )
        }

        // 3) Tech dot grid (cached points, one draw call)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val step = 26.dp.toPx()
                    val pts = ArrayList<Offset>()
                    var y = step / 2f
                    while (y < size.height) {
                        var x = step / 2f
                        while (x < size.width) {
                            pts.add(Offset(x, y))
                            x += step
                        }
                        y += step
                    }
                    val dot = 2.2.dp.toPx()
                    onDrawBehind {
                        drawPoints(
                            points = pts,
                            pointMode = PointMode.Points,
                            color = Color.White.copy(alpha = if (imageUri != null) 0.05f else 0.075f),
                            strokeWidth = dot,
                            cap = StrokeCap.Round
                        )
                    }
                }
        )

        // 4) Scan line + bubbles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = clock.value

            if (config.liveAnimations) {
                val bandH = 110.dp.toPx()
                val y = ((t * 0.10f) % 1.5f - 0.2f) * h
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, accent.copy(alpha = 0.07f), Color.Transparent),
                        startY = y,
                        endY = y + bandH
                    ),
                    topLeft = Offset(0f, y),
                    size = Size(w, bandH)
                )
            }

            if (config.bubbles) {
                val bubbleCount = if (reducedEffects) 6 else 12
                for (i in 0 until bubbleCount) {
                    val radius = (4f + hash01(i, 1) * 15f).dp.toPx()
                    val speed = 0.018f + hash01(i, 2) * 0.03f
                    val progress = (t * speed + hash01(i, 3)) % 1f
                    val y = h * (1.08f - progress * 1.2f)
                    val x = w * hash01(i, 4) + sin(t * 0.8f + i) * 14.dp.toPx() +
                        tilt.value.x * w * 0.02f * (1f + hash01(i, 5))
                    val fade = sin(progress * PI.toFloat()).coerceIn(0f, 1f)
                    val center = Offset(x, y)

                    drawCircle(
                        color = accent.copy(alpha = 0.065f * fade),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.19f * fade),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 0.9.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.48f * fade),
                        radius = radius * 0.22f,
                        center = Offset(x - radius * 0.35f, y - radius * 0.35f)
                    )
                }
            }
        }
    }
}
