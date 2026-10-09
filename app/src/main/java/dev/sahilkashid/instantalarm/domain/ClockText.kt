package dev.sahilkashid.instantalarm.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Clock and date lines for the alarm screen.
 * 12-hour times omit the AM/PM marker, matching the alarm layout (`5:00`, `12:53`).
 */
object ClockText {
    fun formatTime(time: LocalTime, is24Hour: Boolean, locale: Locale = Locale.getDefault()): String {
        val pattern = if (is24Hour) "HH:mm" else "h:mm"
        return time.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun formatDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        return date.format(DateTimeFormatter.ofPattern("EEE, MMMM d", locale))
    }

    fun formatSnoozedUntil(
        time: LocalTime,
        is24Hour: Boolean,
        locale: Locale = Locale.getDefault(),
    ): String {
        return "Snoozed until ${formatTime(time, is24Hour, locale)}"
    }

    fun formatSnoozedUntil(
        epochMillis: Long,
        is24Hour: Boolean,
        zone: ZoneId,
        locale: Locale = Locale.getDefault(),
    ): String {
        val time = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalTime()
        return formatSnoozedUntil(time, is24Hour, locale)
    }
}
