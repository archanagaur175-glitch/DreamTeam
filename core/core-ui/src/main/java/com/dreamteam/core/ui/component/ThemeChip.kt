package com.dreamteam.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.ui.theme.AccentIndigo
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightBorder
import com.dreamteam.core.ui.theme.NightSurfaceHigh
import com.dreamteam.core.ui.theme.TextSecondary
import com.dreamteam.core.ui.theme.TextMuted

/** A single tappable factor chip. */
@Composable
fun ThemeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
) {
    val shape = RoundedCornerShape(Dimens.chipCorner)
    val backgroundBrush: Brush = if (selected) {
        Brush.horizontalGradient(
            listOf(AccentIndigo.copy(alpha = 0.9f), AccentViolet.copy(alpha = 0.9f)),
        )
    } else {
        Brush.solidColor(NightSurfaceHigh)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                color = if (selected) AccentViolet else NightBorder,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.md, vertical = Dimens.sm)
            .alpha(if (selected) 1f else 0.85f),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            emoji?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** FlowRow of factor chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FactorChipFlow(
    tags: List<FactorTag>,
    selected: Set<FactorTag>,
    onToggle: (FactorTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
    ) {
        tags.forEach { tag ->
            val isSelected = tag in selected
            ThemeChip(
                text = tag.displayName,
                emoji = tag.emoji,
                selected = isSelected,
                onClick = { onToggle(tag) },
            )
        }
    }
}

/** Muted helper text used for disclaimers. */
@Composable
fun DreamFootnote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = modifier,
    )
}
