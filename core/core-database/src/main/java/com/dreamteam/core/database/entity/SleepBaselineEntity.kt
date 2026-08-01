package com.dreamteam.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** User-editable baseline sleep need (hours per night). Single row per user. */
@Entity(tableName = "sleep_baseline")
data class SleepBaselineEntity(
    @PrimaryKey val userId: String = DEFAULT_USER_ID,
    val targetSleepHours: Double = 8.0,
    val updatedAtEpochMillis: Long,
) {
    companion object {
        const val DEFAULT_USER_ID = "default"
    }
}
