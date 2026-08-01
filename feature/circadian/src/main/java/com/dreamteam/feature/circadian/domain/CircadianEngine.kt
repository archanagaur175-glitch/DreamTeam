package com.dreamteam.feature.circadian.domain

import com.dreamteam.core.common.clamp
import com.dreamteam.core.common.toMinutesSinceMidnight
import java.time.LocalTime

/** One sampled point on today's energy curve. */
data class EnergyPoint(val time: LocalTime, val energy: Double)

/** Today's full circadian energy timeline (derived, never stored). */
data class EnergyCurve(
    val wakeTime: LocalTime,
    val points: List<EnergyPoint>,
    val peakTime: LocalTime,
    val peakEnergy: Double,
    val slumpTime: LocalTime,
    val slumpEnergy: Double,
    val windDownStart: LocalTime,
    val idealBedtime: LocalTime,
    /** The debt level that modulated this curve's amplitude. */
    val debtHours: Double,
)

/**
 * Circadian Energy Engine — an ORIGINAL heuristic model of the daily energy rhythm,
 * inspired by the *shape* of the two-process model of sleep regulation (circadian
 * Process C + homeostatic sleep pressure Process S) and SAFTE-style fatigue curves.
 *
 * These are our own tunable constants, NOT a reproduction of any proprietary
 * coefficient set. The curve is anchored to wake time (t=0 at wake) and its
 * amplitude is compressed by current sleep debt (higher debt → lower peaks,
 * deeper/earlier slump).
 */
object CircadianConstants {

    data class Anchor(val hoursAfterWake: Double, val energy: Double)

    /** Base curve anchors, relative to wake time (energy 0..100). */
    val ANCHORS: List<Anchor> = listOf(
        Anchor(0.0, 32.0),   // wake — post-sleep-inertia, low but rising
        Anchor(1.5, 62.0),   // morning ramp
        Anchor(3.5, 92.0),   // late-morning peak
        Anchor(6.5, 52.0),   // early/mid-afternoon slump trough
        Anchor(9.5, 72.0),   // secondary peak
        Anchor(13.0, 42.0),  // wind-down begins
        Anchor(16.0, 14.0),  // ideal bedtime — sleep pressure dominant
        Anchor(19.0, 8.0),   // extended tail (if awake very late)
    )

    /** Amplitude floor; peaks compress toward this as debt grows. */
    const val ENERGY_FLOOR = 12.0

    /** scale = 1 / (1 + DEBT_SCALE_FACTOR * debtHours). */
    const val DEBT_SCALE_FACTOR = 0.045

    /** Slump trough shifts earlier by this many minutes per debt hour. */
    const val SLUMP_SHIFT_MIN_PER_DEBT = 3.6

    /** Slump shift is capped at this many minutes. */
    const val SLUMP_SHIFT_MAX_MIN = 45.0

    /** Slump deepens by this many points per debt hour. */
    const val SLUMP_DEPTH_PER_DEBT = 2.0

    /** Slump depth is capped at this many points. */
    const val SLUMP_DEPTH_MAX = 18.0

    const val SAMPLE_MINUTES = 15
    const val CURVE_HOURS = 24.0
    const val IDEAL_BEDTIME_HOURS = 16.0

    /** Slump search window, hours after wake. */
    const val SLUMP_WINDOW_START_HOURS = 4.0
    const val SLUMP_WINDOW_END_HOURS = 9.0

    /** Morning window used to locate the peak, hours after wake. */
    const val PEAK_WINDOW_HOURS = 6.0
}

object CircadianEngine {

