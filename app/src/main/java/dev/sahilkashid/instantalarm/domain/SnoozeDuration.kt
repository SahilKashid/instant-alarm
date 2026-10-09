package dev.sahilkashid.instantalarm.domain

/**
 * Snooze length shown on the alarm pill. One-minute steps, clamped to 1–30.
 */
object SnoozeDuration {
    const val MIN_MINUTES = 1
    const val MAX_MINUTES = 30
    const val DEFAULT_MINUTES = 5
    const val STEP_MINUTES = 1

    fun coerce(minutes: Int): Int = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)

    fun increment(minutes: Int): Int = coerce(coerce(minutes) + STEP_MINUTES)

    fun decrement(minutes: Int): Int = coerce(coerce(minutes) - STEP_MINUTES)

    fun triggerAtMillis(nowMillis: Long, minutes: Int): Long {
        return nowMillis + coerce(minutes) * 60_000L
    }

    fun label(minutes: Int): String {
        val value = coerce(minutes)
        val unit = if (value == 1) "min" else "mins"
        return "Snooze $value $unit"
    }
}
