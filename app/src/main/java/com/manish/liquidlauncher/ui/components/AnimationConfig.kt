package com.manish.liquidlauncher.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import kotlin.math.PI

/**
 * Centralized motion tokens for the Liquid Glass Launcher.
 *
 * Follows the UX Pro Max animation guidelines:
 * - 1-2 key animated elements per view maximum
 * - Duration 150-300ms for UI transitions, never a single universal cutoff
 * - Transform + opacity for animations (never width/height/top/left)
 * - Proper easing: ease-out for entry, linear for spinners, deceleration for arrival
 * - Respect reduced motion preferences
 */
object Motion {

    // ------------------------------------------------------------------
    // Duration tokens (ms)
    // ------------------------------------------------------------------
    @JvmField val DURATION_FAST = 150L    // Tiny micro-interactions (tooltip, ripple)
    @JvmField val DURATION_NORMAL = 250L  // Standard UI transitions (button press, sheet open)
    @JvmField val DURATION_SLOW = 350L    // Larger motions (page transitions, modal)
    @JvmField val DURATION_EXIT = 200L    // Exit animations should feel snappy

    // ------------------------------------------------------------------
    // Easing specs
    // ------------------------------------------------------------------
    /** Deceleration as the element arrives - use for entry animations. */
    @JvmField val EASE_OUT = LinearOutSlowInEasing

    /** Linear for constant-rate progress (spinners, loaders). */
    @JvmField val EASE_LINEAR = androidx.compose.animation.core.FastOutLinearInEasing

    /** Spring for bouncy, physical-feeling motions (cards, icons). */
    @JvmField val SPRING_GENTLE = spring(dampingRatio = 0.10f, stiffness = Spring.StiffnessLow)
    @JvmField val SPRING_SNAPPY = spring(dampingRatio = 0.20f, stiffness = Spring.StiffnessMedium)
    @JvmField val SPRING_BOUNCY = spring(dampingRatio = 0.08f, stiffness = Spring.StiffnessVeryLow)

    // ------------------------------------------------------------------
    // Tween specs
    // ------------------------------------------------------------------
    @JvmField val TWEEN_FADE = tween<Float>(durationMillis = DURATION_NORMAL, easing = EASE_OUT)
    @JvmField val TWEEN_SLIDE = tween<Float>(durationMillis = DURATION_NORMAL, easing = EASE_OUT)
    @JvmField val TWEEN_SCALE = tween<Float>(durationMillis = DURATION_FAST, easing = EASE_OUT)

    // ------------------------------------------------------------------
    // Stagger delays for list/item entrances
    // ------------------------------------------------------------------
    fun staggerDelay(index: Int, base: Long = 60L): Long = base * index
}

/**
 * Physical constants for liquid-style motion.
 */
object LiquidPhysics {

    /** Jelly squash factor when pressing an icon (1.0 = no squash). */
    @JvmField val PRESS_SCALE = 0.92f

    /** Scale-up while dragging. */
    @JvmField val DRAG_SCALE = 1.08f

    /** Idle bobbing amplitude in dp. */
    @JvmField val BOB_AMPLITUDE = 4f

    /** Idle bobbing period in seconds. */
    @JvmField val BOB_PERIOD = 3.0f

    /** Edge glow sweep speed. */
    @JvmField val EDGE_SWEEP_SPEED = 1.5f

    /** Wallpaper blob drift speed. */
    @JvmField val BLOB_DRIFT = 0.07f
}
