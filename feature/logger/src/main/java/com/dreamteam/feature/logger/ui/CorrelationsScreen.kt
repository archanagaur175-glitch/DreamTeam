package com.dreamteam.feature.logger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamFootnote
import com.dreamteam.core.ui.component.SectionHeader
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightSurface
import com.dreamteam.core.ui.theme.SuccessTeal
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import com.dreamteam.feature.logger.domain.CorrelationResult
import java.util.Locale

@Composable
fun CorrelationsScreen(
    onBack: () -> Unit,
    viewModel: CorrelationsViewModel = hiltViewModel(),
) {
    val correlations by viewModel.correlations.collectAsStateWithLifecycle()

    DreamBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text("Associations", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            }

            DreamFootnote(
                "Average sleep on nights following days tagged (or not tagged) with each factor, over the " +
                    "last 30 days. These are associations, not proof of cause and effect.",
            )

            correlations.forEach { result ->
                CorrelationRow(result)
            }
        }
    }
}

@Composable
private fun CorrelationRow(result: CorrelationResult) {
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .background(NightSurface, RoundedCornerShape(Dimens.cardCorner))
            .padding(Dimens.lg),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(result.tag.emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(Dimens.sm))
            Text(result.tag.displayName, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }
        if (result.enoughData && result.meanSleepWith != null && result.meanSleepWithout != null) {
            Text(
                text = "With: %.1f h avg (n=%d)   ·   Without: %.1f h avg (n=%d)".format(
                    result.meanSleepWith, result.nightsWith, result.meanSleepWithout, result.nightsWithout,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            val delta = result.deltaHours ?: 0.0
            val color = when {
                delta > 0.25 -> SuccessTeal
                delta < -0.25 -> AccentAmber
                else -> TextSecondary
            }
            Text(
                text = "%.1f h difference".format(delta),
                style = MaterialTheme.typography.labelLarge,
                color = color,
            )
        } else {
            Text(
                text = "Not enough logged days yet (with: n=%d, without: n=%d)".format(
                    result.nightsWith, result.nightsWithout,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}
