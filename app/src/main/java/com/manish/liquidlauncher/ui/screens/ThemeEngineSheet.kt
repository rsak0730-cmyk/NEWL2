package com.manish.liquidlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Wallpaper
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.features.FeatureStudioActivity
import com.manish.liquidlauncher.model.DockStyle
import com.manish.liquidlauncher.model.GridPresets
import com.manish.liquidlauncher.model.IconShapeStyle
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.model.TypeTargets
import com.manish.liquidlauncher.notifications.NotificationHub
import com.manish.liquidlauncher.model.ThemePresets
import com.manish.liquidlauncher.ui.components.GlassButton
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.popIn
import com.manish.liquidlauncher.ui.components.LiquidSwitch
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.ui.components.jellyTouch
import com.manish.liquidlauncher.viewmodel.LauncherViewModel
import kotlin.math.abs

@Composable
fun ThemeEngineSheet(
    viewModel: LauncherViewModel,
    onPickWallpaper: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val context = LocalContext.current
    val alertsOn by NotificationHub.connected
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.8f).dp

    AnimatedVisibility(
        visible = viewModel.isThemeEngineOpen,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.isThemeEngineOpen = false }
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.popIn()) {
                LiquidGlassSurface(
                    config = config,
                    cornerRadius = 30.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Liquid Theme Engine",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(onClick = { viewModel.updateTheme(LiquidThemeConfig()) }) {
                                    Icon(Icons.Rounded.Refresh, "Reset", tint = accent)
                                }
                                IconButton(onClick = { viewModel.isThemeEngineOpen = false }) {
                                    Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                                }
                            }
                        }

                        Label("Presets")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ThemePresets.forEach { p ->
                                val selected = abs(config.accentHue - p.hue) < 1f &&
                                    abs(config.secondaryHue - p.secondaryHue) < 1f
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .jellyTouch(haptics = config.haptics, pressX = 0.85f, pressY = 0.85f) {
                                                viewModel.updateTheme(
                                                    config.copy(
                                                        accentHue = p.hue,
                                                        secondaryHue = p.secondaryHue,
                                                        refractionOpacity = p.density,
                                                        glowIntensity = p.glow
                                                    )
                                                )
                                            }
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color.hsv(p.hue, 0.8f, 1f),
                                                        Color.hsv(p.secondaryHue, 0.8f, 0.9f)
                                                    )
                                                )
                                            )
                                            .border(
                                                if (selected) 2.5.dp else 1.dp,
                                                Color.White.copy(alpha = if (selected) 1f else 0.3f),
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        text = p.name,
                                        color = Color.White.copy(alpha = if (selected) 1f else 0.65f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        ThemeSlider("Accent Hue (${config.accentHue.toInt()}°)", config.accentHue, 0f..360f, accent) {
                            viewModel.updateTheme(config.copy(accentHue = it))
                        }
                        ThemeSlider("Secondary Hue (${config.secondaryHue.toInt()}°)", config.secondaryHue, 0f..360f, accent) {
                            viewModel.updateTheme(config.copy(secondaryHue = it))
                        }
                        ThemeSlider("Glass Refraction Density", config.refractionOpacity, 0.08f..0.48f, accent) {
                            viewModel.updateTheme(config.copy(refractionOpacity = it))
                        }
                        ThemeSlider("Specular Glow Intensity", config.glowIntensity, 0.2f..1.0f, accent) {
                            viewModel.updateTheme(config.copy(glowIntensity = it))
                        }
                        ThemeSlider("Liquid Squircle Radius", config.cornerRadiusDp, 12f..34f, accent) {
                            viewModel.updateTheme(config.copy(cornerRadiusDp = it))
                        }
                        ThemeSlider("Icon Size", config.iconScale, 0.8f..1.2f, accent) {
                            viewModel.updateTheme(config.copy(iconScale = it))
                        }

                        Label("Icon shape")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconShapeStyle.values().forEach { style ->
                                GlassButton(
                                    text = style.label,
                                    config = config,
                                    modifier = Modifier.weight(1f),
                                    tint = if (config.iconShape == style) accent else null
                                ) { viewModel.updateTheme(config.copy(iconShape = style)) }
                            }
                        }

                        Label("Workspace")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DockStyle.values().forEach { style ->
                                GlassButton(
                                    text = style.label,
                                    config = config,
                                    tint = if (config.dockStyle == style) accent else null
                                ) { viewModel.updateTheme(config.copy(dockStyle = style)) }
                            }
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GridPresets.forEach { preset ->
                                val selected = config.gridColumns == preset.columns && config.gridRows == preset.maxRows
                                GlassButton(
                                    text = preset.label,
                                    config = config,
                                    tint = if (selected) accent else null
                                ) {
                                    viewModel.updateTheme(
                                        config.copy(gridColumns = preset.columns, gridRows = preset.maxRows)
                                    )
                                }
                            }
                        }
                        ToggleRow("One-handed mode", config.oneHanded, accent) {
                            viewModel.updateTheme(config.copy(oneHanded = it))
                        }
                        ToggleRow("Edit mode (drag to rearrange)", viewModel.editMode, accent) {
                            viewModel.toggleEditMode()
                        }
                        ToggleRow("Live activity island", config.activityIsland, accent) {
                            viewModel.updateTheme(config.copy(activityIsland = it))
                        }
                        ToggleRow("Edge lighting while charging", config.edgeLighting, accent) {
                            viewModel.updateTheme(config.copy(edgeLighting = it))
                        }
                        ToggleRow("Floating shortcut bubble", config.floatingBubbles, accent) {
                            viewModel.updateTheme(config.copy(floatingBubbles = it))
                        }

                        Label("Effects")
                        ToggleRow("Live animations", config.liveAnimations, accent) {
                            viewModel.updateTheme(config.copy(liveAnimations = it))
                        }
                        ToggleRow("Floating bubbles", config.bubbles, accent) {
                            viewModel.updateTheme(config.copy(bubbles = it))
                        }
                        ToggleRow("Icon bobbing", config.iconBob, accent) {
                            viewModel.updateTheme(config.copy(iconBob = it))
                        }
                        ToggleRow("Tilt glare & parallax", config.tiltEffects, accent) {
                            viewModel.updateTheme(config.copy(tiltEffects = it))
                        }
                        ToggleRow("Icon labels", config.showLabels, accent) {
                            viewModel.updateTheme(config.copy(showLabels = it))
                        }
                        ToggleRow("Haptic feedback", config.haptics, accent) {
                            viewModel.updateTheme(config.copy(haptics = it))
                        }
                        ToggleRow("Blur behind drawer", config.blurDrawer, accent) {
                            viewModel.updateTheme(config.copy(blurDrawer = it))
                        }
                        ToggleRow("Water-drop sounds", config.sounds, accent) {
                            viewModel.updateTheme(config.copy(sounds = it))
                        }
                        ToggleRow("Notification badges", config.badges, accent) {
                            viewModel.updateTheme(config.copy(badges = it))
                        }

                        Label("Wallpaper & system")
                        GlassButton("Choose wallpaper", config, Modifier.fillMaxWidth(), Icons.Rounded.Wallpaper, onClick = onPickWallpaper)
                        if (viewModel.customWallpaperUri != null) {
                            GlassButton("Use living wallpaper", config, Modifier.fillMaxWidth(), Icons.Rounded.Refresh) {
                                viewModel.setWallpaper(null)
                            }
                        }
                        if (viewModel.hiddenPackages.isNotEmpty()) {
                            GlassButton(
                                "Restore ${viewModel.hiddenPackages.size} hidden apps",
                                config, Modifier.fillMaxWidth(), Icons.Rounded.Visibility
                            ) { viewModel.restoreHiddenApps() }
                        }
                        if (viewModel.customWallpaperUri != null) {
                            GlassButton("Match theme to wallpaper", config, Modifier.fillMaxWidth(), Icons.Rounded.Palette) {
                                viewModel.matchThemeToWallpaper(context)
                            }
                        }
                        if (!alertsOn) {
                            GlassButton("Enable badges & music (notification access)", config, Modifier.fillMaxWidth(), Icons.Rounded.Notifications) {
                                viewModel.openSettingsScreen(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            }
                        }
                        GlassButton("Enable double-tap lock (Accessibility)", config, Modifier.fillMaxWidth(), Icons.Rounded.Lock) {
                            viewModel.openSettingsScreen(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        }
                        Label("Type, gestures & widgets")
                        GlassButton("Type & language studio", config, Modifier.fillMaxWidth(), Icons.Rounded.TextFields) {
                            viewModel.isThemeEngineOpen = false
                            viewModel.openTypeStudio(TypeTargets.HOME)
                        }
                        GlassButton("Gesture studio & app pairs", config, Modifier.fillMaxWidth(), Icons.Rounded.Gesture) {
                            viewModel.isThemeEngineOpen = false
                            viewModel.gestureStudioOpen = true
                        }
                        GlassButton("Private space", config, Modifier.fillMaxWidth(), Icons.Rounded.Lock) {
                            viewModel.isThemeEngineOpen = false
                            viewModel.openPrivateSpace()
                        }
                        GlassButton("3D app carousel", config, Modifier.fillMaxWidth(), Icons.Rounded.Dashboard) {
                            viewModel.isThemeEngineOpen = false
                            viewModel.carouselOpen = true
                        }
                        GlassButton("Multi-device hub", config, Modifier.fillMaxWidth(), Icons.Rounded.Public) {
                            viewModel.isThemeEngineOpen = false
                            viewModel.deviceHubOpen = true
                        }
                        GlassButton("Feature Studio", config, Modifier.fillMaxWidth(), Icons.Rounded.Dashboard) {
                            viewModel.isThemeEngineOpen = false
                            context.startActivity(Intent(context, FeatureStudioActivity::class.java))
                        }

                        Label("Widgets")
                        var cityInput by remember(viewModel.weatherCity) { mutableStateOf(viewModel.weatherCity) }
                        LiquidGlassSurface(
                            config = config,
                            cornerRadius = 16.dp,
                            sheen = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.TextField(
                                value = cityInput,
                                onValueChange = { cityInput = it },
                                singleLine = true,
                                placeholder = {
                                    Text("Weather city", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                                },
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassButton("Save city", config, Modifier.weight(1f)) {
                                viewModel.updateWeatherCity(cityInput)
                            }
                            GlassButton("Refresh", config, Modifier.weight(1f), Icons.Rounded.Refresh) {
                                viewModel.refreshWeather()
                            }
                        }
                        var countdownDays by remember { mutableStateOf("30") }
                        var countdownName by remember(viewModel.countdownLabel) {
                            mutableStateOf(viewModel.countdownLabel)
                        }
                        LiquidGlassSurface(
                            config = config,
                            cornerRadius = 16.dp,
                            sheen = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.TextField(
                                value = countdownName,
                                onValueChange = { countdownName = it.take(24) },
                                singleLine = true,
                                placeholder = {
                                    Text("Countdown label", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                                },
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassButton("In 7 days", config, Modifier.weight(1f)) {
                                viewModel.setCountdown(countdownName, System.currentTimeMillis() + 7L * 86_400_000L)
                            }
                            GlassButton("In 30 days", config, Modifier.weight(1f)) {
                                viewModel.setCountdown(countdownName, System.currentTimeMillis() + 30L * 86_400_000L)
                            }
                            GlassButton("Clear", config, Modifier.weight(1f)) {
                                viewModel.setCountdown(countdownName, 0L)
                            }
                        }

                        Label("Backup")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassButton("Export", config, Modifier.weight(1f), onClick = onExport)
                            GlassButton("Restore", config, Modifier.weight(1f), onClick = onImport)
                        }
                        GlassButton("Set as default launcher", config, Modifier.fillMaxWidth(), Icons.Rounded.SwapHoriz) {
                            viewModel.openDefaultLauncherPicker(context)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        LiquidSwitch(checked = checked, accent = accent, onCheckedChange = onChange)
    }
}

@Composable
private fun ThemeSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    accent: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}
