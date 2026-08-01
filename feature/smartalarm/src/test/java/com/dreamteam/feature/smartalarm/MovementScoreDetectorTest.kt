package com.dreamteam.feature.smartalarm

import com.dreamteam.feature.smartalarm.domain.MovementScoreDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovementScoreDetectorTest {

    @Test
    fun quietScores_neverTrigger() {
        val detector = MovementScoreDetector()
        assertFalse(detector.evaluate(0.05))
        assertFalse(detector.evaluate(0.10))
        assertFalse(detector.evaluate(0.20))
    }

    @Test
    fun sustainedHighScores_triggerAfterConsecutiveWindows() {
        val detector = MovementScoreDetector()
        assertFalse(detector.evaluate(0.5)) // first window — not enough yet
        assertTrue(detector.evaluate(0.6))  // second consecutive window
    }

    @Test
    fun singleSpike_doesNotTrigger() {
        val detector = MovementScoreDetector()
        assertFalse(detector.evaluate(0.9))
        assertFalse(detector.evaluate(0.1)) // streak reset
    }

    @Test
    fun customThresholdAndConsecutiveCount() {
        val detector = MovementScoreDetector(lightSleepThreshold = 0.8, requiredConsecutiveWindows = 3)
        assertFalse(detector.evaluate(0.85))
        assertFalse(detector.evaluate(0.85))
        assertTrue(detector.evaluate(0.85))
    }

    @Test
    fun reset_clearsTheStreak() {
        val detector = MovementScoreDetector()
        detector.evaluate(0.9)
        detector.reset()
        assertFalse(detector.evaluate(0.9))
    }
}
