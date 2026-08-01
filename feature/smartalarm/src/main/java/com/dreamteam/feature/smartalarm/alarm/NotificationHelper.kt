package com.dreamteam.feature.smartalarm.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dreamteam.feature.smartalarm.R

object NotificationHelper {

    const val CHANNEL_MONITORING = "smart_wake_monitoring"
    const val CHANNEL_ALARM = "alarm"
    const val MONITORING_NOTIFICATION_ID = 2001
    const val ALARM_NOTIFICATION_ID = 2002

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_MONITORING, "Smart wake monitoring", NotificationManager.IMPORTANCE_LOW)
                .apply { setShowBadge(false) },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ALARM, "Alarm", NotificationManager.IMPORTANCE_HIGH)
                .apply { setShowBadge(false) },
        )
    }

    fun monitoringNotification(context: Context, targetTimeText: String): Notification =
        NotificationCompat.Builder(context, CHANNEL_MONITORING)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle("Smart wake active")
            .setContentText("Watching for light sleep until $targetTimeText")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

    /** POST_NOTIFICATIONS is a runtime permission on API 33+. */
    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
