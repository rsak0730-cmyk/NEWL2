package com.manish.liquidlauncher.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.notifications.NotificationHub
import com.manish.liquidlauncher.ui.components.LiquidWallpaper
import com.manish.liquidlauncher.ui.components.LocalAnimClock
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.ui.components.rememberBatteryInfo
import com.manish.liquidlauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/** Always-on style night clock: slow liquid background, drifting clock (anti burn-in), tap to exit. */
@Composable
fun IdleScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val view = LocalView.current
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val clock = LocalAnimClock.current
    val battery by rememberBatteryInfo()
    val media by NotificationHub.media
    val is24 = remember { DateFormat.is24HourFormat(context) }
    val timeFmt = remember(is24) { SimpleDateFormat(if (is24) "HH:mm" else "h:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    val now = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val idleFont = remember(viewModel.typeRevision, viewModel.typeMap) {
        viewModel.typeFamily(com.manish.liquidlauncher.model.TypeTargets.CLOCK)
    }

    LaunchedEffect(viewModel.idleOpen) {
        if (!viewModel.idleOpen) return@LaunchedEffect
        while (true) {
            now.longValue = System.currentTimeMillis()
            delay(1000L)
        }
    }
    DisposableEffect(viewModel.idleOpen) {
        view.keepScreenOn = viewModel.idleOpen
        onDispose { view.keepScreenOn = false }
    }

    AnimatedVisibility(
        visible = viewModel.idleOpen,
        enter = fadeIn(tween(600)),
        exit = fadeOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) { detectTapGestures { viewModel.idleOpen = false } }
        ) {
            LiquidWallpaper(
                config = config,
                imageUri = null,
                pageOffset = { 0f },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.5f }
            )
            val date = Date(now.longValue)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = sin(clock.value * 0.05f) * 36f
                        translationY = cos(clock.value * 0.04f) * 36f
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sized from the screen width so a long time string never clips,
                // whatever the display size or font scale.
                val idleTimeSize = (LocalConfiguration.current.screenWidthDp * 0.20f)
                    .coerceIn(34f, 96f)
                BasicText(
                    text = timeFmt.format(date),
                    style = TextStyle(
                        brush = Brush.verticalGradient(listOf(Color.White, accent)),
                        fontSize = idleTimeSize.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = idleFont,
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1
                )
                Text(
                    text = dateFmt.format(date),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = (if (battery.charging) "Charging  " else "Battery  ") + "${battery.level}%",
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp)
                )
                val m = media
                if (m != null) {
                    Text(
                        text = "${m.title} - ${m.artist}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = "Tap anywhere to exit",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
            )
        }
    }
}
