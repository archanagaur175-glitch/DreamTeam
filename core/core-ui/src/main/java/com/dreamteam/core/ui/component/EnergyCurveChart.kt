package com.dreamteam.core.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.dreamteam.core.common.toMinutesSinceMidnight
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentIndigo
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Durations
import com.dreamteam.core.ui.theme.MotionEaseOutCubic
import com.dreamteam.core.ui.theme.NightBorder
import com.dreamteam.core.ui.theme.NightSurfaceHigh
import java.time.LocalTime

/** A single point on the energy curve, in UI space (energy 0..100). */
data class CurvePoint(val time: LocalTime, val energy: Double)

/**
 * The signature glowing energy curve. Draws itself in over ~800ms, renders a soft
 * blurred glow beneath a gradient stroke, and animates a pulsing "now" marker.
 */
@Composable
fun EnergyCurveChart(
    points: List<CurvePoint>,
    modifier: Modifier = Modifier,
    now: LocalTime? = null,
    showNowMarker: Boolean = true,
    animateIn: Boolean = true,
) {
    val drawProgress = remember { Animatable(if (animateIn) 0f else 1f) }
    LaunchedEffect(animateIn) {
        if (animateIn) {
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = Durations.Slow, easing = MotionEaseOutCubic),
            )
        }
    }

    val pulse = rememberInfiniteTransition(label = "glowPulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 2000), RepeatMode.Reverse),
        label = "pulseAlpha",
    )

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val xFor: (LocalTime) -> Float = { t -> (t.toMinutesSinceMidnight() / 1440f) * size.width }
        val yFor: (Double) -> Float = { e -> ((100f - e.toFloat()) / 100f) * size.height }

        // --- faint horizontal grid at 25/50/75 energy ---
        listOf(25.0, 50.0, 75.0).forEach { e ->
            drawLine(
                color = NightBorder.copy(alpha = 0.5f),
                start = Offset(0f, yFor(e)),
                end = Offset(size.width, yFor(e)),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // --- full path + fill under curve ---
        val fullPath = Path().apply {
            moveTo(xFor(points.first().time), yFor(points.first().energy))
            points.drop(1).forEach { p -> lineTo(xFor(p.time), yFor(p.energy)) }
        }
        val fillPath = Path().apply {
            addPath(fullPath)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(AccentIndigo.copy(alpha = 0.16f), Color.Transparent),
                startY = 0f,
                endY = size.height,
            ),
        )

        // --- partial (draw-in) gradient stroke ---
        val progress = drawProgress.value
        val partial = Path()
        if (progress < 1f) {
            val measure = PathMeasure()
            measure.setPath(fullPath, false)
            val length = measure.length
            measure.getSegment(0f, length * progress, partial, true)
        } else {
            partial.addPath(fullPath)
        }

        // soft glow layer (native blur)
        drawIntoCanvas { canvas ->
            val glowPaint = Paint().apply {
                color = AccentViolet
                maskFilter = BlurMaskFilter(22.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                style = PaintingStyle.STROKE
                strokeWidth = 9.dp.toPx()
                strokeCap = StrokeCap.ROUND
            }
            canvas.drawPath(partial, glowPaint)
        }

        // main gradient stroke
        drawPath(
            path = partial,
            brush = Brush.horizontalGradient(listOf(AccentIndigo, AccentViolet, AccentAmber)),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )

        // --- now marker ---
        if (showNowMarker && now != null) {
            val x = xFor(now)
            if (x in 0f..size.width) {
                val energy = energyAt(points, now)
                val y = yFor(energy)
                drawLine(
                    color = AccentAmber.copy(alpha = 0.5f * pulseAlpha),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentAmber.copy(alpha = 0.45f * pulseAlpha), Color.Transparent),
                        center = Offset(x, y),
                        radius = 16.dp.toPx(),
                    ),
                    radius = 16.dp.toPx(),
                    center = Offset(x, y),
                )
                drawCircle(
                    color = AccentAmber,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y),
                )
            }
        }
    }
}

/** Linear interpolation of the curve at [t], clamped to the point range. */
fun energyAt(points: List<CurvePoint>, t: LocalTime): Double {
    if (points.isEmpty()) return 0.0
    if (points.size == 1) return points.first().energy
    val target = t.toMinutesSinceMidnight()
    var prev = points.first()
    for (p in points) {
        if (p.time.toMinutesSinceMidnight() >= target) {
            val span = (p.time.toMinutesSinceMidnight() - prev.time.toMinutesSinceMidnight()).coerceAtLeast(1)
            val frac = (target - prev.time.toMinutesSinceMidnight()).coerceIn(0, span) / span.toDouble()
            return prev.energy + (p.energy - prev.energy) * frac
        }
        prev = p
    }
    return points.last().energy
}
