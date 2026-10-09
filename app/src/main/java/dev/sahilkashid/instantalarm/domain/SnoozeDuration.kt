package dev.sahilkashid.instantalarm.domain

/**
 * Snooze length shown on the alarm pill. Five-minute steps, clamped to 5–30.
 * Off-step values, including a duration saved by the previous one-minute build,
 * snap to the nearest step (7 becomes 5, 8 becomes 10).
 */
object SnoozeDuration {
    const val MIN_MINUTES = 5
    const val MAX_MINUTES = 30
    const val DEFAULT_MINUTES = 5
    const val STEP_MINUTES = 5

    fun coerce(minutes: Int): Int {
        val clamped = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        val steps = (clamped - MIN_MINUTES + STEP_MINUTES / 2) / STEP_MINUTES
        return (MIN_MINUTES + steps * STEP_MINUTES).coerceIn(MIN_MINUTES, MAX_MINUTES)
    }

    fun increment(minutes: Int): Int = coerce(coerce(minutes) + STEP_MINUTES)

    fun decrement(minutes: Int): Int = coerce(coerce(minutes) - STEP_MINUTES)

    fun triggerAtMillis(nowMillis: Long, minutes: Int): Long {
        return nowMillis + coerce(minutes) * 60_000L
    }

    fun label(minutes: Int): String = "Snooze ${coerce(minutes)} mins"
}
