package dev.sahilkashid.instantalarm.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale

class ClockTextTest {
    private val us = Locale.US

    @Test
    fun twelveHourClockOmitsMeridiemAndLeadingZero() {
        assertEquals("5:00", ClockText.formatTime(LocalTime.of(5, 0), is24Hour = false, us))
        assertEquals("12:53", ClockText.formatTime(LocalTime.of(12, 53), is24Hour = false, us))
        assertEquals("12:05", ClockText.formatTime(LocalTime.of(0, 5), is24Hour = false, us))
        assertEquals("12:00", ClockText.formatTime(LocalTime.of(0, 0), is24Hour = false, us))
        assertEquals("12:00", ClockText.formatTime(LocalTime.of(12, 0), is24Hour = false, us))
        assertEquals("3:07", ClockText.formatTime(LocalTime.of(15, 7, 59), is24Hour = false, us))
    }

    @Test
    fun twentyFourHourClockUsesLeadingZero() {
        assertEquals("05:00", ClockText.formatTime(LocalTime.of(5, 0), is24Hour = true, us))
        assertEquals("00:00", ClockText.formatTime(LocalTime.of(0, 0), is24Hour = true, us))
        assertEquals("15:07", ClockText.formatTime(LocalTime.of(15, 7), is24Hour = true, us))
        assertEquals("23:59", ClockText.formatTime(LocalTime.of(23, 59, 30), is24Hour = true, us))
    }

    @Test
    fun dateMatchesAlarmScreen() {
        assertEquals("Fri, October 9", ClockText.formatDate(LocalDate.of(2026, 10, 9), us))
        assertEquals("Thu, January 1", ClockText.formatDate(LocalDate.of(2026, 1, 1), us))
    }

    @Test
    fun snoozedUntilUsesTheSameClockFormat() {
        assertEquals(
            "Snoozed until 5:05",
            ClockText.formatSnoozedUntil(LocalTime.of(5, 5), is24Hour = false, us),
        )
        assertEquals(
            "Snoozed until 17:05",
            ClockText.formatSnoozedUntil(LocalTime.of(17, 5), is24Hour = true, us),
        )

        val epoch = Instant.parse("2026-10-09T17:05:00Z").toEpochMilli()
        assertEquals(
            "Snoozed until 5:05",
            ClockText.formatSnoozedUntil(epoch, is24Hour = false, ZoneOffset.UTC, us),
        )
        assertEquals(
            "Snoozed until 17:05",
            ClockText.formatSnoozedUntil(epoch, is24Hour = true, ZoneOffset.UTC, us),
        )
    }
}
