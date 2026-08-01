package com.dreamteam.feature.sleepdebt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreamteam.core.ui.component.DebtRing
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamFootnote
import com.dreamteam.core.ui.component.SectionHeader
import com.dreamteam.core.ui.component.StatCard
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightBorder
import com.dreamteam.core.ui.theme.NightSurface
import com.dreamteam.core.ui.theme.SuccessTeal
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import com.dreamteam.feature.sleepdebt.domain.RollingDebt
import java.util.Locale

@Composable
fun DebtScreen(
    onBack: () -> Unit,
    viewModel: DebtViewModel = hiltViewModel(),
) {
    val debt by viewModel.debt.collectAsStateWithLifecycle()

    DreamBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary,
                        )
                    }
                    Text(
                        text = "Sleep debt",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                }
            }

            item {
                debt?.let { d -> DebtHero(d) } ?: DebtHero(
                    RollingDebt(
                        windowStart = java.time.LocalDate.now(),
                        windowEnd = java.time.LocalDate.now(),
                        windowDays = 14,
                        nightsWithData = 0,
                        coverageRatio = 0.0,
                        isConfident = false,
                        totalDeficitHours = 0.0,
                        avgDeficitHours = 0.0,
                        dailyBreakdown = emptyList(),
                        previousWindowTotalHours = null,
                        trendHours = null,
                    )
                )
            }

            item {
                debt?.let { d ->
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                        SectionHeader("How debt works")
                        Text(
                            text = "Each night you owe ${"%.1f".format(d.totalDeficitHours)}h over your baseline. " +
                                "The last ${d.windowDays} nights roll as a running total — a good night can't erase it " +
                                "instantly, only the trend moves it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        if (!d.isConfident) {
                            DreamFootnote(
                                "Only ${d.nightsWithData}/${d.windowDays} nights have data — this number is not yet confident.",
                            )
                        }
                    }
                }
            }

            item {
                debt?.let { d -> DailyBreakdown(d) }
            }
        }
    }
}

@Composable
private fun DebtHero(debt: RollingDebt) {
    val trendColor = when {
        debt.trendHours == null -> androidx.compose.ui.graphics.Color.Unspecified
        debt.trendHours!! < 0 -> SuccessTeal
        else -> AccentAmber
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.lg),
    ) {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            DebtRing(
                debtHours = debt.totalDeficitHours,
                centerValue = String.format(Locale.US, "%.1f", debt.totalDeficitHours),
                centerSub = "hours owed",
                modifier = Modifier.fillMaxSize(),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
            StatCard(
                label = "Coverage",
                value = "${debt.nightsWithData}/${debt.windowDays}",
                unit = "nights",
                accent = AccentViolet,
            )
            debt.trendHours?.let { trend ->
                StatCard(
                    label = "Trend",
                    value = if (trend < 0) "%.1f".format(trend) else "+%.1f".format(trend),
                    unit = "h vs last window",
                    accent = trendColor,
                )
            }
        }
    }
}

@Composable
private fun DailyBreakdown(debt: RollingDebt) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        SectionHeader("Last ${debt.windowDays} nights")
        debt.dailyBreakdown.forEach { day ->
            val isMissing = day.actualHours == null
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NightSurface, RoundedCornerShape(Dimens.cardCorner))
                    .padding(horizontal = Dimens.lg, vertical = Dimens.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                Text(
                    text = day.nightDate.dayOfMonth.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    modifier = Modifier.size(width = 24.dp, height = 16.dp),
                    textAlign = TextAlign.Center,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMissing) "No data" else "%.1f h".format(day.actualHours),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMissing) androidx.compose.ui.graphics.Color(0xFF5D5B6E) else TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(NightBorder, RoundedCornerShape(50)),
                    ) {
                        val frac = (day.deficitHours / 14.0).toFloat().coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(frac)
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(AccentViolet, AccentAmber)),
                                    RoundedCornerShape(50),
                                ),
                        )
                    }
                }
                Text(
                    text = if (isMissing) "—" else "%.1f".format(day.deficitHours),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (day.deficitHours > 0.01) AccentAmber else SuccessTeal,
                )
            }
        }
    }
}
