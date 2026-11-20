package com.manish.liquidlauncher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.manish.liquidlauncher.model.FolderState
import com.manish.liquidlauncher.model.IconShapeStyle
import com.manish.liquidlauncher.model.LiquidThemeConfig

// ---------------------------------------------------------
// APP ICON
// ---------------------------------------------------------

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
    var entered by remember(app.packageName, entranceIndex) {
        mutableStateOf(entranceIndex < 0)
    }

    LaunchedEffect(app.packageName, entranceIndex) {
        if (entranceIndex >= 0) {
            entered = false
            kotlinx.coroutines.delay(
                entranceIndex.coerceAtMost(20) * 22L
            )
            entered = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "iconEntranceScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(250),
        label = "iconEntranceAlpha"
    )

    val transition = rememberInfiniteTransition(
        label = "iconBobTransition"
    )

    val bobOffset by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "iconBob"
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
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationY = if (bob && config.iconBob) {
                    bobOffset.dp.toPx()
                } else {
                    0f
                }
            }
            .pointerInput(app.packageName, onLongClick) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick?.invoke() }
                )
            }
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(iconBoxSize)
                .clip(iconShape)
                .background(Color.White.copy(alpha = 0.055f)),
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
                modifier = Modifier.size(
                    width = iconBoxSize + 16.dp,
                    height = 17.dp
                )
            )
        }
    }
}

// ---------------------------------------------------------
// FOLDER ICON
// ---------------------------------------------------------

@Composable
fun LiquidFolderItem(
    title: String,
    apps: List<AppInfo>,
    config: LiquidThemeConfig,
    fontFamily: FontFamily = FontFamily.SansSerif,
    onClick: () -> Unit
) {
    val accent = config.accentColor()

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.20f),
                            accent.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.07f)
                        )
                    )
                )
                .padding(7.dp),
            contentAlignment = Alignment.Center
        ) {
            val previewApps = apps.take(4)

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (row in 0 until 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (column in 0 until 2) {
                            val index = row * 2 + column
                            val app = previewApps.getOrNull(index)

                            Box(
                                modifier = Modifier
                                    .size(17.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Color.White.copy(alpha = 0.10f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (app != null) {
                                    Image(
                                        bitmap = app.icon,
                                        contentDescription = app.label,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Color.White.copy(alpha = 0.28f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (config.showLabels) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.94f),
                fontFamily = fontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------
// LIQUID SWITCH
// ---------------------------------------------------------

@Composable
fun LiquidSwitch(
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbPosition by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "liquidSwitchPosition"
    )

    val trackColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) {
            accent.copy(alpha = 0.78f)
        } else {
            Color.White.copy(alpha = 0.20f)
        },
        animationSpec = tween(180),
        label = "liquidSwitchColor"
    )

    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 25.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(21.dp)
                .graphicsLayer {
                    translationX = thumbPosition.toPx()
                }
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ---------------------------------------------------------
// POP-IN ANIMATION
// ---------------------------------------------------------

fun Modifier.popIn(): Modifier = composed {
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
        label = "popupScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "popupAlpha"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}