package dev.sahilkashid.instantalarm.alarm

/**
 * How a firing snooze and the launcher shortcut present themselves, matching
 * a stock alarm clock.
 *
 * The system full-screen intent on a high-importance alarm notification is what
 * opens the alarm screen: locked or screen-off launches that screen and turns
 * the display on; an unlocked phone that is in use gets a heads-up notification
 * instead. This process must not start the activity itself in that second case.
 */
object AlarmLaunchPolicy {
    const val FULL_SCREEN_SETTINGS_ACTION = "android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT"
    const val APP_DETAILS_SETTINGS_ACTION = "android.settings.APPLICATION_DETAILS_SETTINGS"

    /**
     * True when a direct [android.content.Context.startActivity] is appropriate.
     * False while the screen is on and the keyguard is not showing.
     */
    fun launchActivityDirectly(screenInteractive: Boolean, keyguardLocked: Boolean): Boolean {
        return !screenInteractive || keyguardLocked
    }

    /** The platform launches the full-screen intent only when this app may send one. */
    fun attachFullScreenIntent(canUseFullScreenIntent: Boolean): Boolean = canUseFullScreenIntent

    /**
     * A firing snooze and the launcher shortcut share this rule. Manual opens
     * ([RingingService.REASON_MANUAL]) always come from an activity that is
     * already on screen, so they do not launch another one.
     */
    fun shouldOpenAlarmUi(
        reason: String?,
        screenInteractive: Boolean,
        keyguardLocked: Boolean,
    ): Boolean {
        if (reason != RingingService.REASON_SNOOZE && reason != RingingService.REASON_SHORTCUT) {
            return false
        }
        return launchActivityDirectly(screenInteractive, keyguardLocked)
    }

    /** Android 14+ can revoke full-screen intents; offer an in-app grant when it has. */
    fun shouldOfferFullScreenAccess(sdkInt: Int, canUseFullScreenIntent: Boolean): Boolean {
        return sdkInt >= 34 && !canUseFullScreenIntent
    }

    /**
     * Settings screens to try, in order. The second is the fallback so the
     * button still opens something when the full-screen page is missing.
     */
    fun fullScreenSettingsActions(): List<String> = listOf(
        FULL_SCREEN_SETTINGS_ACTION,
        APP_DETAILS_SETTINGS_ACTION,
    )
}
