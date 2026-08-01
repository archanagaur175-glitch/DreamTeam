package com.dreamteam.feature.smartalarm.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreamteam.core.common.toLocalTime
import com.dreamteam.core.common.toMinutesSinceMidnight
import com.dreamteam.core.ui.component.AlarmWindowArc
import com.dreamteam.core.ui.component.DreamBackground
import com.dreamteam.core.ui.component.DreamButton
import com.dreamteam.core.ui.component.DreamFootnote
import com.dreamteam.core.ui.component.SectionHeader
import com.dreamteam.core.ui.component.ThemeChip
import com.dreamteam.core.ui.theme.AccentAmber
import com.dreamteam.core.ui.theme.AccentIndigo
import com.dreamteam.core.ui.theme.AccentViolet
import com.dreamteam.core.ui.theme.Dimens
import com.dreamteam.core.ui.theme.NightSurface
import com.dreamteam.core.ui.theme.SuccessTeal
import com.dreamteam.core.ui.theme.TextPrimary
import com.dreamteam.core.ui.theme.TextSecondary
import com.dreamteam.feature.smartalarm.alarm.NotificationHelper
import com.dreamteam.feature.smartalarm.domain.AlarmConfig
import com.dreamteam.feature.smartalarm.domain.SensorMode
import java.time.LocalTime
import java.util.Locale

@Composable
fun AlarmSetupScreen(
    onBack: () -> Unit,
    viewModel: AlarmSetupViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    var targetTime by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var windowMinutes by remember { mutableStateOf(30) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var sensorMode by remember { mutableStateOf(SensorMode.ACCELEROMETER) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        config?.let {
            targetTime = it.targetWakeTime
            windowMinutes = it.windowMinutes
            vibrationEnabled = it.vibrationEnabled
            sensorMode = it.sensorMode
        }
    }

    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { }

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
                    text = "Smart wake alarm",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
            }

            // --- target time hero ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                Text(
                    text = "Wake target",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Text(
                    text = hhmm(targetTime),
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                )
                AlarmWindowArc(
                    windowStart = targetTime.minusMinutes(windowMinutes.toLong()),
                    targetTime = targetTime,
                    isActive = false,
                    modifier = Modifier.size(180.dp),
                )
                Text(
                    text = "Monitoring window ${hhmm(targetTime.minusMinutes(windowMinutes.toLong()))} – ${hhmm(targetTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                DreamButton(
                    text = "Change wake time",
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // --- window length ---
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                SectionHeader("Wake window")
                Text(
                    text = "Window: $windowMinutes min — monitoring starts ${hhmm(targetTime.minusMinutes(windowMinutes.toLong()))}. " +
                        "The alarm fires at the first light-sleep signal inside the window, or at " +
                        "${hhmm(targetTime)} at the latest. Never later than requested.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Slider(
                    value = windowMinutes.toFloat(),
                    onValueChange = { windowMinutes = it.toInt() },
                    valueRange = 10f..60f,
                    steps = 4,
                )
            }

            // --- sensing ---
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                SectionHeader("Light-sleep sensing")
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    ThemeChip(
                        text = "Accelerometer",
                        selected = sensorMode == SensorMode.ACCELEROMETER,
                        onClick = { sensorMode = SensorMode.ACCELEROMETER },
                    )
                    ThemeChip(
                        text = "Off (fixed time)",
                        selected = sensorMode == SensorMode.OFF,
                        onClick = { sensorMode = SensorMode.OFF },
                    )
                }
                DreamFootnote(
                    "Phone-sensor light-sleep detection is modestly accurate (~50–70% vs. clinical sleep " +
                        "studies). It may wake you a few minutes early — never late. Microphone sensing " +
                        "arrives in a later version.",
                )
            }

            // --- vibration ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vibration", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        "Escalating pulse alongside the fade-in tone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
            }

            // --- permissions ---
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                SectionHeader("Permissions")
                val notificationsGranted = NotificationHelper.canPostNotifications(context)
                PermissionRow(
                    title = "Notifications",
                    subtitle = if (notificationsGranted) "Granted — full-screen wake works" else "Needed for the lock-screen alarm",
                    granted = notificationsGranted,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
                val batteryGranted = isIgnoringBatteryOptimizations(context)
                PermissionRow(
                    title = "Battery optimization",
                    subtitle = if (batteryGranted) "Exempt — sensor window will run reliably" else "Recommended so Doze can't stall monitoring",
                    granted = batteryGranted,
                    onRequest = {
                        if (!batteryGranted) {
                            runCatching {
                                batteryLauncher.launch(
                                    Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:${context.packageName}"),
                                    ),
                                )
                            }
                        }
                    },
                )
                DreamFootnote(
                    "Scheduling uses setAlarmClock(), which does not require special access and works even " +
                        "if exact-alarm permission is denied.",
                )
            }

            // --- actions ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                if (config != null || saved) {
                    Text(
                        text = "Armed — ${hhmm(targetTime)} · window ${windowMinutes} min",
                        style = MaterialTheme.typography.labelLarge,
                        color = SuccessTeal,
                    )
                    DreamButton(
                        text = "Cancel alarm",
                        onClick = viewModel::cancelAlarm,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    DreamButton(
                        text = "Arm smart alarm",
                        onClick = {
                            viewModel.saveAndSchedule(
                                AlarmConfig(
                                    targetWakeTime = targetTime,
                                    windowMinutes = windowMinutes,
                                    vibrationEnabled = vibrationEnabled,
                                    sensorMode = sensorMode,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = targetTime.hour,
            initialMinute = targetTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        targetTime = LocalTime.of(state.hour, state.minute)
                        showTimePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) },
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    granted: Boolean,
    onRequest: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        ThemeChip(
            text = if (granted) "Done" else "Allow",
            selected = granted,
            onClick = onRequest,
        )
    }
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun hhmm(time: LocalTime): String =
    String.format(Locale.US, "%02d:%02d", time.hour, time.minute)
