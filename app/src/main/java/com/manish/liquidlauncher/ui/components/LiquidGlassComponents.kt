package com.manish.liquidlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.AppInfo
import com.manish.liquidlauncher.model.IconShapeStyle
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.notifications.NotificationHub
import com.manish.liquidlauncher.util.SoundFx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.sin

fun LiquidThemeConfig.accentColor(): Color = Color.hsv(accentHue.coerceIn(0f, 359.9f), 0.82f, 1f)

fun LiquidThemeConfig.secondaryColor(): Color = Color.hsv(secondaryHue.coerceIn(0f, 359.9f), 0.75f, 1f)

private val LabelShadow = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.65f), Offset(0f, 2f), 6f))

/**
 * Squash & stretch jelly touch feedback, plus the liquid touch ripple:
 * a soft ring expands from the exact point that was pressed.
 */
fun Modifier.jellyTouch(
    haptics: Boolean = true,
    pressX: Float = 0.86f,
    pressY: Float = 0.80f,
    ripple: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val clickState = rememberUpdatedState(onClick)
    val longState = rememberUpdatedState(onLongClick)
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf(false) }
    var rippleAt by remember { mutableStateOf<Offset?>(null) }
    val rippleProgress = remember { Animatable(1f) }
    val sx by animateFloatAsState(
        targetValue = if (pressed) pressX else 1f,
        animationSpec = spring(dampingRatio = 0.32f, stiffness = Spring.StiffnessMedium),
        label = "jellyX"
    )
    val sy by animateFloatAsState(
        targetValue = if (pressed) pressY else 1f,
        animationSpec = spring(dampingRatio = 0.28f, stiffness = Spring.StiffnessMedium),
        label = "jellyY"
    )
    this
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { position ->
                    pressed = true
                    if (ripple) {
                        rippleAt = position
                        rippleProgress.snapTo(0f)
                        scope.launch {
                            rippleProgress.animateTo(1f, tween(460, easing = LinearEasing))
                        }
                    }
                    if (haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    tryAwaitRelease()
                    pressed = false
                },
                onLongPress = {
                    val long = longState.value
                    if (long != null) {
                        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        long()
                    }
                },
                onTap = {
                    SoundFx.play(SoundFx.POP)
                    clickState.value()
                }
            )
        }
        .drawWithContent {
            drawContent()
            val origin = rippleAt
            val progress = rippleProgress.value
            if (ripple && origin != null && progress < 1f) {
                val radius = (size.maxDimension * 0.62f) * progress + 4f
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f * (1f - progress)),
                    radius = radius,
                    center = origin,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f * (1f - progress)),
                    radius = radius * 0.72f,
                    center = origin
                )
            }
        }
        .graphicsLayer {
            scaleX = sx
            scaleY = sy
        }
}

/** Multi-layer refractive glass surface. */
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
    val tilt = LocalTilt.current
    val glow = config.glowIntensity
    val density = config.refractionOpacity

    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = shape, ambientColor = accent.copy(alpha = 0.45f), spotColor = accent.copy(alpha = 0.60f))
            .clip(shape)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val sw = 1.2.dp.toPx()
                val cr = cornerRadius.toPx()
                val bandW = w * 0.34f
                val phase = ((w * 0.37f + h * 0.11f) % 100f) / 100f

                val body = Brush.verticalGradient(colors = listOf(
                    Color.White.copy(alpha = (density * 0.75f + 0.08f).coerceAtMost(0.35f)),
                    accent.copy(alpha = (density * 0.55f).coerceAtMost(0.28f)),
                    Color(0xFF060D18).copy(alpha = 0.66f),
                    accent.copy(alpha = (density * 0.45f).coerceAtMost(0.24f))
                ))
                val topGloss = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                    startY = 0f,
                    endY = h * 0.55f
                )
                val rim = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.72f * glow).coerceIn(0.2f, 0.95f)),
                        accent.copy(alpha = (0.45f * glow).coerceIn(0.15f, 0.85f)),
                        Color.White.copy(alpha = 0.06f),
                        accent.copy(alpha = (0.55f * glow).coerceIn(0.15f, 0.85f))
                    ),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )
                val glareRadius = maxOf(w, h) * 0.8f
                val glare = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.10f + 0.16f * glow), Color.Transparent),
                    center = Offset.Zero,
                    radius = glareRadius
                )
                val band = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.11f), Color.Transparent),
                    startX = 0f,
                    endX = bandW
                )

                onDrawWithContent {
                    drawRect(body)
                    drawRect(topGloss)
                    drawContent()
                    translate(
                        left = w * (0.5f + tilt.value.x * 0.55f),
                        top = h * (0.25f + tilt.value.y * 0.35f)
                    ) {
                        drawCircle(brush = glare, radius = glareRadius, center = Offset.Zero)
                    }
                    if (sheen) {
                        val cycle = (clock.value * 0.12f + phase * 1.8f) % 1.8f
                        if (cycle < 1f) {
                            val x = -bandW + cycle * (w + bandW * 2f)
                            rotate(degrees = 18f, pivot = Offset(w / 2f, h / 2f)) {
                                translate(left = x) {
                                    drawRect(brush = band, topLeft = Offset(0f, -h), size = Size(bandW, h * 3f))
                                }
                            }
                        }
                    }
                    drawRoundRect(
                        brush = rim,
                        topLeft = Offset(sw / 2f, sw / 2f),
                        size = Size(w - sw, h - sw),
                        cornerRadius = CornerRadius((cr - sw / 2f).coerceAtLeast(0f)),
                        style = Stroke(width = sw)
                    )
                }
            },
        content = content
    )
}

