package com.manish.liquidlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.AppInfo
import com.manish.liquidlauncher.model.IconShapeStyle
import com.manish.liquidlauncher.model.LiquidThemeConfig

/**
 * Staggered entrance animation for app icons on first page load.
 *
 * UX guidelines applied:
 * - Staggered entrances feel deliberate and polished (rules 8, 13)
 * - Icons slide up and fade in with easing (rules 7, 9)
 */
@Composable
private fun StaggeredEntrance(
    index: Int,
    config: LiquidThemeConfig,
    content: @Composable () -> Unit
) {
    val delayMs = Motion.staggerDelay(index)
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = Motion.DURATION_NORMAL,
                delayMillis = delayMs,
                easing = Motion.EASE_OUT
            )
        ) +
            slideInVertically(
                initialOffsetY = { 12.dp },
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = Motion.DURATION_NORMAL,
                    delayMillis = delayMs,
                    easing = Motion.EASE_OUT
                )
            )
    ) {
        content()
    }
}

@Composable
fun LiquidAppIconItem(
    app: AppInfo,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    showLabel: Boolean = config.showLabels,
    iconBoxSize: Dp = (56f * config.iconScale).dp,
    bob: Boolean = config.iconBob,
    entranceIndex: Int = -1,
    fontFamily: FontFamily = FontFamily.SansSerif,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val bobScale by animateFloatAsState(
        targetValue = if (bob) 1.035f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow),
        label = "appIconBob"
    )
    val shape = when (config.iconShape) {
        IconShapeStyle.Circle -> CircleShape
        IconShapeStyle.Square -> RoundedCornerShape(8.dp)
        IconShapeStyle.Teardrop -> RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 26.dp, bottomEnd = 8.dp)
        IconShapeStyle.Squircle -> RoundedCornerShape(15.dp)
    }

    Column(
        modifier = modifier
            .jellyTouch(haptics = config.haptics, onLongClick = onLongClick, onClick = onClick)
            .graphicsLayer {
                scaleX = bobScale
                scaleY = bobScale
                alpha = 1f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (entranceIndex >= 0) {
            StaggeredEntrance(index = entranceIndex, config = config) {
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(shape)
                        .background(app.tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = app.icon,
                        contentDescription = app.label,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(4.dp).clip(shape)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(iconBoxSize)
                    .clip(shape)
                    .background(app.tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(shape)
                )
            }
        }
        if (showLabel) {
            Text(
                text = app.label,
                color = Color.White,
                fontFamily = fontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(width = iconBoxSize + 12.dp, height = 18.dp).padding(top = 3.dp)
            )
        }
    }
}

@Composable
fun LiquidFolderItem(
    title: String,
    apps: List<AppInfo>,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.SansSerif,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape((config.cornerRadiusDp * 0.55f).dp)
    Column(
        modifier = modifier
            .jellyTouch(haptics = config.haptics, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size((56f * config.iconScale).dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column {
                repeat(2) { row ->
                    androidx.compose.foundation.layout.Row {
                        repeat(2) { col ->
                            val app = apps.getOrNull(row * 2 + col)
                            if (app != null) {
                                Image(
                                    bitmap = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp).padding(2.dp).clip(RoundedCornerShape(6.dp))
                                )
                            } else {
                                Box(modifier = Modifier.size(22.dp).padding(2.dp))
                            }
                        }
                    }
                }
            }
        }
        if (config.showLabels) {
            Text(
                text = title,
                color = Color.White,
                fontFamily = fontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(width = 72.dp, height = 18.dp).padding(top = 3.dp)
            )
        }
    }
}
