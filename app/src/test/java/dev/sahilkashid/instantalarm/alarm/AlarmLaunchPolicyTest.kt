package dev.sahilkashid.instantalarm.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmLaunchPolicyTest {
    @Test
    fun lockedOrScreenOffOpensTheAlarmScreen() {
        assertTrue(AlarmLaunchPolicy.launchActivityDirectly(screenInteractive = false, keyguardLocked = true))
        assertTrue(AlarmLaunchPolicy.launchActivityDirectly(screenInteractive = false, keyguardLocked = false))
        assertTrue(AlarmLaunchPolicy.launchActivityDirectly(screenInteractive = true, keyguardLocked = true))
    }

    @Test
    fun unlockedAndInUseDoesNotTakeOverTheScreen() {
        assertFalse(AlarmLaunchPolicy.launchActivityDirectly(screenInteractive = true, keyguardLocked = false))
    }

    @Test
    fun shortcutAndSnoozeShareTheFullScreenRule() {
        assertFalse(
            AlarmLaunchPolicy.shouldOpenAlarmUi(
                RingingService.REASON_SHORTCUT,
                screenInteractive = true,
                keyguardLocked = false,
            ),
        )
        assertTrue(
            AlarmLaunchPolicy.shouldOpenAlarmUi(
                RingingService.REASON_SHORTCUT,
                screenInteractive = false,
                keyguardLocked = false,
            ),
        )
        assertTrue(
            AlarmLaunchPolicy.shouldOpenAlarmUi(
                RingingService.REASON_SHORTCUT,
                screenInteractive = true,
                keyguardLocked = true,
            ),
        )
        assertTrue(
            AlarmLaunchPolicy.shouldOpenAlarmUi(
                RingingService.REASON_SNOOZE,
                screenInteractive = false,
                keyguardLocked = true,
            ),
        )
        assertFalse(
            AlarmLaunchPolicy.shouldOpenAlarmUi(
                RingingService.REASON_MANUAL,
                screenInteractive = false,
                keyguardLocked = true,
            ),
        )
    }

    @Test
    fun fullScreenIntentIsAttachedOnlyWhenTheAppMaySendOne() {
        assertTrue(AlarmLaunchPolicy.attachFullScreenIntent(canUseFullScreenIntent = true))
        assertFalse(AlarmLaunchPolicy.attachFullScreenIntent(canUseFullScreenIntent = false))
    }

    @Test
    fun android14OffersAnInAppGrantWhenFullScreenAccessIsMissing() {
        assertFalse(AlarmLaunchPolicy.shouldOfferFullScreenAccess(33, canUseFullScreenIntent = false))
        assertFalse(AlarmLaunchPolicy.shouldOfferFullScreenAccess(34, canUseFullScreenIntent = true))
        assertTrue(AlarmLaunchPolicy.shouldOfferFullScreenAccess(34, canUseFullScreenIntent = false))
        assertTrue(AlarmLaunchPolicy.shouldOfferFullScreenAccess(37, canUseFullScreenIntent = false))
    }

    @Test
    fun fullScreenGrantButtonHasASettingsFallback() {
        assertEquals(
            listOf(
                "android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT",
                "android.settings.APPLICATION_DETAILS_SETTINGS",
            ),
            AlarmLaunchPolicy.fullScreenSettingsActions(),
        )
    }
}
