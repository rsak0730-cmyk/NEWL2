package com.manish.liquidlauncher.ui.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.model.ThemeMode
import com.manish.liquidlauncher.ui.theme.isDarkMode
import com.manish.liquidlauncher.ui.components.AnimationConfig
import kotlin.math.sin

/** Minimum touch target size per Android accessibility guidelines (48dp). */
private val MinTouchTarget = 48.dp

/**
 * Main liquid-glass surface used throughout the launcher.
 *
 * UX guidelines applied:
 * - Glass surface colors adapt to dark/light theme mode
 * - Subtle sheen sweep for visual depth
 * - Proper shadow elevation for layering
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    config: LiquidThemeConfig,
    cornerRadius: Dp = config.cornerRadiusDp.dp,
    tintOverride: Color? = null,
    sheen: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val accent = tintOverride ?: config.accentColor()
    val clock = LocalAnimClock.current
    val context = LocalContext.current
    val isDark = config.themeMode.isDarkMode(context)

    val pulse = (sin(clock.value * 1.4f) + 1f) / 2f
    val lift = 8.dp + (pulse * 2f).dp

    // Glass surface colours adapt to the theme mode.
    val surfaceBg = if (isDark) Color(0xFF0A101B) else Color(0xFFF7F8FC)

    Box(
        modifier = modifier
            .shadow(
                elevation = lift,
                shape = shape,
                ambientColor = if (isDark) Color(0xFFB9C2D8).copy(alpha = 0.65f) else Color(0xFFB9C2D8).copy(alpha = 0.25f),
                spotColor = if (isDark) Color(0xFF9DAAC8).copy(alpha = 0.42f) else Color(0xFF9DAAC8).copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(surfaceBg)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val stroke = 1.dp.toPx()
                val radius = cornerRadius.toPx()

                val light = Brush.linearGradient(
                    colors = listOf(
                        if (isDark) Color.White else Color(0xFFFFFFFF),
                        if (isDark) Color(0xFFF5F6FB) else Color(0xFFEEF2F7),
                        accent.copy(alpha = 0.10f),
                        if (isDark) Color(0xFFE9ECF5) else Color(0xFFD9DEE7)
                    ),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )

                val highlight = Brush.verticalGradient(
                    colors = listOf(
                        if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFFFFFFFF).copy(alpha = 0.7f),
                        if (isDark) Color.White.copy(alpha = 0.28f) else Color(0xFFFFFFFF).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.65f
                )

                val rim = Brush.linearGradient(
                    colors = listOf(
                        if (isDark) Color.White else Color(0xFFFFFFFF),
                        if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFFFFFFFF).copy(alpha = 0.5f),
                        accent.copy(alpha = 0.20f),
                        if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFFFFFFFF).copy(alpha = 0.8f)
                    ),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )

                onDrawWithContent {
                    drawRect(light)
                    drawRect(highlight)

                    drawContent()

                    if (sheen) {
                        val bandWidth = w * 0.30f
                        val cycle = (clock.value * 0.16f) % 2f

                        if (cycle < 1f) {
                            val x = -bandWidth +
                                cycle * (w + bandWidth * 2f)

                            rotate(
                                degrees = 18f,
                                pivot = Offset(w / 2f, h / 2f)
                            ) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFFFFFFFF).copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    ),
                                    topLeft = Offset(x, -h),
                                    size = Size(bandWidth, h * 3f)
                                )
                            }
                        }
                    }

                    drawRoundRect(
                        brush = rim,
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(
                            (w - stroke).coerceAtLeast(0f),
                            (h - stroke).coerceAtLeast(0f)
                        ),
                        cornerRadius = CornerRadius(
                            (radius - stroke).coerceAtLeast(0f)
                        ),
                        style = Stroke(width = stroke)
                    )
                }
            },
        content = content
    )
}

/**
 * Shared liquid-glass action button with pressed state and disabled styling.
 *
 * UX guidelines applied:
 * - 48dp minimum touch target
 * - Pressed state scales down for tactile feedback
 * - Disabled state shows reduced opacity (rule 31)
 * - Loading state disables interaction (rule 32)
 */
@Composable
fun GlassButton(
    text: String,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val accent = tint ?: config.accentColor()
    val shape = RoundedCornerShape(17.dp)
    val isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.30f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glassButtonPress"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minHeight(MinTouchTarget)
            .shadow(
                elevation = if (enabled && !loading) 5.dp else 2.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.16f),
                spotColor = accent.copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(
                if (!enabled) Color.White.copy(alpha = 0.08f)
                else Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.17f),
                        accent.copy(alpha = 0.13f),
                        Color.White.copy(alpha = 0.07f)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                onClick = { isPressed = true; onClick() }
            )
            .padding(horizontal = 15.dp, vertical = 13.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                alpha = if (enabled) 1f else 0.5f
            },
        contentAlignment = Alignment.Center,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (loading) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(16.dp)
                    ) {
                        // Simple spinning dot as loading indicator
                        val center = size.center
                        drawCircle(
                            color = accent,
                            radius = 4.dp.toPx(),
                            center = center
                        )
                    }
                } else {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) accent else accent.copy(alpha = 0.5f),
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                    }

                    Text(
                        text = text,
                        color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}

/**
 * Circular icon action button with 48dp minimum touch target.
 *
 * UX guidelines applied:
 * - 48dp minimum touch target (rule 22)
 * - Pressed state with scale + opacity feedback
 * - Focus indicator for keyboard navigation
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    tint: Color? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val accent = tint ?: config.accentColor()
    val buttonSize = size.coerceAtLeast(MinTouchTarget)
    val isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.30f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glassIconButtonPress"
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .shadow(
                elevation = if (enabled) 5.dp else 2.dp,
                shape = CircleShape,
                ambientColor = accent.copy(alpha = 0.18f),
                spotColor = accent.copy(alpha = 0.14f)
            )
            .clip(CircleShape)
            .background(
                if (enabled) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f)
            )
            .border(
                1.dp,
                if (enabled) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.12f),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = { isPressed = true; onClick() }
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                alpha = if (enabled) 1f else 0.5f
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) accent else accent.copy(alpha = 0.5f),
            modifier = Modifier.size(buttonSize * 0.48f)
        )
    }
}

/**
 * Theme-engine toggle switch with smooth animation.
 */
@Composable
fun LiquidSwitch(
    checked: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbFraction by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "liquidSwitch"
    )

    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(CircleShape)
            .background(if (checked) accent.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.32f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(22.dp)
                .padding(horizontal = (thumbFraction * 20f).dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
