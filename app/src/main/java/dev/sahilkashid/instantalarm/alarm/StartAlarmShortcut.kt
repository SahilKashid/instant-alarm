package dev.sahilkashid.instantalarm.alarm

/**
 * Static launcher shortcut declared in `res/xml/shortcuts.xml`.
 * Long-pressing the app icon offers "Start alarm", which opens the app and
 * rings immediately. It is a new manual trigger, so it also wins over a snooze
 * that is still showing.
 */
object StartAlarmShortcut {
    const val ID = "start_alarm"
    const val ACTION = "dev.sahilkashid.instantalarm.action.START_ALARM"

    fun matches(action: String?): Boolean = action == ACTION
}
