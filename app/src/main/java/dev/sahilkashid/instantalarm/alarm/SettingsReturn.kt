package dev.sahilkashid.instantalarm.alarm

/**
 * Coming back from a settings screen this app opened is not a new visit.
 * The alarm must keep doing what it was already doing.
 */
object SettingsReturn {
    fun shouldStartRinging(returningFromOurSettings: Boolean): Boolean = !returningFromOurSettings
}
