package com.dreamteam.feature.smartalarm.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.dreamteam.core.common.stdDev
import com.dreamteam.core.common.toLocalDateTime
import com.dreamteam.feature.smartalarm.domain.MovementScoreDetector
import java.util.Locale

/**
 * Foreground service that samples the accelerometer during the wake window and
 * fires the alarm at the first light-sleep movement signal. If no signal arrives
 * by the hard target time, the AlarmManager fallback fires anyway — never late.
 *
 * Foreground-service type is `health` (declared in the manifest), which is
 * appropriate for biometric/sensor monitoring; Android 15's 6h health-FGS limit is
 * irrelevant for a ≤60-minute window. Exact-alarm broadcasts carry a background
 * FGS-start exemption, so this can start from the AlarmReceiver.
 */
class SmartAlarmMonitoringService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val samples = ArrayList<Float>()
    private val detector = MovementScoreDetector()
    private var source: SleepSignalSource? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var targetMillis = 0L

    private val tick = object : Runnable {
        override fun run() {
            evaluate()
            handler.postDelayed(this, TICK_MILLIS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetMillis = intent?.getLongExtra(EXTRA_TARGET_MILLIS, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        startAsForeground(targetMillis)
        acquireWakeLock()
        startMonitoring()
        return START_STICKY
    }

    private fun startAsForeground(targetMillis: Long) {
        val targetTime = targetMillis.toLocalDateTime().toLocalTime()
        val notification = NotificationHelper.monitoringNotification(
            this,
            String.format(Locale.US, "%02d:%02d", targetTime.hour, targetTime.minute),
        )
        ServiceCompat.startForeground(
            this,
            NotificationHelper.MONITORING_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
        )
    }

    private fun acquireWakeLock() {
        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "dreamteam:smartwake")
            .apply { acquire(MAX_WINDOW_MILLIS) }
    }

    private fun startMonitoring() {
        if (source != null) return
        val accelerometer = AccelerometerSignalSource(this)
        source = accelerometer
        accelerometer.start { deviation ->
            if (samples.size >= MAX_BUFFER_SAMPLES) samples.removeAt(0)
            samples.add(deviation)
        }
        handler.post(tick)
    }

    private fun evaluate() {
        val now = System.currentTimeMillis()

        // Hard deadline: the AlarmManager fallback fires at target time; stop monitoring.
        if (now >= targetMillis) {
            stopSelf()
            return
        }

        // Rolling movement score: std-dev of recent magnitude deviations from gravity.
        val recent = samples.takeLast(WINDOW_SAMPLE_COUNT).map { it.toDouble() }
        val score = stdDev(recent)
        if (detector.evaluate(score)) {
            // Light-sleep signal detected inside the window -> fire early.
            AlarmTrigger(this).fire(targetMillis)
            stopSelf()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        source?.stop()
        source = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_TARGET_MILLIS = "targetMillis"
        const val EXTRA_WINDOW_MINUTES = "windowMinutes"

        private const val DEFAULT_WINDOW_MINUTES = 30
        private const val TICK_MILLIS = 5_000L
        private const val MAX_BUFFER_SAMPLES = 256
        // ~15s @ ~5Hz
        private const val WINDOW_SAMPLE_COUNT = 75
        // Cap the wake lock at the longest supported window (60 min) + margin.
        private const val MAX_WINDOW_MILLIS = 90 * 60_000L
    }
}
