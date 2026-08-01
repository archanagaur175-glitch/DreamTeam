package com.dreamteam.feature.logger.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.toLocalTime
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamButton
import com.dreamteam.core.ui.component.DreamFootnote
import com.dreamteam.core.ui.component.FactorChipFlow
import com.dreamteam.core.ui.component.SectionHeader
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightSurface
import com.dreamteam.core.ui.theme.TextMuted
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LoggerScreen(
    onBack: () -> Unit,
    viewModel: LoggerViewModel = hiltViewModel(),
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedLog by viewModel.selectedLog.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    var note by remember { mutableStateOf("") }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    var bedTime by remember { mutableStateOf(LocalTime.of(23, 0)) }
    var wakeTime by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var quality by remember { mutableIntStateOf(70) }

    LaunchedEffect(selectedLog) {
        note = selectedLog?.freeNote ?: ""
    }

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
                Text("Logger", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            }

            // --- date selector ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.selectDate(selectedDate.minusDays(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day", tint = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Text(
                        text = if (selectedDate == LocalDate.now().minusDays(1)) "yesterday" else if (selectedDate == LocalDate.now()) "today" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
                IconButton(onClick = { viewModel.selectDate(selectedDate.plusDays(1).coerceAtMost(LocalDate.now())) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day", tint = TextSecondary)
                }
            }

            // --- factor chips ---
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                SectionHeader("How did the day go?")
                FactorChipFlow(
                    tags = FactorTag.entries,
                    selected = selectedLog?.tags ?: emptySet(),
                    onToggle = viewModel::toggleTag,
                    modifier = Modifier.fillMaxWidth(),
                )
                DreamFootnote(
                    "Tap anything that applied. Tagged days are linked to the night that follows them.",
                )
            }

            // --- free note ---
            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    viewModel.saveNote(it)
                },
                label = { Text("Free note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.cardCorner),
            )

            // --- log a sleep session ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NightSurface, RoundedCornerShape(Dimens.cardCorner))
                    .padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                SectionHeader("Log a night's sleep")
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                    TimeField("Bed time", bedTime) { pickerTarget = PickerTarget.BED }
                    TimeField("Wake time", wakeTime) { pickerTarget = PickerTarget.WAKE }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quality", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.weight(1f))
                    Text("$quality%", style = MaterialTheme.typography.labelLarge, color = AccentAmber)
                }
                Slider(
                    value = quality.toFloat(),
                    onValueChange = { quality = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 9,
                )
                DreamButton(
                    text = "Save night",
                    onClick = {
                        viewModel.addSession(
                            start = selectedDate.atTime(bedTime),
                            end = selectedDate.plusDays(1).atTime(wakeTime),
                            quality = quality,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (sessions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                        Text("Recent nights", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        sessions.take(5).forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${s.sleepStart.toLocalDate()} · %.1f h".format(s.durationHours),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { viewModel.deleteSession(s.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pickerTarget?.let { target ->
        val initial = if (target == PickerTarget.BED) bedTime else wakeTime
        val state = rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { pickerTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val t = LocalTime.of(state.hour, state.minute)
                        if (target == PickerTarget.BED) bedTime = t else wakeTime = t
                        pickerTarget = null
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pickerTarget = null }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) },
        )
    }
}

@Composable
private fun TimeField(label: String, time: LocalTime, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(NightSurface, RoundedCornerShape(Dimens.cardCorner))
            .padding(Dimens.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(
            text = String.format(Locale.US, "%02d:%02d", time.hour, time.minute),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = Dimens.xs),
        )
    }
}

private enum class PickerTarget { BED, WAKE }
