package com.dreamteam.feature.smartalarm.domain

import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

/** Which signal source the monitoring window uses. */
enum class SensorMode { ACCELEROMETER, MICROPHONE, OFF }

/**
 * Smart wake alarm configuration. The window opens [windowMinutes] before
 * [targetWakeTime]; the alarm fires at the first light-sleep signal inside the
 * window, or at [targetWakeTime] at the latest (never later than requested).
 */
data class AlarmConfig(
    val targetWakeTime: LocalTime,
    val windowMinutes: Int,
    val soundUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val sensorMode: SensorMode = SensorMode.ACCELEROMETER,
)

/** Data boundary for the smart alarm feature. Implemented in the app module. */
interface AlarmConfigRepository {

    fun observeConfig(): Flow<AlarmConfig?>

    /** Persists the config AND arms the alarm with the system scheduler. */
    suspend fun save(config: AlarmConfig)

    /** Removes the config AND cancels any armed alarm. */
    suspend fun clear()
}
