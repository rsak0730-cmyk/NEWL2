package com.manish.liquidlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.ThemeMode

private val ClayWhite = Color(0xFFF1F2F6)
private val ClaySurfaceDark = Color(0xFF0A101B)
private val ClaySurfaceElevatedDark = Color(0xFF0F1827)
private val ClayTextDark = Color(0xFFF1F2F6)
private val ClayMutedDark = Color(0xFFB9C2D8)
private val ClayBlue = Color(0xFF4FC3D7)
private val ClayPurple = Color(0xFF9A8BEA)

// Light glass scheme (mirrors the dark one on a light base)
private val ClaySurfaceLight = Color(0xFFF7F8FC)
private val ClaySurfaceElevatedLight = Color(0xFFE8EAF2)
private val ClayTextLight = Color(0xFF343849)
private val ClayMutedLight = Color(0xFF858B9C)

private val LiquidDarkScheme = darkColorScheme(
    primary = ClayBlue,
    onPrimary = Color.White,
    secondary = ClayPurple,
    onSecondary = Color.White,
    tertiary = Color(0xFF75C8C1),
    background = Color(0xFF030712),
    onBackground = ClayTextDark,
    surface = ClaySurfaceDark,
    onSurface = ClayTextDark,
    surfaceVariant = ClaySurfaceElevatedDark,
    onSurfaceVariant = ClayMutedDark,
    outline = Color(0xFF1B2535)
)

private val LiquidLightScheme = lightColorScheme(
    primary = ClayBlue,
    onPrimary = Color.White,
    secondary = ClayPurple,
    onSecondary = Color.White,
    tertiary = Color(0xFF75C8C1),
    background = ClayWhite,
    onBackground = ClayTextLight,
    surface = ClaySurfaceLight,
    onSurface = ClayTextLight,
    surfaceVariant = ClaySurfaceElevatedLight,
    onSurfaceVariant = ClayMutedLight,
    outline = Color(0xFFD5D9E5)
)

val LiquidTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        letterSpacing = (-0.5).sp,
        color = ClayTextDark
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = ClayTextDark
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = ClayTextDark
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = ClayTextDark
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = ClayTextDark
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.2.sp,
        color = ClayMutedDark
    )
)

@Composable
fun LiquidGlassTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDark) LiquidDarkScheme else LiquidLightScheme,
        typography = LiquidTypography,
        content = content
    )
}

/** Resolves the effective dark/light mode from the stored theme mode. */
fun ThemeMode.isDarkMode(context: android.content.Context): Boolean = when (this) {
    ThemeMode.Dark -> true
    ThemeMode.Light -> false
    ThemeMode.System -> {
        val uiMode = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}