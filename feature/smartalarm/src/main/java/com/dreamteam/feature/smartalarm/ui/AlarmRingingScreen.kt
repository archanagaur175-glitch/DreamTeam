package com.dreamteam.feature.smartalarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamButton
import com.dreamteam.core.ui.theme.TextMuted
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/** Full-screen wake UI: giant time, gentle copy, Snooze / Dismiss. */
@Composable
fun AlarmRingingScreen(
    targetMillis: Long,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
) {
    val targetTime = remember(targetMillis) {
        Instant.ofEpochMilli(targetMillis).atZone(ZoneId.systemDefault()).toLocalTime()
    }
    val timeText = String.format(Locale.US, "%02d:%02d", targetTime.hour, targetTime.minute)

    DreamBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            androidx.compose.material3.Text(
                text = "SMART WAKE",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                color = TextMuted,
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Text(
                text = timeText,
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Text(
                text = "Time to wake up",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = TextSecondary,
            )
            Spacer(Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DreamButton(text = "Snooze 9 min", onClick = onSnooze)
                DreamButton(text = "Dismiss", onClick = onDismiss)
            }
        }
    }
}
