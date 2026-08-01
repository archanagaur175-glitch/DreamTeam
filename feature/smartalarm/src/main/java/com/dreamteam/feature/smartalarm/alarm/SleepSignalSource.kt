package com.dreamteam.feature.smartalarm.alarm

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Abstraction over a "what is the sleeper doing" signal source. The alarm-decision
 * logic depends only on this interface, so microphone, wearable, or future sources
 * can be swapped in without touching [com.dreamteam.feature.smartalarm.domain.MovementScoreDetector]
 * or the monitoring service.
 *
 * @param onSample receives acceleration magnitude deviation from gravity (m/s²),
 *   roughly 5×/second at SENSOR_DELAY_NORMAL.
 */
interface SleepSignalSource {
    fun start(onSample: (Float) -> Unit)
    fun stop()
}

/** Accelerometer-based source (v1 default). */
class AccelerometerSignalSource(
    private val context: Context,
    private val batchDelayMicros: Int = 500_000,
) : SleepSignalSource, SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var listener: ((Float) -> Unit)? = null

    override fun start(onSample: (Float) -> Unit) {
        listener = onSample
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm
        val accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, batchDelayMicros)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        // Deviation from gravity: still body ≈ 0, movement produces spikes.
        listener?.invoke(magnitude - SensorManager.GRAVITY_EARTH)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        listener = null
    }
}
