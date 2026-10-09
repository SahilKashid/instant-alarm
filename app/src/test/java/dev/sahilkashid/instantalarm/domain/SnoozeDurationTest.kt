package dev.sahilkashid.instantalarm.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SnoozeDurationTest {
    @Test
    fun boundsAndDefaultMatchTheAlarmPill() {
        assertEquals(1, SnoozeDuration.MIN_MINUTES)
        assertEquals(30, SnoozeDuration.MAX_MINUTES)
        assertEquals(5, SnoozeDuration.DEFAULT_MINUTES)
        assertEquals(1, SnoozeDuration.STEP_MINUTES)
    }

    @Test
    fun coerceClampsToInclusiveRange() {
        assertEquals(1, SnoozeDuration.coerce(0))
        assertEquals(1, SnoozeDuration.coerce(-4))
        assertEquals(1, SnoozeDuration.coerce(1))
        assertEquals(5, SnoozeDuration.coerce(5))
        assertEquals(30, SnoozeDuration.coerce(30))
        assertEquals(30, SnoozeDuration.coerce(31))
        assertEquals(30, SnoozeDuration.coerce(120))
    }

    @Test
    fun incrementStepsByOneAndStopsAtThirty() {
        assertEquals(6, SnoozeDuration.increment(5))
        assertEquals(30, SnoozeDuration.increment(29))
        assertEquals(30, SnoozeDuration.increment(30))
        assertEquals(30, SnoozeDuration.increment(80))
        assertEquals(2, SnoozeDuration.increment(0))
    }

    @Test
    fun decrementStepsByOneAndStopsAtOne() {
        assertEquals(4, SnoozeDuration.decrement(5))
        assertEquals(1, SnoozeDuration.decrement(2))
        assertEquals(1, SnoozeDuration.decrement(1))
        assertEquals(1, SnoozeDuration.decrement(0))
        assertEquals(29, SnoozeDuration.decrement(40))
    }

    @Test
    fun labelUsesSingularMinuteOnlyForOne() {
        assertEquals("Snooze 1 min", SnoozeDuration.label(1))
        assertEquals("Snooze 5 mins", SnoozeDuration.label(5))
        assertEquals("Snooze 30 mins", SnoozeDuration.label(30))
        assertEquals("Snooze 1 min", SnoozeDuration.label(0))
        assertEquals("Snooze 30 mins", SnoozeDuration.label(99))
    }

    @Test
    fun triggerAtAddsClampedMinutesInMillis() {
        val now = 1_700_000_000_000L
        assertEquals(now + 5 * 60_000L, SnoozeDuration.triggerAtMillis(now, 5))
        assertEquals(now + 60_000L, SnoozeDuration.triggerAtMillis(now, 0))
        assertEquals(now + 30 * 60_000L, SnoozeDuration.triggerAtMillis(now, 45))
    }
}
