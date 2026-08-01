package com.dreamteam.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamButton
import com.dreamteam.core.ui.component.DreamFootnote
import com.dreamteam.core.ui.component.SectionHeader
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.SuccessTeal
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val baseline by viewModel.baselineHours.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf(baseline) }

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
                Text("Settings", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                SectionHeader("Sleep need")
                Text(
                    text = "Your nightly sleep need. DreamTeam compares every night against this to " +
                        "build your 14-night rolling debt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%.1f h", draft),
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (saved) {
                        Text(
                            text = "Saved",
                            style = MaterialTheme.typography.labelLarge,
                            color = SuccessTeal,
                        )
                    }
                }
                Slider(
                    value = draft.toFloat(),
                    onValueChange = { draft = it.toDouble() },
                    valueRange = 6f..10f,
                    steps = 7, // 0.5h increments
                )
                DreamButton(
                    text = "Save sleep need",
                    onClick = { viewModel.setBaseline(draft) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                SectionHeader("About")
                Text(
                    text = "DreamTeam v1.0 — a sleep instrument panel: rolling sleep debt, a wake-anchored " +
                        "circadian energy curve, and a smart wake window.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                DreamFootnote(
                    "v1 notes: all data stays on this device; light-sleep sensing uses the accelerometer " +
                        "(≈50–70% accurate vs. clinical studies, may wake a few minutes early — never late); " +
                        "release builds are CI/debug-signed only.",
                )
            }
            Spacer(Modifier.height(Dimens.lg))
        }
    }
}
