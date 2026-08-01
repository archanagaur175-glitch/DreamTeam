package com.dreamteam.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One sleep session (one night). Times stored as epoch millis.
 * [source] is a [com.dreamteam.core.common.SleepSource] name: MANUAL | SENSOR.
 */
@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sleepStartEpochMillis: Long,
    val sleepEndEpochMillis: Long,
    val source: String,
    val qualityScore: Int? = null,
    val notes: String? = null,
)
