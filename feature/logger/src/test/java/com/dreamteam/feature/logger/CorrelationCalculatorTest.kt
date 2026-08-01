package com.dreamteam.feature.logger

import com.dreamteam.core.common.FactorTag
import com.dreamteam.core.common.SleepSession
import com.dreamteam.feature.logger.domain.CorrelationCalculator
import com.dreamteam.feature.logger.domain.DailyLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CorrelationCalculatorTest {

    private val today: LocalDate = LocalDate.of(2026, 7, 14)

    private fun session(nightsAgo: Long, hours: Double): SleepSession {
        val wake = today.minusDays(nightsAgo).atTime(7, 0)
        return SleepSession(
            sleepStart = wake.minusMinutes((hours * 60).toLong()),
            sleepEnd = wake,
        )
    }

    private fun log(day: LocalDate, vararg tags: FactorTag): DailyLog = DailyLog(day, tags.toSet())

    @Test
    fun withVsWithout_usesNightsFollowingTaggedDays() {
        val sessions = (0L until 10L).map { session(it, 7.0) }
        val logs = listOf(
            log(today.minusDays(1), FactorTag.CAFFEINE),
            log(today.minusDays(2), FactorTag.ALCOHOL),
            log(today.minusDays(3), FactorTag.CAFFEINE),
            log(today.minusDays(4), FactorTag.ALCOHOL),
            log(today.minusDays(5), FactorTag.CAFFEINE),
            log(today.minusDays(6), FactorTag.ALCOHOL),
            log(today.minusDays(7), FactorTag.CAFFEINE),
            log(today.minusDays(8), FactorTag.ALCOHOL),
        )
        val results = CorrelationCalculator.compute(logs, sessions, today)
        val caffeine = results.first { it.tag == FactorTag.CAFFEINE }
        assertEquals(4, caffeine.nightsWith)
        assertEquals(4, caffeine.nightsWithout)
        assertTrue(caffeine.enoughData)
        assertEquals(7.0, caffeine.meanSleepWith!!, 0.001)
        assertEquals(7.0, caffeine.meanSleepWithout!!, 0.001)
        assertEquals(0.0, caffeine.deltaHours!!, 0.001)
    }

    @Test
    fun insufficientData_isFlaggedHonestly() {
        val results = CorrelationCalculator.compute(emptyList(), listOf(session(0, 7.0)), today)
        val caffeine = results.first { it.tag == FactorTag.CAFFEINE }
        assertFalse(caffeine.enoughData)
        assertEquals(0, caffeine.nightsWith)
        assertEquals(0, caffeine.nightsWithout)
    }

    @Test
    fun trailingWindow_isLimitedToThirtyDays() {
        val sessions = (0L until 40L).map { session(it, 7.0) }
        val logs = (0L until 40L).map { log(today.minusDays(it * 2), FactorTag.EXERCISE) }
        val results = CorrelationCalculator.compute(logs, sessions, today)
        val exercise = results.first { it.tag == FactorTag.EXERCISE }
        // Nights outside the trailing 30 days must not contribute
        assertTrue(exercise.nightsWith + exercise.nightsWithout <= 30)
    }

    @Test
    fun results_existForEveryTag() {
        val results = CorrelationCalculator.compute(emptyList(), emptyList(), today)
        assertEquals(FactorTag.entries.size, results.size)
    }
}
