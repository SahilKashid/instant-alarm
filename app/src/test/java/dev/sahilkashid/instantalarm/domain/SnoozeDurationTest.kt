package dev.sahilkashid.instantalarm.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SnoozeDurationTest {
    @Test
    fun boundsAndDefaultMatchTheAlarmPill() {
        assertEquals(5, SnoozeDuration.MIN_MINUTES)
        assertEquals(30, SnoozeDuration.MAX_MINUTES)
        assertEquals(5, SnoozeDuration.DEFAULT_MINUTES)
        assertEquals(5, SnoozeDuration.STEP_MINUTES)
    }

    @Test
    fun coerceClampsToInclusiveRange() {
        assertEquals(5, SnoozeDuration.coerce(0))
        assertEquals(5, SnoozeDuration.coerce(-4))
        assertEquals(5, SnoozeDuration.coerce(1))
        assertEquals(5, SnoozeDuration.coerce(5))
        assertEquals(15, SnoozeDuration.coerce(15))
        assertEquals(30, SnoozeDuration.coerce(30))
        assertEquals(30, SnoozeDuration.coerce(31))
        assertEquals(30, SnoozeDuration.coerce(120))
    }

    @Test
    fun coerceSnapsOffStepValuesOntoFiveMinuteGrid() {
        assertEquals(5, SnoozeDuration.coerce(6))
        assertEquals(5, SnoozeDuration.coerce(7))
        assertEquals(10, SnoozeDuration.coerce(8))
        assertEquals(10, SnoozeDuration.coerce(12))
        assertEquals(15, SnoozeDuration.coerce(13))
        assertEquals(25, SnoozeDuration.coerce(27))
        assertEquals(30, SnoozeDuration.coerce(28))
    }

    @Test
    fun incrementStepsByFiveAndStopsAtThirty() {
        assertEquals(10, SnoozeDuration.increment(5))
        assertEquals(15, SnoozeDuration.increment(10))
        assertEquals(30, SnoozeDuration.increment(25))
        assertEquals(30, SnoozeDuration.increment(30))
        assertEquals(30, SnoozeDuration.increment(80))
        assertEquals(10, SnoozeDuration.increment(0))
    }

    @Test
    fun decrementStepsByFiveAndStopsAtFive() {
        assertEquals(5, SnoozeDuration.decrement(10))
        assertEquals(20, SnoozeDuration.decrement(25))
        assertEquals(5, SnoozeDuration.decrement(5))
        assertEquals(5, SnoozeDuration.decrement(1))
        assertEquals(5, SnoozeDuration.decrement(0))
        assertEquals(25, SnoozeDuration.decrement(40))
    }

    @Test
    fun plusAndMinusWalkTheFiveMinuteLadder() {
        var minutes = SnoozeDuration.DEFAULT_MINUTES
        for (expected in listOf(10, 15, 20, 25, 30, 30)) {
            minutes = SnoozeDuration.increment(minutes)
            assertEquals(expected, minutes)
        }
        for (expected in listOf(25, 20, 15, 10, 5, 5)) {
            minutes = SnoozeDuration.decrement(minutes)
            assertEquals(expected, minutes)
        }
    }

    @Test
    fun labelKeepsSnoozeMinsFormat() {
        assertEquals("Snooze 5 mins", SnoozeDuration.label(5))
        assertEquals("Snooze 10 mins", SnoozeDuration.label(10))
        assertEquals("Snooze 30 mins", SnoozeDuration.label(30))
        assertEquals("Snooze 5 mins", SnoozeDuration.label(0))
        assertEquals("Snooze 5 mins", SnoozeDuration.label(1))
        assertEquals("Snooze 30 mins", SnoozeDuration.label(99))
    }

    @Test
    fun triggerAtAddsClampedMinutesInMillis() {
        val now = 1_700_000_000_000L
        assertEquals(now + 5 * 60_000L, SnoozeDuration.triggerAtMillis(now, 5))
        assertEquals(now + 5 * 60_000L, SnoozeDuration.triggerAtMillis(now, 0))
        assertEquals(now + 10 * 60_000L, SnoozeDuration.triggerAtMillis(now, 10))
        assertEquals(now + 30 * 60_000L, SnoozeDuration.triggerAtMillis(now, 45))
    }
}
