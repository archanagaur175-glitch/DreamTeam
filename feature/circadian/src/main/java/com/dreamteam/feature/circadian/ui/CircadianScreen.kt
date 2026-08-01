package com.dreamteam.feature.circadian.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreamteam.core.common.nowFlow
import com.dreamteam.core.ui.component.CurvePoint
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamFootnote
import com.dreamteam.core.ui.component.EnergyCurveChart
import com.dreamteam.core.ui.component.StatCard
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightSurface
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import com.dreamteam.feature.circadian.domain.EnergyCurve
import java.time.LocalTime
import java.util.Locale

@Composable
fun CircadianScreen(
    onBack: () -> Unit,
    viewModel: CircadianViewModel = hiltViewModel(),
) {
    val curve by viewModel.curve.collectAsStateWithLifecycle()
    val now by produceState<LocalTime?>(initialValue = null) {
        nowFlow(30_000).collect { value = it }
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
                    )
                }
                Text(
                    text = "Today's energy",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
            }

            curve?.let { c ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NightSurface, RoundedCornerShape(Dimens.cardCorner))
                        .padding(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    Text(
                        text = "Anchored to your wake time · ${hhmm(c.wakeTime)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    EnergyCurveChart(
                        points = c.points.map { CurvePoint(it.time, it.energy) },
                        now = now,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                        StatCard(label = "Peak", value = hhmm(c.peakTime), accent = AccentViolet)
                        StatCard(label = "Slump", value = hhmm(c.slumpTime), accent = AccentAmber)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                    StatCard(
                        label = "Wind-down starts",
                        value = hhmm(c.windDownStart),
                    )
                    StatCard(
                        label = "Ideal bedtime",
                        value = hhmm(c.idealBedtime),
                        accent = AccentAmber,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    Text(
                        text = "Why it looks like this",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Energy rises after waking, peaks late morning, dips mid-afternoon " +
                            "(the classic slump), climbs to a smaller second peak, then winds down " +
                            "toward bedtime. The shape is anchored to your wake time — shift your " +
                            "wake, and the whole day shifts with it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    if (c.debtHours > 0.5) {
                        DreamFootnote(
                            "You're carrying %.1f h of sleep debt, so today's peaks are compressed " +
                                "and your afternoon slump is deeper and earlier than a debt-free day."
                                .format(c.debtHours),
                        )
                    } else {
                        DreamFootnote(
                            "Low debt — your curve is running at full amplitude.",
                        )
                    }
                }
            } ?: Column {
                Text(
                    text = "Computing your circadian rhythm…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

private fun hhmm(time: LocalTime): String = String.format(Locale.US, "%02d:%02d", time.hour, time.minute)
