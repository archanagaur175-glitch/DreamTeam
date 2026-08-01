package com.dreamteam.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

val MotionEaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
val MotionEaseInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
val MotionEaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

object Durations {
    const val Quick = 150
    const val Fast = 250
    const val Standard = 400
    const val Slow = 800
}

/** Standard float tween used across the design system. */
fun motionTween(durationMs: Int = Durations.Standard, easing: Easing = MotionEaseOutCubic): TweenSpec<Float> =
    tween(durationMillis = durationMs, easing = easing)
