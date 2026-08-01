package com.dreamteam.core.common

import kotlin.math.sqrt

fun clamp(value: Double, min: Double, max: Double): Double = value.coerceIn(min, max)

fun clamp(value: Float, min: Float, max: Float): Float = value.coerceIn(min, max)

fun lerp(start: Double, end: Double, t: Double): Double = start + (end - start) * t

/**
 * Sample standard deviation. Returns 0.0 for fewer than 2 samples.
 * Used by the movement-score detector in the smart alarm.
 */
fun stdDev(values: List<Double>): Double {
    if (values.size < 2) return 0.0
    val mean = values.average()
    val variance = values.sumOf { (it - mean) * (it - mean) } / (values.size - 1)
    return sqrt(variance)
}
