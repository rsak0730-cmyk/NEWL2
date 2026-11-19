package com.manish.liquidlauncher.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/** Icon bitmap + dominant tint are pre-computed once on a background thread (no per-frame work). */
@Immutable
data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap,
    val tint: Color
)

@Immutable
data class FolderState(
    val title: String,
    val apps: List<AppInfo>
)

@Immutable
data class LiquidThemeConfig(
    val accentHue: Float = 195f,          // Primary hue (Deep Ocean Cyan)
    val secondaryHue: Float = 245f,       // Secondary hue used by the living wallpaper
    val refractionOpacity: Float = 0.22f, // Glass surface density
    val glowIntensity: Float = 0.65f,     // Specular edge highlight
    val cornerRadiusDp: Float = 26f,      // Liquid squircle curvature
    val iconScale: Float = 1f,            // Icon size multiplier
    val liveAnimations: Boolean = true,   // Living wallpaper, sheen, waves
    val bubbles: Boolean = true,          // Floating bubbles
    val iconBob: Boolean = true,          // Idle floating motion for icons
    val tiltEffects: Boolean = true,      // Gyro-driven glare + parallax
    val showLabels: Boolean = true,
    val haptics: Boolean = true,
    val blurDrawer: Boolean = true,       // Blur the home screen behind the drawer (Android 12+)
    val iconShape: IconShapeStyle = IconShapeStyle.Squircle,
    val sounds: Boolean = true,           // Soft water-drop sounds
    val badges: Boolean = true,           // Notification count bubbles (needs notification access)
    val dockStyle: DockStyle = DockStyle.GlassPill,
    val gridColumns: Int = 4,             // 3..5 (Grid presets)
    val gridRows: Int = 6,                // maximum rows on a page
    val oneHanded: Boolean = false,       // Shrink the workspace toward the bottom half
    val activityIsland: Boolean = true,   // Live activity island at the top edge
    val edgeLighting: Boolean = true,     // Charging / notification edge glow
    val gestureTrail: Boolean = false,    // Liquid trail that follows the finger
    val floatingBubbles: Boolean = false  // Draggable shortcut bubble
)

enum class IconShapeStyle(val label: String) {
    Squircle("Squircle"),
    Circle("Circle"),
    Teardrop("Drop"),
    Square("Square")
}

/** User-made home folder (apps referenced by package name). */
@Immutable
data class CustomFolder(
    val name: String,
    val packages: List<String>
)

data class ThemePreset(
    val name: String,
    val hue: Float,
    val secondaryHue: Float,
    val density: Float,
    val glow: Float
)

val ThemePresets = listOf(
    ThemePreset("Ocean", 195f, 245f, 0.22f, 0.65f),
    ThemePreset("Aurora", 150f, 285f, 0.20f, 0.75f),
    ThemePreset("Sunset", 18f, 335f, 0.24f, 0.70f),
    ThemePreset("Neon", 305f, 185f, 0.18f, 0.95f),
    ThemePreset("Toxic", 105f, 170f, 0.20f, 0.85f),
    ThemePreset("Ruby", 352f, 28f, 0.26f, 0.70f),
    ThemePreset("Ice", 205f, 175f, 0.30f, 0.55f)
)
