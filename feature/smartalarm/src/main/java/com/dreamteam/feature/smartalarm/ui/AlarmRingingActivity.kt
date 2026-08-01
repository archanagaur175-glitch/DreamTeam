package com.dreamteam.feature.smartalarm.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dreamteam.core.ui.theme.DreamTeamTheme
import com.dreamteam.feature.smartalarm.alarm.AlarmScheduler
import com.dreamteam.feature.smartalarm.domain.AlarmConfigRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Full-screen, lock-screen-ready alarm UI. Plays a gentle volume ramp (~20s fade-in)
 * with optional escalating vibration; the user can snooze (9 min) or dismiss.
 */
@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    @Inject lateinit var scheduler: AlarmScheduler
    @Inject lateinit var configRepository: AlarmConfigRepository

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setShowWhenLocked/setTurnScreenOn are API 27+; the manifest attributes cover
        // newer devices, this guard covers the minSdk 26 case gracefully.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val targetMillis = intent?.getLongExtra(EXTRA_TARGET_MILLIS, System.currentTimeMillis())
            ?: System.currentTimeMillis()
        val config = runBlocking { configRepository.observeConfig().firstOrNull() }

        startRinging(config?.soundUri, config?.vibrationEnabled != false)

        setContent {
            DreamTeamTheme {
                AlarmRingingScreen(
                    targetMillis = targetMillis,
                    onDismiss = { dismiss() },
                    onSnooze = { snooze() },
                )
            }
        }
    }

    private fun startRinging(soundUri: String?, vibrationEnabled: Boolean) {
        val uri: Uri = soundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        mediaPlayer = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@AlarmRingingActivity, uri)
                isLooping = true
                setVolume(0f, 0f)
                prepare()
                start()
            }
        }.getOrNull()

        rampVolume()
        if (vibrationEnabled) startVibration()
    }

    /** Fade volume 0 → 1 over ~20 seconds (40 ticks × 500ms). */
    private fun rampVolume() {
        handler.post(object : Runnable {
            var step = 0
            override fun run() {
                val progress = (step / 40f).coerceIn(0f, 1f)
                mediaPlayer?.setVolume(progress, progress)
                step++
                if (step <= 40) handler.postDelayed(this, 500)
            }
        })
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 500, 400, 500, 400, 800), 1),
            )
        }
    }

    private fun snooze() {
        stopRinging()
        scheduler.snooze(9)
        finishAndRemoveTask()
    }

    private fun dismiss() {
        stopRinging()
        finishAndRemoveTask()
    }

    private fun stopRinging() {
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.let {
            runCatching { it.stop() }
            it.release()
        }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TARGET_MILLIS = "targetMillis"
    }
}
