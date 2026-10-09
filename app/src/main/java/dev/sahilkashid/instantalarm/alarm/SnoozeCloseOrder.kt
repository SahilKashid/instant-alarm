package dev.sahilkashid.instantalarm.alarm

/**
 * The alarm screen has to finish before snooze scheduling starts.
 * [finishAndRemoveTask] only queues the teardown, so any AlarmManager or
 * notification work still on this call stack holds the screen up. [handoff]
 * takes that work off the stack.
 */
object SnoozeCloseOrder {
    fun closeThenSchedule(
        closeScreen: () -> Unit,
        schedule: () -> Unit,
        handoff: (() -> Unit) -> Unit,
    ) {
        closeScreen()
        handoff(schedule)
    }
}
