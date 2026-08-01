package com.dreamteam.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Daily factor log. [dateEpochDay] is the epoch day the log belongs to.
 * [tags] is a comma-separated list of [com.dreamteam.core.common.FactorTag] names.
 */
@Entity(tableName = "factor_logs")
data class DailyFactorLogEntity(
    @PrimaryKey val dateEpochDay: Long,
    val tags: String,
    val freeNote: String? = null,
)