/** Round glass button used in the top bar. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 46.dp,
    onClick: () -> Unit
) {
    LiquidGlassSurface(
        config = config,
        cornerRadius = size / 2,
        modifier = modifier.size(size).jellyTouch(haptics = config.haptics, onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.align(Alignment.Center).size(20.dp)
        )
    }
}

/** Glass pill button with optional icon. */
@Composable
fun GlassButton(
    text: String,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color? = null,
    onClick: () -> Unit
) {
    LiquidGlassSurface(
        config = config,
        cornerRadius = 18.dp,
        tintOverride = tint,
        sheen = false,
        modifier = modifier.height(46.dp).jellyTouch(haptics = config.haptics, pressX = 0.95f, pressY = 0.88f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = tint ?: config.accentColor(), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Squishy liquid toggle. */
@Composable
fun LiquidSwitch(checked: Boolean, accent: Color, onCheckedChange: (Boolean) -> Unit) {
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "switchProgress"
    )
    var pressed by remember { mutableStateOf(false) }
    val thumbWidth by animateDpAsState(
        targetValue = if (pressed) 30.dp else 22.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "switchThumb"
    )
    val trackColor = lerp(Color.White.copy(alpha = 0.14f), accent.copy(alpha = 0.60f), progress.coerceIn(0f, 1f))
    val latest = rememberUpdatedState(onCheckedChange)
    val current = rememberUpdatedState(checked)

    Box(
        modifier = Modifier
            .width(50.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { latest.value(!current.value) }
                )
            }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = (42.dp - thumbWidth) * progress)
                .width(thumbWidth)
                .height(22.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFCFE8FF))))
        )
    }
}

fun iconShapeFor(style: IconShapeStyle, size: Dp): Shape = when (style) {
    IconShapeStyle.Squircle -> RoundedCornerShape((size.value * 0.34f).dp)
    IconShapeStyle.Circle -> CircleShape
    IconShapeStyle.Teardrop -> RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomEndPercent = 12, bottomStartPercent = 50)
    IconShapeStyle.Square -> RoundedCornerShape((size.value * 0.14f).dp)
}

/** Red glass count bubble used for notification badges. */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    val label = if (count > 99) "99+" else count.toString()
    Box(
        modifier = modifier.popIn().defaultMinSize(minWidth = 18.dp, minHeight = 18.dp).clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFFFF8A80), Color(0xFFFF1744))))
            .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape).padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Bubbly glass app icon. */
