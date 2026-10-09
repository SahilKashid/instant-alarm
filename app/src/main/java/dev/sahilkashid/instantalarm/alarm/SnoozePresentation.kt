package dev.sahilkashid.instantalarm.alarm

/**
 * Snooze stops the alarm and leaves. The shade notification is the snoozed
 * state. The alarm screen does not stay up, on the lock screen or in the app.
 */
object SnoozePresentation {
    fun closesAlarmScreen(): Boolean = true

    fun postsShadeNotice(): Boolean = true

    /** There is no in-app "Snoozed until" line. That text is the shade notice. */
    fun showsInAppSnoozedLabel(): Boolean = false

    /**
     * Notification and exact-alarm hints used to sit on the snoozed screen.
     * The ringing screen is the one that stays up, so the hints live there.
     */
    fun showPermissionHintWhileRinging(): Boolean = true

    /** Dismiss on the shade notice cancels the pending snooze. */
    fun shadeDismissAction(): String = AlarmReceiver.ACTION_DISMISS
}
