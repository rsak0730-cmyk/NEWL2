package com.manish.liquidlauncher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
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
 * Animated app icon used by:
 * - Home screen
 * - App drawer
 * - Dock
 * - Folders
 * - App recommendations
 */
@Composable
fun LiquidAppIconItem(
    app: AppInfo,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    showLabel: Boolean = config.showLabels,
    iconBoxSize: Dp = 56.dp,
    bob: Boolean = true,
    entranceIndex: Int = -1,
    fontFamily: FontFamily = FontFamily.SansSerif,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val accent = config.accentColor()

    // Entrance animation.
    var entered by remember(app.packageName, entranceIndex) {
        mutableStateOf(entranceIndex < 0)
    }

    LaunchedEffect(app.packageName, entranceIndex) {
        if (entranceIndex >= 0) {
            entered = false
            kotlinx.coroutines.delay(
                (entranceIndex.coerceAtMost(20) * 22L)
            )
            entered = true
        }
    }

    val entranceScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "appIconEntrance"
    )

    val entranceAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = 260,
            easing = FastOutSlowInEasing
        ),
        label = "appIconAlpha"
    )

    // Gentle floating animation.
    val infiniteTransition = rememberInfiniteTransition(
        label = "appIconBob"
    )

    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "iconFloating"
    )

    val iconShape = when (config.iconShape) {
        IconShapeStyle.Circle -> CircleShape
        IconShapeStyle.Squircle -> RoundedCornerShape(30)
        IconShapeStyle.Teardrop -> RoundedCornerShape(
            topStartPercent = 50,
            topEndPercent = 50,
            bottomEndPercent = 50,
            bottomStartPercent = 15
        )
        IconShapeStyle.Square -> RoundedCornerShape(8.dp)
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = entranceScale
                scaleY = entranceScale
                alpha = entranceAlpha
                translationY = if (bob && config.iconBob) {
                    bobOffset.dp.toPx()
                } else {
                    0f
                }
            }
            .pointerInput(app.packageName, onLongClick) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    },
                    onLongPress = {
                        onLongClick?.invoke()
                    }
                )
            }
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        // App icon container.
        Box(
            modifier = Modifier
                .size(iconBoxSize)
                .clip(iconShape)
                .background(
                    Color.White.copy(alpha = 0.055f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(iconBoxSize * 0.88f)
                    .clip(iconShape)
            )
        }

        // App label.
        if (showLabel) {
            Text(
                text = app.label,
                color = Color.White.copy(alpha = 0.94f),
                fontFamily = fontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .size(
                        width = iconBoxSize + 16.dp,
                        height = 17.dp
                    )
            )
        }
    }
}

/**
 * Reusable spring popup animation.
 *
 * Used by:
 * FolderOverlay
 * DeviceHubSheet
 * CarouselSheet
 * TypeStudioSheet
 */
fun Modifier.popIn(): Modifier = androidx.compose.ui.composed {

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "liquidPopupScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "liquidPopupAlpha"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}