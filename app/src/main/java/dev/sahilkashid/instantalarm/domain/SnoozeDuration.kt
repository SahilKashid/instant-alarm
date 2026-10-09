package dev.sahilkashid.instantalarm.domain

/**
 * Snooze length shown on the alarm pill. Clamped to 1–30.
 * Below 5 minutes the pill steps by 1. At 5 and above it steps by 5.
 * Values above 5 that are off that grid snap to the nearest 5 (7 becomes 5,
 * 8 becomes 10). 1 through 4 are already valid steps.
 */
object SnoozeDuration {
    const val MIN_MINUTES = 1
    const val MAX_MINUTES = 30
    const val DEFAULT_MINUTES = 5
    const val FINE_UNTIL_MINUTES = 5
    const val FINE_STEP_MINUTES = 1
    const val COARSE_STEP_MINUTES = 5

    fun coerce(minutes: Int): Int {
        val clamped = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        if (clamped <= FINE_UNTIL_MINUTES) return clamped
        val steps = (clamped + COARSE_STEP_MINUTES / 2) / COARSE_STEP_MINUTES
        return (steps * COARSE_STEP_MINUTES).coerceIn(FINE_UNTIL_MINUTES, MAX_MINUTES)
    }

    fun increment(minutes: Int): Int {
        val current = coerce(minutes)
        if (current >= MAX_MINUTES) return MAX_MINUTES
        val step = if (current < FINE_UNTIL_MINUTES) FINE_STEP_MINUTES else COARSE_STEP_MINUTES
        return coerce(current + step)
    }

    fun decrement(minutes: Int): Int {
        val current = coerce(minutes)
        if (current <= MIN_MINUTES) return MIN_MINUTES
        val step = if (current <= FINE_UNTIL_MINUTES) FINE_STEP_MINUTES else COARSE_STEP_MINUTES
        return coerce(current - step)
    }

    fun triggerAtMillis(nowMillis: Long, minutes: Int): Long {
        return nowMillis + coerce(minutes) * 60_000L
    }

    fun label(minutes: Int): String {
        val value = coerce(minutes)
        val unit = if (value == 1) "min" else "mins"
        return "Snooze $value $unit"
    }
}
