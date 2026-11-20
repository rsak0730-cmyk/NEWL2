package com.manish.liquidlauncher.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.LiquidThemeConfig
import kotlin.math.sin

/**
 * Main liquid-glass surface used throughout the launcher.
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

    val pulse = (sin(clock.value * 1.4f) + 1f) / 2f
    val lift = 8.dp + (pulse * 2f).dp

    Box(
        modifier = modifier
            .shadow(
                elevation = lift,
                shape = shape,
                ambientColor = Color(0xFFB9C2D8).copy(alpha = 0.65f),
                spotColor = Color(0xFF9DAAC8).copy(alpha = 0.42f)
            )
            .clip(shape)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val stroke = 1.dp.toPx()
                val radius = cornerRadius.toPx()

                val light = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF5F6FB),
                        accent.copy(alpha = 0.10f),
                        Color(0xFFE9ECF5)
                    ),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )

                val highlight = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.65f
                )

                val rim = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.65f),
                        accent.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.9f)
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
                                            Color.White.copy(alpha = 0.55f),
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

/** Shared liquid-glass action button. */
@Composable
fun GlassButton(
    text: String,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val accent = tint ?: config.accentColor()
    val shape = RoundedCornerShape(17.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.16f),
                spotColor = accent.copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.17f),
                        accent.copy(alpha = 0.13f),
                        Color.White.copy(alpha = 0.07f)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(9.dp))
        }

        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Circular icon action used in the home-screen toolbar. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val accent = tint ?: config.accentColor()
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 5.dp,
                shape = CircleShape,
                ambientColor = accent.copy(alpha = 0.18f),
                spotColor = accent.copy(alpha = 0.14f)
            )
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
            .jellyTouch(haptics = config.haptics, pressX = 0.92f, pressY = 0.92f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accent,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

/** Theme-engine toggle switch. */
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
