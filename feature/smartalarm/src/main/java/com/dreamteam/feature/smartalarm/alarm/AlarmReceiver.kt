package com.dreamteam.feature.smartalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dreamteam.feature.smartalarm.domain.AlarmConfigRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Handles the alarm lifecycle: window-open (start monitoring), hard fallback (fire),
 * and BOOT_COMPLETED (re-arm a persisted alarm).
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var configRepository: AlarmConfigRepository
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject lateinit var trigger: AlarmTrigger

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmScheduler.ACTION_WINDOW_OPEN -> startMonitoring(context, intent)
            AlarmScheduler.ACTION_FIRE_ALARM -> fire(context, intent)
            Intent.ACTION_BOOT_COMPLETED -> rescheduleOnBoot(context)
        }
    }

    private fun startMonitoring(context: Context, intent: Intent) {
        val config = runBlocking { configRepository.observeConfig().firstOrNull() } ?: return
        // Use the exact target the scheduler armed, so a late-firing alarm can't
        // slip to the wrong day via a recomputed nextTarget().
        val targetMillis = intent.getLongExtra(
            AlarmScheduler.EXTRA_TARGET_MILLIS,
            System.currentTimeMillis(),
        )
        val serviceIntent = Intent(context, SmartAlarmMonitoringService::class.java)
            .putExtra(SmartAlarmMonitoringService.EXTRA_TARGET_MILLIS, targetMillis)
            .putExtra(SmartAlarmMonitoringService.EXTRA_WINDOW_MINUTES, config.windowMinutes)
        androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
    }

    private fun fire(context: Context, intent: Intent) {
        val targetMillis = intent.getLongExtra(
            AlarmScheduler.EXTRA_TARGET_MILLIS,
            System.currentTimeMillis(),
        )
        context.stopService(Intent(context, SmartAlarmMonitoringService::class.java))
        trigger.fire(targetMillis)
    }

    private fun rescheduleOnBoot(context: Context) {
        val config = runBlocking { configRepository.observeConfig().firstOrNull() } ?: return
        scheduler.schedule(config)
    }
}