    fun computeEnergyCurve(wakeTime: LocalTime, currentDebtHours: Double): EnergyCurve {
        val scale = 1.0 / (1.0 + CircadianConstants.DEBT_SCALE_FACTOR * currentDebtHours)
        val slumpShiftMin = minOf(
            CircadianConstants.SLUMP_SHIFT_MAX_MIN,
            currentDebtHours * CircadianConstants.SLUMP_SHIFT_MIN_PER_DEBT,
        )
        val slumpDepth = minOf(
            CircadianConstants.SLUMP_DEPTH_MAX,
            currentDebtHours * CircadianConstants.SLUMP_DEPTH_PER_DEBT,
        )

        // 1) Apply debt modulation to anchors: shift + deepen the slump, then
        //    compress all amplitudes against the floor.
        val anchors = CircadianConstants.ANCHORS.mapIndexed { index, a ->
            var hours = a.hoursAfterWake
            var energy = a.energy
            if (index == 3) { // the slump anchor
                hours -= slumpShiftMin / 60.0
                energy -= slumpDepth
            }
            val compressed =
                CircadianConstants.ENERGY_FLOOR + (energy - CircadianConstants.ENERGY_FLOOR) * scale
            CircadianConstants.Anchor(hours, clamp(compressed, 0.0, 100.0))
        }

        // 2) Sample the Catmull-Rom curve through the modulated anchors.
        val sampleCount =
            (CircadianConstants.CURVE_HOURS * 60 / CircadianConstants.SAMPLE_MINUTES).toInt()
        val points = (0 until sampleCount).map { i ->
            val minutes = i * CircadianConstants.SAMPLE_MINUTES
            EnergyPoint(
                time = wakeTime.plusMinutes(minutes.toLong()),
                energy = energyAtHours(anchors, minutes / 60.0),
            )
        }

        // 3) Locate peak (morning window) and slump (afternoon window).
        val peakWindow = (CircadianConstants.PEAK_WINDOW_HOURS * 60 / CircadianConstants.SAMPLE_MINUTES).toInt()
        val peak = points.take(peakWindow).maxByOrNull { it.energy } ?: points.first()

        val slumpStart = (CircadianConstants.SLUMP_WINDOW_START_HOURS * 60 / CircadianConstants.SAMPLE_MINUTES).toInt()
        val slumpEnd = (CircadianConstants.SLUMP_WINDOW_END_HOURS * 60 / CircadianConstants.SAMPLE_MINUTES).toInt()
            .coerceAtMost(points.size)
        val slump = points.subList(slumpStart, slumpEnd).minByOrNull { it.energy } ?: points.first()

        return EnergyCurve(
            wakeTime = wakeTime,
            points = points,
            peakTime = peak.time,
            peakEnergy = peak.energy,
            slumpTime = slump.time,
            slumpEnergy = slump.energy,
            windDownStart = wakeTime.plusMinutes(
                (CircadianConstants.ANCHORS[5].hoursAfterWake * 60).toLong(),
            ),
            idealBedtime = wakeTime.plusHours(CircadianConstants.IDEAL_BEDTIME_HOURS.toLong()),
            debtHours = currentDebtHours,
        )
    }

    /** Energy at [hours] after wake via centripetal-free (uniform) Catmull-Rom. */
    private fun energyAtHours(anchors: List<CircadianConstants.Anchor>, hours: Double): Double {
        if (hours <= anchors.first().hoursAfterWake) return anchors.first().energy
        if (hours >= anchors.last().hoursAfterWake) return anchors.last().energy
        val i = anchors.indexOfLast { it.hoursAfterWake <= hours }
        val p0 = anchors.getOrNull(i - 1) ?: anchors[i]
        val p1 = anchors[i]
        val p2 = anchors[i + 1]
        val p3 = anchors.getOrNull(i + 2) ?: p2
        val span = p2.hoursAfterWake - p1.hoursAfterWake
        val t = if (span > 0) (hours - p1.hoursAfterWake) / span else 0.0
        return clamp(catmullRom(p0.energy, p1.energy, p2.energy, p3.energy, t), 0.0, 100.0)
    }

    private fun catmullRom(p0: Double, p1: Double, p2: Double, p3: Double, t: Double): Double {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5 * (
            (2 * p1) +
                (-p0 + p2) * t +
                (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 +
                (-p0 + 3 * p1 - 3 * p2 + p3) * t3
            )
    }
}
