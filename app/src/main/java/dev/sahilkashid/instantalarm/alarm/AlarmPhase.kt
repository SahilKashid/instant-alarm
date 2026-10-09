package dev.sahilkashid.instantalarm.alarm

sealed interface AlarmPhase {
    data object Ringing : AlarmPhase

    data class Snoozed(
        val untilEpochMillis: Long,
        val exact: Boolean,
    ) : AlarmPhase
}
