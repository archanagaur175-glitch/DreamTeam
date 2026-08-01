package com.dreamteam.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MathUtilsTest {

    @Test
    fun clamp_keepsValuesInsideRange() {
        assertEquals(0.0, clamp(-5.0, 0.0, 10.0), 0.0001)
        assertEquals(5.0, clamp(5.0, 0.0, 10.0), 0.0001)
        assertEquals(10.0, clamp(42.0, 0.0, 10.0), 0.0001)
    }

    @Test
    fun lerp_interpolatesLinearly() {
        assertEquals(5.0, lerp(0.0, 10.0, 0.5), 0.0001)
        assertEquals(0.0, lerp(0.0, 10.0, 0.0), 0.0001)
        assertEquals(10.0, lerp(0.0, 10.0, 1.0), 0.0001)
    }

    @Test
    fun stdDev_zeroForSmallSamples() {
        assertEquals(0.0, stdDev(listOf(1.0)), 0.0001)
        assertEquals(0.0, stdDev(emptyList()), 0.0001)
    }

    @Test
    fun stdDev_computesSampleStandardDeviation() {
        // Population of {1,2,3,4}: mean 2.5, sample std-dev = sqrt(5/3) ~ 1.29099
        assertEquals(1.2909944, stdDev(listOf(1.0, 2.0, 3.0, 4.0)), 0.0001)
    }
}
