package com.dreamteam.feature.smartalarm.alarm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dreamteam.feature.smartalarm.R
import com.dreamteam.feature.smartalarm.ui.AlarmRingingActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Launches the full-screen alarm experience: tries a direct activity launch and
 * posts a full-screen-intent notification so the ringing UI surfaces even when the
 * device is locked or the app is in the background.
 */
class AlarmTrigger @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @SuppressLint("NotificationPermission")
    fun fire(targetMillis: Long = System.currentTimeMillis()) {
        val intent = Intent(context, AlarmRingingActivity::class.java)
            .putExtra(AlarmRingingActivity.EXTRA_TARGET_MILLIS, targetMillis)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pending = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Direct launch works when we may start activities; FSI covers the rest.
        runCatching { context.startActivity(intent) }

        if (NotificationHelper.canPostNotifications(context)) {
            val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ALARM)
                .setSmallIcon(R.drawable.ic_stat_alarm)
                .setContentTitle("Smart wake")
                .setContentText("Time to wake up")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pending, true)
                .setAutoCancel(true)
                .build()
            runCatching {
                NotificationManagerCompat.from(context).notify(
                    NotificationHelper.ALARM_NOTIFICATION_ID,
                    notification,
                )
            }
        }
    }

    private companion object {
        const val REQUEST_CODE = 201
    }
}
