package com.dreamteam.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dreamteam.core.common.toMinutesSinceMidnight
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentIndigo
import com.dreamteam.core.ui.theme.NightBorder
import com.dreamteam.core.ui.theme.NightSurfaceHigh
import java.time.LocalTime

/**
 * Visualizes the smart-wake window as a segment on a 24h ring. When [isActive],
 * the segment pulses and a "scanner" dot sweeps from window start toward target.
 */
@Composable
fun AlarmWindowArc(
    windowStart: LocalTime,
    targetTime: LocalTime,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    now: LocalTime? = null,
) {
    val pulse = rememberInfiniteTransition(label = "alarmPulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1400), RepeatMode.Reverse),
        label = "alarmPulseAlpha",
    )

    Canvas(modifier = modifier) {
        val stroke = 7.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)

        fun angleFor(minutes: Int): Float = -90f + (minutes / 1440f) * 360f

        // full-day track
        drawArc(
            color = NightSurfaceHigh,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        val startMin = windowStart.toMinutesSinceMidnight()
        val targetMin = targetTime.toMinutesSinceMidnight()
        val sweepMinutes = ((targetMin - startMin) % 1440 + 1440) % 1440
        if (sweepMinutes > 0) {
            val startAngle = angleFor(startMin)
            val sweep = sweepMinutes / 1440f * 360f
            // glow when active
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(AccentAmber, AccentIndigo, AccentAmber),
                    center = Offset(size.width / 2, size.height / 2),
                ),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke + 6.dp.toPx(), cap = StrokeCap.Round),
                alpha = if (isActive) 0.35f * pulseAlpha else 0.12f,
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(AccentAmber, AccentIndigo, AccentAmber),
                    center = Offset(size.width / 2, size.height / 2),
                ),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                alpha = if (isActive) 0.8f + 0.2f * pulseAlpha else 0.55f,
            )
            // segment end caps
            listOf(startAngle, startAngle + sweep).forEach { a ->
                val rad = Math.toRadians(a.toDouble())
                val cx = size.width / 2 + (size.width / 2 - inset) * Math.cos(rad).toFloat()
                val cy = size.height / 2 + (size.height / 2 - inset) * Math.sin(rad).toFloat()
                drawCircle(color = AccentAmber.copy(alpha = 0.85f), radius = stroke * 0.7f, center = Offset(cx, cy))
            }
        }

        // scanner dot when active and now is inside the window
        if (isActive && now != null) {
            val nowMin = now.toMinutesSinceMidnight()
            val elapsed = ((nowMin - startMin) % 1440 + 1440) % 1440
            if (elapsed in 0..sweepMinutes) {
                val a = angleFor(startMin + elapsed)
                val rad = Math.toRadians(a.toDouble())
                val cx = size.width / 2 + (size.width / 2 - inset) * Math.cos(rad).toFloat()
                val cy = size.height / 2 + (size.height / 2 - inset) * Math.sin(rad).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.6f * pulseAlpha), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = 14.dp.toPx(),
                    ),
                    radius = 14.dp.toPx(),
                    center = Offset(cx, cy),
                )
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(cx, cy))
            }
        }

        // center divider ticks (decorative)
        drawLine(
            color = NightBorder,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 1.dp.toPx(),
        )
    }
}