@Composable
fun LiquidAppIconItem(
    app: AppInfo,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier,
    showLabel: Boolean = config.showLabels,
    iconBoxSize: Dp = (60f * config.iconScale).dp,
    bob: Boolean = false,
    entranceIndex: Int = -1,
    fontFamily: FontFamily = FontFamily.SansSerif,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val clock = LocalAnimClock.current
    val tilt = LocalTilt.current
    val tint = app.tint
    val style = config.iconShape
    val shape = iconShapeFor(style, iconBoxSize)
    val phase = remember(app.packageName) { (app.packageName.hashCode() and 0xFFFF) / 10000f }
    val counts by NotificationHub.counts
    val badge = if (config.badges) (counts[app.packageName] ?: 0) else 0

    val appear = remember { Animatable(if (entranceIndex < 0) 1f else 0f) }
    LaunchedEffect(app.packageName) {
        if (entranceIndex >= 0) {
            delay(min(entranceIndex, 14) * 28L)
            appear.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .jellyTouch(haptics = config.haptics, onLongClick = onLongClick, onClick = onClick)
            .graphicsLayer {
                val a = appear.value
                val s = 0.5f + 0.5f * a
                scaleX = s
                scaleY = s
                alpha = a.coerceIn(0f, 1f)
                translationY = if (bob) sin(clock.value * 1.7f + phase) * 2.2.dp.toPx() else 0f
                transformOrigin = TransformOrigin(0.5f, 0.75f)
            }
            .padding(2.dp)
    ) {
        Box(modifier = Modifier.size(iconBoxSize)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val r = if (style == IconShapeStyle.Circle) size.minDimension / 2f else size.minDimension * 0.34f
                        for (i in 1..3) {
                            val grow = i * 3.dp.toPx()
                            drawRoundRect(
                                color = tint.copy(alpha = 0.13f / i),
                                topLeft = Offset(-grow, -grow + 4.dp.toPx()),
                                size = Size(size.width + grow * 2f, size.height + grow * 2f),
                                cornerRadius = CornerRadius(r + grow)
                            )
                        }
                    }
                    .clip(shape)
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val sw = 1.25.dp.toPx()
                        val half = sw / 2f
                        val radii = when (style) {
                            IconShapeStyle.Squircle -> floatArrayOf(w * 0.34f, w * 0.34f, w * 0.34f, w * 0.34f)
                            IconShapeStyle.Circle -> floatArrayOf(w / 2f, w / 2f, w / 2f, w / 2f)
                            IconShapeStyle.Teardrop -> floatArrayOf(w / 2f, w / 2f, w * 0.12f, w / 2f)
                            IconShapeStyle.Square -> floatArrayOf(w * 0.14f, w * 0.14f, w * 0.14f, w * 0.14f)
                        }
                        val rimPath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = half,
                                    top = half,
                                    right = w - half,
                                    bottom = h - half,
                                    topLeftCornerRadius = CornerRadius((radii[0] - half).coerceAtLeast(0f)),
                                    topRightCornerRadius = CornerRadius((radii[1] - half).coerceAtLeast(0f)),
                                    bottomRightCornerRadius = CornerRadius((radii[2] - half).coerceAtLeast(0f)),
                                    bottomLeftCornerRadius = CornerRadius((radii[3] - half).coerceAtLeast(0f))
                                )
                            )
                        }
                        val base = Brush.radialGradient(
                            colors = listOf(tint.copy(alpha = 0.50f), Color(0xFF0A1322).copy(alpha = 0.82f), tint.copy(alpha = 0.58f)),
                            center = Offset(w * 0.3f, h * 0.25f),
                            radius = w * 1.2f
                        )
                        val rim = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.85f), tint.copy(alpha = 0.65f), Color.White.copy(alpha = 0.12f), tint.copy(alpha = 0.75f)),
                            start = Offset.Zero,
                            end = Offset(w, h)
                        )
                        val gloss = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(w, 0f)
                            lineTo(w, h * 0.30f)
                            quadraticBezierTo(w * 0.5f, h * 0.14f, 0f, h * 0.40f)
                            close()
                        }
                        val glossBrush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.34f), Color.Transparent), startY = 0f, endY = h * 0.45f)
                        val caustic = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.18f)), startY = h * 0.62f, endY = h)
                        val dotRadius = w * 0.13f
                        val dot = Brush.radialGradient(colors = listOf(Color.White.copy(alpha = 0.95f), Color.Transparent), center = Offset.Zero, radius = dotRadius)

                        onDrawWithContent {
                            drawRect(base)
                            drawContent()
                            drawPath(gloss, glossBrush)
                            drawRect(caustic)
                            translate(left = w * 0.27f + tilt.value.x * w * 0.10f, top = h * 0.21f + tilt.value.y * h * 0.08f) {
                                drawCircle(brush = dot, radius = dotRadius, center = Offset.Zero)
                            }
                            drawPath(rimPath, brush = rim, style = Stroke(width = sw))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = app.icon, contentDescription = app.label, modifier = Modifier.size(iconBoxSize * 0.62f).clip(RoundedCornerShape(iconBoxSize * 0.2f)))
            }
            if (badge > 0) {
                CountBadge(count = badge, modifier = Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp))
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                color = Color.White.copy(alpha = 0.95f),
                fontFamily = fontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = LabelShadow,
                modifier = Modifier.widthIn(max = iconBoxSize + 14.dp)
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
    val accent = config.accentColor()
    val boxSize = (60f * config.iconScale).dp
    val mini = boxSize * 0.32f
    val counts by NotificationHub.counts
    val badge = if (config.badges) apps.sumOf { counts[it.packageName] ?: 0 } else 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.jellyTouch(haptics = config.haptics, onClick = onClick).padding(2.dp)
    ) {
        Box(modifier = Modifier.size(boxSize)) {
            LiquidGlassSurface(config = config, cornerRadius = (boxSize.value * 0.35f).dp, tintOverride = accent, modifier = Modifier.size(boxSize)) {
                Column(modifier = Modifier.align(Alignment.Center).padding(2.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val preview = apps.take(4)
                    for (row in 0..1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (col in 0..1) {
                                val idx = row * 2 + col
                                if (idx < preview.size) {
                                    Image(bitmap = preview[idx].icon, contentDescription = null, modifier = Modifier.size(mini).clip(RoundedCornerShape(mini * 0.3f)))
                                } else {
                                    Spacer(modifier = Modifier.size(mini))
                                }
                            }
                        }
                    }
                }
            }
            if (badge > 0) {
                CountBadge(count = badge, modifier = Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp))
            }
        }
        if (config.showLabels) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.95f),
                fontFamily = fontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = LabelShadow
            )
        }
    }
}

/** Bouncy scale-in for overlay cards. */
fun Modifier.popIn(): Modifier = composed {
    val progress = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 420f))
    }
    graphicsLayer {
        scaleX = progress.value
        scaleY = progress.value
        alpha = ((progress.value - 0.6f) / 0.4f).coerceIn(0f, 1f)
    }
}
