package com.dreamteam.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Smart wake alarm configuration (single row). [targetWakeTimeMinutes] is minutes
 * since midnight; [sensorMode] is a [com.dreamteam.feature.smartalarm.domain.SensorMode]
 * name: ACCELEROMETER | MICROPHONE | OFF.
 */
@Entity(tableName = "alarm_config")
data class AlarmConfigEntity(
    @PrimaryKey val id: Int = 1,
    val targetWakeTimeMinutes: Int = 7 * 60,
    val windowMinutes: Int = 30,
    val soundUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val sensorMode: String = "ACCELEROMETER",
)
