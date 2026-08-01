package com.dreamteam.feature.smartalarm.domain

/**
 * Decides when a rolling movement score indicates "light sleep" so the smart alarm
 * can fire early. Pure, deterministic logic — unit tested.
 *
 * Rationale: accelerometer movement correlates with lighter sleep stages. We
 * require a movement score at/above [lightSleepThreshold] for
 * [requiredConsecutiveWindows] consecutive evaluation windows before triggering,
 * which suppresses one-off twitches. Phone-sensor light-sleep detection is
 * modestly accurate (~50–70% vs. clinical PSG in public literature); this is a
 * best-effort signal, never a guarantee.
 */
class MovementScoreDetector(
    private val lightSleepThreshold: Double = 0.35,
    private val requiredConsecutiveWindows: Int = 2,
) {
    private var streak = 0

    /** Feed one window's movement score (std-dev of acceleration magnitude, m/s²). */
    fun evaluate(score: Double): Boolean {
        streak = if (score >= lightSleepThreshold) streak + 1 else 0
        return streak >= requiredConsecutiveWindows
    }

    fun reset() {
        streak = 0
    }
}
