package com.dreamteam.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentIndigo
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Durations
import com.dreamteam.core.ui.theme.MotionEaseOutCubic
import com.dreamteam.core.ui.theme.NightSurfaceHigh
import com.dreamteam.core.ui.theme.TextMuted
import com.dreamteam.core.ui.theme.TextPrimary

/**
 * Sleep-debt ring: a filling/draining arc around a central hours value, animated on
 * every change. Sweep fraction = debt / [maxDebtHours].
 */
@Composable
fun DebtRing(
    debtHours: Double,
    maxDebtHours: Double = 14.0,
    modifier: Modifier = Modifier,
    centerValue: String,
    centerSub: String,
) {
    val fraction = (debtHours / maxDebtHours).toFloat().coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = Durations.Slow, easing = MotionEaseOutCubic),
        label = "debtRing",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 11.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            // track
            drawArc(
                color = NightSurfaceHigh,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // glow behind progress
            if (animatedFraction > 0.01f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(AccentIndigo, AccentViolet, AccentAmber, AccentIndigo),
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedFraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke + 8.dp.toPx(), cap = StrokeCap.Round),
                    alpha = 0.22f,
                )
            }
            // progress
            drawArc(
                brush = Brush.sweepGradient(listOf(AccentIndigo, AccentViolet, AccentAmber)),
                startAngle = -90f,
                sweepAngle = 360f * animatedFraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // end-cap dot
            if (animatedFraction > 0.01f) {
                val angle = Math.toRadians((-90 + 360 * animatedFraction).toDouble())
                val cx = size.width / 2 + (size.width / 2 - inset) * Math.cos(angle).toFloat()
                val cy = size.height / 2 + (size.height / 2 - inset) * Math.sin(angle).toFloat()
                drawCircle(color = AccentAmber, radius = stroke * 0.9f, center = Offset(cx, cy))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Text(
                text = centerValue,
                color = TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            )
            androidx.compose.material3.Text(
                text = centerSub,
                color = TextMuted,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            )
        }
    }
}
