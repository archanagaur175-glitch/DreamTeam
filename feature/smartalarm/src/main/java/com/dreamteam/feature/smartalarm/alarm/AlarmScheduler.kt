package com.dreamteam.feature.smartalarm.alarm

import android.annotation.SuppressLint
import android.app.AlarmClockInfo
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dreamteam.feature.smartalarm.domain.AlarmConfig
import com.dreamteam.feature.smartalarm.ui.AlarmRingingActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Schedules the smart wake window.
 *
 * Both the window-open trigger and the hard fallback use [AlarmManager.setAlarmClock],
 * which is EXEMPT from the exact-alarm permission requirements and Doze-safe. The
 * hard fallback additionally tries setExactAndAllowWhileIdle for precision and
 * gracefully falls back to setAlarmClock when the permission is unavailable.
 */
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    /** Arms both the window-open trigger and the hard fallback. */
    @SuppressLint("MissingPermission")
    fun schedule(config: AlarmConfig) {
        val target = nextTarget(config.targetWakeTime)
        val windowStart = target.minusMinutes(config.windowMinutes.toLong())
        val targetMillis = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val windowMillis = windowStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Window-open: start the sensor-monitoring foreground service.
        alarmManager.setAlarmClock(
            AlarmClockInfo(targetMillis, showPendingIntent(targetMillis)),
            windowOpenPendingIntent(windowMillis, targetMillis),
        )

        // Hard fallback: never later than requested.
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetMillis,
                hardFallbackPendingIntent(targetMillis),
            )
        } catch (_: SecurityException) {
            alarmManager.setAlarmClock(
                AlarmClockInfo(targetMillis, showPendingIntent(targetMillis)),
                hardFallbackPendingIntent(targetMillis),
            )
        }
    }

    fun cancel() {
        alarmManager.cancel(windowOpenPendingIntent(0, 0))
        alarmManager.cancel(hardFallbackPendingIntent(0))
        alarmManager.cancel(snoozePendingIntent(0))
    }

    /** Re-fires the alarm [minutes] from now (default snooze). */
    @SuppressLint("MissingPermission")
    fun snooze(minutes: Int = 9) {
        val t = System.currentTimeMillis() + minutes * 60_000L
        val pi = snoozePendingIntent(t)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
        } catch (_: SecurityException) {
            alarmManager.setAlarmClock(AlarmClockInfo(t, showPendingIntent(t)), pi)
        }
    }

    /** Next occurrence of [wakeTime] that is in the future. */
    fun nextTarget(wakeTime: LocalTime): LocalDateTime {
        var target = LocalDateTime.now()
            .withHour(wakeTime.hour)
            .withMinute(wakeTime.minute)
            .withSecond(0)
            .withNano(0)
        if (!target.isAfter(LocalDateTime.now().plusMinutes(1))) {
            target = target.plusDays(1)
        }
        return target
    }

    private fun windowOpenPendingIntent(windowMillis: Long, targetMillis: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_WINDOW_OPEN)
            .putExtra(EXTRA_WINDOW_MILLIS, windowMillis)
            .putExtra(EXTRA_TARGET_MILLIS, targetMillis)
        return PendingIntent.getBroadcast(
            context, RC_WINDOW_OPEN, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun hardFallbackPendingIntent(targetMillis: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_FIRE_ALARM)
            .putExtra(EXTRA_TARGET_MILLIS, targetMillis)
        return PendingIntent.getBroadcast(
            context, RC_HARD_FALLBACK, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun snoozePendingIntent(targetMillis: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_FIRE_ALARM)
            .putExtra(EXTRA_TARGET_MILLIS, targetMillis)
        return PendingIntent.getBroadcast(
            context, RC_SNOOZE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun showPendingIntent(targetMillis: Long): PendingIntent {
        val intent = Intent(context, AlarmRingingActivity::class.java)
            .putExtra(AlarmRingingActivity.EXTRA_TARGET_MILLIS, targetMillis)
        return PendingIntent.getActivity(
            context, RC_SHOW, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_WINDOW_OPEN = "com.dreamteam.action.WINDOW_OPEN"
        const val ACTION_FIRE_ALARM = "com.dreamteam.action.FIRE_ALARM"
        const val EXTRA_WINDOW_MILLIS = "windowMillis"
        const val EXTRA_TARGET_MILLIS = "targetMillis"

        private const val RC_WINDOW_OPEN = 101
        private const val RC_HARD_FALLBACK = 102
        private const val RC_SNOOZE = 103
        private const val RC_SHOW = 104
    }
}
