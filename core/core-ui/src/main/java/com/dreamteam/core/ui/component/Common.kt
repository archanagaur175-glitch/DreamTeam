package com.dreamteam.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentIndigo
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightBorder
import com.dreamteam.core.ui.theme.NightSurface
import com.dreamteam.core.ui.theme.TextMuted
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary

/** Dark stat card with label + large value + optional unit. */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    accent: Color = AccentViolet,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.cardCorner))
            .background(NightSurface)
            .border(1.dp, NightBorder, RoundedCornerShape(Dimens.cardCorner))
            .padding(Dimens.lg),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
        Spacer(modifier = Modifier.height(Dimens.sm))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = accent,
            )
            unit?.let {
                Text(
                    text = " $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
    }
}

/** Section header with optional amber action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = AccentAmber,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

/** Primary gradient pill button. */
@Composable
fun DreamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(Dimens.buttonCorner)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(AccentIndigo, AccentViolet)))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.45f)
            .padding(vertical = 14.dp, horizontal = Dimens.xl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}
