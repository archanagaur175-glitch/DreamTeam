package com.dreamteam.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dreamteam.core.ui.theme.AccentIndigo
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.NightBase

/**
 * Full-screen "dark studio" backdrop: a near-black vertical gradient with two faint
 * aurora glows (indigo top, violet right) for depth.
 */
@Composable
fun DreamBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to NightBase,
                    0.45f to Color(0xFF0E0F1A),
                    1f to NightBase,
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentIndigo.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.12f, 0f),
                        radius = size.width * 0.9f,
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.12f, 0f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentViolet.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width, size.height * 0.85f),
                        radius = size.width * 0.8f,
                    ),
                    radius = size.width * 0.8f,
                    center = Offset(size.width, size.height * 0.85f),
                )
            },
    ) {
        content()
    }
}
