package com.dreamteam.core.common

import java.time.Duration
import java.time.LocalDateTime

/** Where a sleep session came from. */
enum class SleepSource { MANUAL, SENSOR }

/**
 * A single sleep session (one night). Pure domain model shared across features —
 * lives in core so the data layer and every feature can reference it without
 * cross-feature dependencies.
 */
data class SleepSession(
    val id: Long = 0,
    val sleepStart: LocalDateTime,
    val sleepEnd: LocalDateTime,
    val source: SleepSource = SleepSource.MANUAL,
    /** Optional 0..100 self-reported quality. */
    val qualityScore: Int? = null,
    val notes: String? = null,
) {
    /** Actual sleep hours, as a double. */
    val durationHours: Double
        get() = Duration.between(sleepStart, sleepEnd).toMinutes() / 60.0
}
