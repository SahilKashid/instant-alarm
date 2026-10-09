package dev.sahilkashid.instantalarm.alarm

/**
 * Static launcher shortcut declared in `res/xml/shortcuts.xml`.
 * Long-pressing the app icon offers "Start alarm". The shortcut starts ringing
 * and cancels a pending snooze. It does not open the alarm screen while the
 * phone is unlocked and in use; that case is a heads-up notification. A locked
 * phone still gets the full-screen alarm.
 */
object StartAlarmShortcut {
    const val ID = "start_alarm"
    const val ACTION = "dev.sahilkashid.instantalarm.action.START_ALARM"

    fun matches(action: String?): Boolean = action == ACTION
}
