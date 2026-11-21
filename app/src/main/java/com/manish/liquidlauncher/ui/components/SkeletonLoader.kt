package com.manish.liquidlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.repeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.unit.dp
import com.manish.liquidlauncher.model.LiquidThemeConfig

/**
 * Shimmering skeleton placeholder for loading states.
 *
 * UX guidelines applied:
 * - Shows feedback during async operations (rule 10)
 * - Preserves layout while content loads (rule 19)
 * - Matches expected wait with stable skeleton (rule 78)
 */
@Composable
fun SkeletonLoader(
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    width: Dp = Dp.Unspecified,
    shimmer: Boolean = true
) {
    val isDark = config.themeMode == com.manish.liquidlauncher.model.ThemeMode.Dark
    val baseColor = if (isDark) Color(0xFF1B2535) else Color(0xFFE2E8F0)
    val highlightColor = if (isDark) Color(0xFF2A3B4C) else Color(0xFFFFFFFF)

    val shimmerProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animationSpec = tween(
                durationMillis = 1500,
                delayMillis = 250,
                easing = LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "skeletonShimmer"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .width(width)
            .background(baseColor)
            .drawWithContent {
                drawContent()

                if (shimmer) {
                    val w = size.width
                    val h = size.height
                    val gradient = ShaderBrush(
                        LinearGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0f),
                                highlightColor.copy(alpha = 0.6f),
                                baseColor.copy(alpha = 0f)
                            ),
                            startX = -w * 0.5f + (w * shimmerProgress),
                            endX = -w * 0.5f + (w * shimmerProgress) + w,
                            startY = 0f,
                            endY = h
                        )
                    )
                    drawRect(
                        brush = gradient,
                        topLeft = androidx.compose.ui.geometry.Offset.Zero,
                        size = size
                    )
                }
            }
    )
}

/**
 * Vertical list of skeleton lines for text blocks.
 */
@Composable
fun SkeletonTextBlock(
    config: LiquidThemeConfig,
    lines: Int = 3,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 16.dp,
    lineGap: Dp = 8.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(lineGap)
    ) {
        repeat(lines) { index ->
            val lineWidth = if (index == lines - 1) 0.6f else 1f
            SkeletonLoader(
                config = config,
                modifier = Modifier
                    .fillMaxWidth(0.9f * lineWidth)
                    .height(lineHeight),
                shimmer = true
            )
        }
    }
}
