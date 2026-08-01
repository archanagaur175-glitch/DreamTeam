package com.dreamteam.feature.logger.domain

import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.SleepSession
import java.time.LocalDate

/** One tag's with/without comparison. */
data class CorrelationResult(
    val tag: FactorTag,
    val nightsWith: Int,
    val meanSleepWith: Double?,
    val nightsWithout: Int,
    val meanSleepWithout: Double?,
    /** withMean − withoutMean, in hours. Positive = tag associated with MORE sleep. */
    val deltaHours: Double?,
    val enoughData: Boolean,
)

/**
 * Simple, honest association statistics — never causation.
 *
 * A night with wake date W is associated with the factor log of day W−1 ("nights
 * following days tagged X"). Both groups require a logged day so we compare like
 * with like; small samples are reported as insufficient data.
 */
object CorrelationCalculator {

    const val MIN_NIGHTS_PER_GROUP = 2
    const val MIN_VALID_HOURS = 0.5
    const val MAX_VALID_HOURS = 18.0

    fun compute(
        logs: List<DailyLog>,
        sessions: List<SleepSession>,
        today: LocalDate,
        days: Int = 30,
    ): List<CorrelationResult> {
        val cutoff = today.minusDays(days.toLong())
        val nights = sessions
            .asSequence()
            .filter { it.sleepEnd.toLocalDate() >= cutoff }
            .filter { it.durationHours in MIN_VALID_HOURS..MAX_VALID_HOURS }
            .toList()
        val logByDate = logs.associateBy { it.date }

        return FactorTag.entries.map { tag ->
            val with = nights.filter { night ->
                val log = logByDate[night.sleepEnd.toLocalDate().minusDays(1)]
                log != null && tag in log.tags
            }
            val without = nights.filter { night ->
                val log = logByDate[night.sleepEnd.toLocalDate().minusDays(1)]
                log != null && tag !in log.tags
            }
            val withMean = meanHours(with)
            val withoutMean = meanHours(without)
            CorrelationResult(
                tag = tag,
                nightsWith = with.size,
                meanSleepWith = withMean,
                nightsWithout = without.size,
                meanSleepWithout = withoutMean,
                deltaHours = if (withMean != null && withoutMean != null) withMean - withoutMean else null,
                enoughData = with.size >= MIN_NIGHTS_PER_GROUP && without.size >= MIN_NIGHTS_PER_GROUP,
            )
        }
    }

    private fun meanHours(nights: List<SleepSession>): Double? =
        if (nights.isEmpty()) null else nights.map { it.durationHours }.average()
}
