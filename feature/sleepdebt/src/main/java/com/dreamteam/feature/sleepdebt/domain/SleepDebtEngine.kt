package com.dreamteam.feature.sleepdebt.domain

import com.dreamteam.core.common.SleepSession
import java.time.LocalDate

/** One night's contribution to the rolling debt window. */
data class DailyDebt(
    val nightDate: LocalDate,
    /** Actual sleep hours, or null when the night has no data. */
    val actualHours: Double?,
    val deficitHours: Double,
)

/** Rolling sleep debt over the trailing [windowDays] nights. Derived, never stored. */
data class RollingDebt(
    val windowStart: LocalDate,
    val windowEnd: LocalDate,
    val windowDays: Int,
    val nightsWithData: Int,
    val coverageRatio: Double,
    val isConfident: Boolean,
    val totalDeficitHours: Double,
    val avgDeficitHours: Double,
    val dailyBreakdown: List<DailyDebt>,
    /** Total deficit of the PREVIOUS window, when data exists. */
    val previousWindowTotalHours: Double?,
    /** total − previous; negative means the debt is being paid down. */
    val trendHours: Double?,
)

/**
 * Sleep Debt Engine — original implementation of the publicly understood concept
 * of a rolling sleep-debt window (as popularized by Rise), not a reproduction of
 * any proprietary formula.
 *
 * Contract:
 *  - A "night" is attributed to the date its sleep ENDS on (the wake date).
 *  - `nightlyDeficit = max(0, baselineHours − actualSleepHours)`. A great night
 *    cannot refund debt; debt only ever accrues.
 *  - Nights without data are EXCLUDED from the sum and surfaced via
 *    [RollingDebt.coverageRatio] / [RollingDebt.isConfident] instead of silently
 *    assuming 0 debt.
 *  - The oldest night falls off the window each morning (recomputed on read).
 */
object SleepDebtEngine {

    const val DEFAULT_WINDOW_DAYS = 14
    const val MAX_VALID_SESSION_HOURS = 18.0
    const val MIN_VALID_SESSION_HOURS = 0.5
    const val CONFIDENT_COVERAGE = 8.0 / 14.0

    fun computeRollingDebt(
        sessions: List<SleepSession>,
        baselineHours: Double,
        today: LocalDate,
        windowDays: Int = DEFAULT_WINDOW_DAYS,
    ): RollingDebt {
        require(baselineHours > 0) { "baselineHours must be positive" }

        val windowStart = today.minusDays((windowDays - 1).toLong())
        val windowEnd = today

        val breakdown = (0 until windowDays).map { offset ->
            val date = windowStart.plusDays(offset.toLong())
            val session = bestSessionForNight(sessions, date)
            if (session != null) {
                DailyDebt(
                    nightDate = date,
                    actualHours = session.durationHours,
                    deficitHours = maxOf(0.0, baselineHours - session.durationHours),
                )
            } else {
                DailyDebt(nightDate = date, actualHours = null, deficitHours = 0.0)
            }
        }

        val withData = breakdown.filter { it.actualHours != null }
        val nightsWithData = withData.size
        val coverage = nightsWithData / windowDays.toDouble()
        val total = withData.sumOf { it.deficitHours }

        return RollingDebt(
            windowStart = windowStart,
            windowEnd = windowEnd,
            windowDays = windowDays,
            nightsWithData = nightsWithData,
            coverageRatio = coverage,
            isConfident = coverage >= CONFIDENT_COVERAGE,
            totalDeficitHours = total,
            avgDeficitHours = if (nightsWithData > 0) total / nightsWithData else 0.0,
            dailyBreakdown = breakdown,
            previousWindowTotalHours = previousWindowTotal(sessions, baselineHours, today, windowDays),
            trendHours = null, // computed below
        ).let { debt ->
            debt.copy(trendHours = debt.previousWindowTotalHours?.let { debt.totalDeficitHours - it })
        }
    }

    /** Longest valid session ending on [nightWakeDate]; null when none/invalid. */
    fun bestSessionForNight(sessions: List<SleepSession>, nightWakeDate: LocalDate): SleepSession? =
        sessions
            .asSequence()
            .filter { it.sleepEnd.toLocalDate() == nightWakeDate }
            .filter { it.durationHours in MIN_VALID_SESSION_HOURS..MAX_VALID_SESSION_HOURS }
            .maxByOrNull { it.durationHours }

    private fun previousWindowTotal(
        sessions: List<SleepSession>,
        baselineHours: Double,
        today: LocalDate,
        windowDays: Int,
    ): Double? {
        val prevEnd = today.minusDays(windowDays.toLong())
        val prevStart = today.minusDays((windowDays * 2 - 1).toLong())
        val nights = sessions
            .asSequence()
            .filter { it.sleepEnd.toLocalDate() in prevStart..prevEnd }
            .filter { it.durationHours in MIN_VALID_SESSION_HOURS..MAX_VALID_SESSION_HOURS }
            .toList()
        if (nights.isEmpty()) return null
        return nights.sumOf { maxOf(0.0, baselineHours - it.durationHours) }
    }
}
