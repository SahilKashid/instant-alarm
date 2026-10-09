package dev.sahilkashid.instantalarm.alarm

/**
 * Which permission lines the alarm screen should show for a fresh read of
 * the current grants. Callers re-read this when the activity resumes or a
 * permission broadcast arrives, so a grant made in system settings shows up
 * without reopening the app.
 */
object PermissionBanners {
    const val NOTIFICATIONS = "notifications"
    const val EXACT_ALARM = "exact"
    const val FULL_SCREEN = "full_screen"

    data class State(
        val snoozeHint: String?,
        val fullScreen: String?,
    )

    fun resolve(
        sdkInt: Int,
        notificationsGranted: Boolean,
        exactAlarmsAllowed: Boolean,
        canUseFullScreenIntent: Boolean,
    ): State {
        val snoozeHint = when {
            sdkInt >= 33 && !notificationsGranted -> NOTIFICATIONS
            !exactAlarmsAllowed -> EXACT_ALARM
            else -> null
        }
        val fullScreen = if (
            AlarmLaunchPolicy.shouldOfferFullScreenAccess(sdkInt, canUseFullScreenIntent)
        ) {
            FULL_SCREEN
        } else {
            null
        }
        return State(snoozeHint, fullScreen)
    }
}
