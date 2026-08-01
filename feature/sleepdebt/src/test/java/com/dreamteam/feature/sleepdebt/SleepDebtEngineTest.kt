package com.dreamteam.feature.sleepdebt

import com.dreamteam.core.common.SleepSession
import com.dreamteam.feature.sleepdebt.domain.SleepDebtEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SleepDebtEngineTest {

    private val today: LocalDate = LocalDate.of(2026, 7, 14)

    /** A session of [hours] ending (waking) [nightsAgo] nights before [today] at 07:00. */
    private fun session(nightsAgo: Long, hours: Double): SleepSession {
        val wake = today.minusDays(nightsAgo).atTime(7, 0)
        return SleepSession(
            sleepStart = wake.minusMinutes((hours * 60).toLong()),
            sleepEnd = wake,
        )
    }

    @Test
    fun perfectNights_produceZeroDebt() {
        val debt = SleepDebtEngine.computeRollingDebt(
            (0L until 14L).map { session(it, 8.0) }, 8.0, today,
        )
        assertEquals(0.0, debt.totalDeficitHours, 0.001)
        assertEquals(14, debt.nightsWithData)
        assertTrue(debt.isConfident)
        assertEquals(14, debt.dailyBreakdown.size)
    }

    @Test
    fun shortNights_accrueFullDeficit() {
        // 14 nights × 6h vs 8h need → 2h per night = 28h total
        val debt = SleepDebtEngine.computeRollingDebt(
            (0L until 14L).map { session(it, 6.0) }, 8.0, today,
        )
        assertEquals(28.0, debt.totalDeficitHours, 0.001)
        assertEquals(2.0, debt.avgDeficitHours, 0.001)
    }

    @Test
    fun longNight_cannotRefundDebt() {
        // 10h of sleep must contribute 0, never negative
        val debt = SleepDebtEngine.computeRollingDebt(listOf(session(0, 10.0)), 8.0, today)
        assertEquals(0.0, debt.totalDeficitHours, 0.001)
    }

    @Test
    fun missingNights_areExcludedAndFlagged() {
        val debt = SleepDebtEngine.computeRollingDebt(
            listOf(session(0, 6.0), session(1, 7.0)), 8.0, today,
        )
        assertEquals(2, debt.nightsWithData)
        assertEquals(3.0, debt.totalDeficitHours, 0.001) // 2 + 1
        assertFalse(debt.isConfident)
        assertEquals(2, debt.dailyBreakdown.count { it.actualHours != null })
        // Missing nights must not silently count as 0 debt
        assertEquals(12, debt.dailyBreakdown.count { it.actualHours == null })
    }

    @Test
    fun oldestNight_fallsOffTheWindow() {
        // 15 nights of 6h → only the most recent 14 count → 28h
        val debt = SleepDebtEngine.computeRollingDebt(
            (0L until 15L).map { session(it, 6.0) }, 8.0, today,
        )
        assertEquals(28.0, debt.totalDeficitHours, 0.001)
    }

    @Test
    fun trend_comparesAgainstPreviousWindow() {
        // Current window 7h nights (deficit 1h × 14 = 14h); previous window 6h nights (2h × 14 = 28h)
        val current = (0L until 14L).map { session(it, 7.0) }
        val previous = (14L until 28L).map { session(it, 6.0) }
        val debt = SleepDebtEngine.computeRollingDebt(current + previous, 8.0, today)
        assertEquals(14.0, debt.totalDeficitHours, 0.001)
        assertEquals(28.0, debt.previousWindowTotalHours, 0.001)
        assertEquals(-14.0, debt.trendHours, 0.001) // paying down
    }

    @Test
    fun corruptSessions_areIgnored() {
        val negative = SleepSession(
            sleepStart = today.atTime(7, 0),
            sleepEnd = today.atTime(6, 0),
        )
        val tooLong = SleepSession(
            sleepStart = today.atTime(7, 0).minusHours(30),
            sleepEnd = today.atTime(7, 0),
        )
        val good = session(0, 7.0)
        val debt = SleepDebtEngine.computeRollingDebt(listOf(negative, tooLong, good), 8.0, today)
        assertEquals(1, debt.nightsWithData)
        assertEquals(1.0, debt.totalDeficitHours, 0.001)
    }

    @Test
    fun baselineChange_altersDebt() {
        val nights = (0L until 14L).map { session(it, 7.0) }
        val debt8 = SleepDebtEngine.computeRollingDebt(nights, 8.0, today)
        val debt9 = SleepDebtEngine.computeRollingDebt(nights, 9.0, today)
        assertEquals(14.0, debt8.totalDeficitHours, 0.001)
        assertEquals(28.0, debt9.totalDeficitHours, 0.001)
    }

    @Test
    fun noData_debtIsZeroButNotConfident() {
        val debt = SleepDebtEngine.computeRollingDebt(emptyList(), 8.0, today)
        assertEquals(0.0, debt.totalDeficitHours, 0.001)
        assertFalse(debt.isConfident)
        assertNull(debt.previousWindowTotalHours)
        assertNull(debt.trendHours)
    }
}
