package com.dreamteam.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.toMinutesSinceMidnight
import com.dreamteam.core.ui.component.AlarmWindowArc
import com.dreamteam.core.ui.component.CurvePoint
import com.dreamteam.core.ui.component.DebtRing
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamButton
import com.dreamteam.core.ui.component.DreamFootnote
import com.dreamteam.core.ui.component.EnergyCurveChart
import com.dreamteam.core.ui.component.SectionHeader
import com.dreamteam.core.ui.component.ThemeChip
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightSurface
import com.dreamteam.core.ui.theme.SuccessTeal
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import com.dreamteam.feature.circadian.domain.EnergyCurve
import com.dreamteam.feature.dashboard.DashboardViewModel
import com.dreamteam.feature.sleepdebt.domain.RollingDebt
import com.dreamteam.feature.smartalarm.domain.AlarmConfig
import com.dreamteam.feature.smartalarm.domain.SensorMode
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Navigation destinations the dashboard can open. */
sealed interface DashboardDestination {
    data object Debt : DashboardDestination
    data object Circadian : DashboardDestination
    data object Alarm : DashboardDestination
    data object Logger : DashboardDestination
    data object Correlations : DashboardDestination
    data object Settings : DashboardDestination
}

@Composable
fun DashboardScreen(
    onNavigate: (DashboardDestination) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val debt by viewModel.debt.collectAsStateWithLifecycle()
    val curve by viewModel.curve.collectAsStateWithLifecycle()
    val alarm by viewModel.alarm.collectAsStateWithLifecycle()
    val todayTags by viewModel.todayTags.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()

    DreamBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg),
        ) {
            // --- header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = LocalTime.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    Text(
                        text = "DreamTeam",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                    )
                }
                IconButton(onClick = { onNavigate(DashboardDestination.Settings) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }

            // --- sleep debt ring ---
            debt?.let { d -> DebtCard(d, onOpen = { onNavigate(DashboardDestination.Debt) }) }

            // --- energy curve ---
            curve?.let { c -> EnergyCard(c, now, onOpen = { onNavigate(DashboardDestination.Circadian) }) }

            // --- tonight's alarm ---
            AlarmCard(alarm, now, onOpen = { onNavigate(DashboardDestination.Alarm) })

            // --- quick log ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.cardCorner))
                    .background(NightSurface)
                    .padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                SectionHeader(
                    title = "Today",
                    action = "Log more",
                    onAction = { onNavigate(DashboardDestination.Logger) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    listOf(
                        FactorTag.CAFFEINE,
                        FactorTag.EXERCISE,
                        FactorTag.STRESS,
                        FactorTag.NAP,
                    ).forEach { tag ->
                        ThemeChip(
                            text = tag.displayName,
                            emoji = tag.emoji,
                            selected = tag in todayTags,
                            onClick = { onNavigate(DashboardDestination.Logger) },
                        )
                    }
                }
                DreamFootnote("Quick-log for today — open Logger for last night and details.")
                DreamButton(
                    text = "Associations",
                    onClick = { onNavigate(DashboardDestination.Correlations) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(Dimens.lg))
        }
    }
}

@Composable
private fun DebtCard(debt: RollingDebt, onOpen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardCorner))
            .background(NightSurface)
            .padding(Dimens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Sleep debt", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Text(
                text = if (debt.isConfident) "Rolling ${debt.windowDays}-night window" else "Low data confidence",
                style = MaterialTheme.typography.labelSmall,
                color = if (debt.isConfident) TextSecondary else AccentAmber,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.xl),
        ) {
            DebtRing(
                debtHours = debt.totalDeficitHours,
                centerValue = String.format(Locale.US, "%.1f", debt.totalDeficitHours),
                centerSub = "hours owed",
                modifier = Modifier.size(150.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                debt.trendHours?.let { trend ->
                    val color = if (trend < 0) SuccessTeal else AccentAmber
                    Text(
                        text = if (trend < 0) "Paying down −%.1f h".format(trend) else "Trending up +%.1f h".format(trend),
                        style = MaterialTheme.typography.labelLarge,
                        color = color,
                    )
                }
                Text(
                    text = "%.1f h avg deficit".format(debt.avgDeficitHours),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Text(
                    text = "Tap for the 14-night breakdown",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.labelLarge,
                color = AccentViolet,
                modifier = Modifier
                    .padding(Dimens.sm)
                    .clickable(onClick = onOpen),
            )
        }
    }
}

@Composable
private fun EnergyCard(curve: EnergyCurve, now: LocalTime, onOpen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardCorner))
            .background(NightSurface)
            .clickable(onClick = onOpen)
            .padding(Dimens.lg),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        SectionHeader("Today's energy")
        EnergyCurveChart(
            points = curve.points.map { CurvePoint(it.time, it.energy) },
            now = now,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.lg)) {
            Text(
                text = "Peak ${hhmm(curve.peakTime)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Text(
                text = "Slump ${hhmm(curve.slumpTime)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Text(
                text = "Bedtime ${hhmm(curve.idealBedtime)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun AlarmCard(alarm: AlarmConfig?, now: LocalTime, onOpen: () -> Unit) {
    val monitoring = alarm != null && alarm.sensorMode != SensorMode.OFF &&
        isInWindow(
            now,
            alarm.targetWakeTime.minusMinutes(alarm.windowMinutes.toLong()),
            alarm.targetWakeTime,
        )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardCorner))
            .background(NightSurface)
            .padding(Dimens.lg),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        SectionHeader(
            title = "Smart wake",
            action = if (alarm != null) "Change" else null,
            onAction = if (alarm != null) onOpen else null,
        )
        if (alarm == null) {
            Text(
                text = "No alarm set. Set a wake target and we'll watch for light sleep inside your window.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            DreamButton(text = "Set tonight's alarm", onClick = onOpen, modifier = Modifier.fillMaxWidth())
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.lg),
            ) {
                AlarmWindowArc(
                    windowStart = alarm.targetWakeTime.minusMinutes(alarm.windowMinutes.toLong()),
                    targetTime = alarm.targetWakeTime,
                    isActive = monitoring,
                    now = now,
                    modifier = Modifier.size(140.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    Text(
                        text = if (monitoring) "Monitoring now" else "Armed",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (monitoring) SuccessTeal else TextSecondary,
                    )
                    Text(
                        text = hhmm(alarm.targetWakeTime),
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Window ${hhmm(alarm.targetWakeTime.minusMinutes(alarm.windowMinutes.toLong()))}–${hhmm(alarm.targetWakeTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

/** Circular check that handles windows crossing midnight. */
private fun isInWindow(now: LocalTime, start: LocalTime, end: LocalTime): Boolean {
    val n = now.toMinutesSinceMidnight()
    val s = start.toMinutesSinceMidnight()
    val e = end.toMinutesSinceMidnight()
    return if (s <= e) n in s..e else (n >= s || n <= e)
}

private fun hhmm(time: LocalTime): String =
    String.format(Locale.US, "%02d:%02d", time.hour, time.minute)
