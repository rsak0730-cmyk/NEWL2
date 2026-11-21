package com.manish.liquidlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.util.SoundFx
import kotlinx.coroutines.launch

fun LiquidThemeConfig.accentColor(): Color =
    Color.hsv(
        accentHue.coerceIn(0f, 359.9f),
        0.82f,
        1f
    )

fun LiquidThemeConfig.secondaryColor(): Color =
    Color.hsv(
        secondaryHue.coerceIn(0f, 359.9f),
        0.75f,
        1f
    )

/**
 * Jelly-style touch modifier with visual pressed feedback, ripple, haptics, and sound.
 *
 * UX guidelines applied:
 * - 48dp minimum touch target (enforced by callers)
 * - Pressed state scales down slightly for tactile feedback
 * - Ripple expands from press point
 * - Haptics on press and long-press
 * - Sound on tap
 * - Animation uses Motion tokens for consistent timing
 */
fun Modifier.jellyTouch(
    haptics: Boolean = true,
    pressX: Float = 0.94f,
    pressY: Float = 0.90f,
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

    val rippleProgress = remember {
        Animatable(1f)
    }

    val sx by animateFloatAsState(
        targetValue = if (pressed) pressX else 1f,
        animationSpec = spring(
            dampingRatio = 0.30f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "jellyX"
    )

    val sy by animateFloatAsState(
        targetValue = if (pressed) pressY else 1f,
        animationSpec = spring(
            dampingRatio = 0.26f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "jellyY"
    )

    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = tween(
            durationMillis = Motion.DURATION_FAST,
            easing = Motion.EASE_OUT
        ),
        label = "jellyAlpha"
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
                            rippleProgress.animateTo(
                                1f,
                                tween(
                                    durationMillis = Motion.DURATION_NORMAL,
                                    easing = LinearEasing
                                )
                            )
                        }
                    }

                    if (haptics) {
                        haptic.performHapticFeedback(
                            HapticFeedbackType.TextHandleMove
                        )
                    }

                    tryAwaitRelease()
                    pressed = false
                },

                onLongPress = {
                    val long = longState.value

                    if (long != null) {
                        if (haptics) {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                        }

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

                val radius =
                    (size.maxDimension * 0.62f) * progress + 4f

                drawCircle(
                    color = Color.White.copy(
                        alpha = 0.28f * (1f - progress)
                    ),
                    radius = radius,
                    center = origin,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                drawCircle(
                    color = Color.White.copy(
                        alpha = 0.10f * (1f - progress)
                    ),
                    radius = radius * 0.72f,
                    center = origin
                )
            }
        }
        .graphicsLayer {
            scaleX = sx
            scaleY = sy
            alpha = alpha
        }
}
