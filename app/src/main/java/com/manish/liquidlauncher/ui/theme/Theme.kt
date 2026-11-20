package com.manish.liquidlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ClayWhite = Color(0xFFF1F2F6)
private val ClaySurface = Color(0xFFF7F8FC)
private val ClayText = Color(0xFF343849)
private val ClayMuted = Color(0xFF858B9C)
private val ClayBlue = Color(0xFF7199E8)
private val ClayPurple = Color(0xFF9A8BEA)

private val LiquidDarkScheme = lightColorScheme(
    primary = ClayBlue,
    onPrimary = Color.White,
    secondary = ClayPurple,
    onSecondary = Color.White,
    tertiary = Color(0xFF75C8C1),
    background = ClayWhite,
    onBackground = ClayText,
    surface = ClaySurface,
    onSurface = ClayText,
    surfaceVariant = Color(0xFFE8EAF2),
    onSurfaceVariant = ClayMuted,
    outline = Color(0xFFD5D9E5)
)

val LiquidTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        letterSpacing = (-0.5).sp,
        color = ClayText
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = ClayText
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = ClayText
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = ClayText
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = ClayText
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.2.sp,
        color = ClayMuted
    )
)

@Composable
fun LiquidGlassTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LiquidDarkScheme,
        typography = LiquidTypography,
        content = content
    )
}