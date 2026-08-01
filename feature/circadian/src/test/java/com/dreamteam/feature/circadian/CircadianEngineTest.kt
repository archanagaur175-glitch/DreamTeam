package com.dreamteam.feature.circadian

import com.dreamteam.core.common.toMinutesSinceMidnight
import com.dreamteam.feature.circadian.domain.CircadianEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class CircadianEngineTest {

    private val wake = LocalTime.of(7, 0)

    @Test
    fun curve_anchorsToWakeTime() {
        val curve = CircadianEngine.computeEnergyCurve(wake, 0.0)
        assertEquals(wake, curve.points.first().time)
        assertEquals(96, curve.points.size) // 24h × 4 samples/hour
    }

    @Test
    fun peak_isLateMorning_slump_isAfternoon() {
        val curve = CircadianEngine.computeEnergyCurve(wake, 0.0)
        assertTrue("peak hour ${curve.peakTime}", curve.peakTime.hour in 8..12)
        assertTrue("slump hour ${curve.slumpTime}", curve.slumpTime.hour in 12..17)
        assertTrue(curve.peakEnergy > curve.slumpEnergy)
    }

    @Test
    fun energies_stayWithinBoundsEvenWithDebt() {
        val curve = CircadianEngine.computeEnergyCurve(wake, 14.0)
        assertTrue(curve.points.all { it.energy in 0.0..100.0 })
    }

    @Test
    fun debt_compressesPeaksAndSlump() {
        val debtFree = CircadianEngine.computeEnergyCurve(wake, 0.0)
        val indebted = CircadianEngine.computeEnergyCurve(wake, 10.0)
        assertTrue(indebted.peakEnergy < debtFree.peakEnergy)
        assertTrue(indebted.slumpEnergy < debtFree.slumpEnergy)
    }

    @Test
    fun debt_shiftsSlumpEarlier() {
        val debtFree = CircadianEngine.computeEnergyCurve(wake, 0.0)
        val indebted = CircadianEngine.computeEnergyCurve(wake, 10.0)
        assertTrue(indebted.slumpTime.toMinutesSinceMidnight() <= debtFree.slumpTime.toMinutesSinceMidnight())
    }

    @Test
    fun idealBedtime_is16HoursAfterWake() {
        assertEquals(LocalTime.of(23, 0), CircadianEngine.computeEnergyCurve(wake, 0.0).idealBedtime)
    }

    @Test
    fun differentWakeTime_shiftsWholeCurve() {
        val curve5 = CircadianEngine.computeEnergyCurve(LocalTime.of(5, 0), 0.0)
        val curve9 = CircadianEngine.computeEnergyCurve(LocalTime.of(9, 0), 0.0)
        assertEquals(LocalTime.of(5, 0), curve5.points.first().time)
        assertEquals(LocalTime.of(9, 0), curve9.points.first().time)
        assertEquals(LocalTime.of(21, 0), curve5.idealBedtime)
        assertEquals(LocalTime.of(1, 0), curve9.idealBedtime)
    }

    @Test
    fun zeroDebt_curveHasHigherAverageThanHeavyDebt() {
        val free = CircadianEngine.computeEnergyCurve(wake, 0.0)
        val heavy = CircadianEngine.computeEnergyCurve(wake, 12.0)
        val avgFree = free.points.map { it.energy }.average()
        val avgHeavy = heavy.points.map { it.energy }.average()
        assertTrue(avgFree > avgHeavy)
    }
}
