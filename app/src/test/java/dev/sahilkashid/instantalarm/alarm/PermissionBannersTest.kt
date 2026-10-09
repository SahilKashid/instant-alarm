package dev.sahilkashid.instantalarm.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PermissionBannersTest {
    @Test
    fun fullScreenBannerClearsWhenAccessIsGranted() {
        val denied = banners(canUseFullScreenIntent = false)
        val granted = banners(canUseFullScreenIntent = true)

        assertEquals(PermissionBanners.FULL_SCREEN, denied.fullScreen)
        assertNull(granted.fullScreen)
    }

    @Test
    fun snoozeHintFollowsTheLatestGrant() {
        assertEquals(
            PermissionBanners.NOTIFICATIONS,
            banners(notificationsGranted = false).snoozeHint,
        )
        assertEquals(
            PermissionBanners.EXACT_ALARM,
            banners(notificationsGranted = true, exactAlarmsAllowed = false).snoozeHint,
        )
        assertNull(
            banners(notificationsGranted = true, exactAlarmsAllowed = true).snoozeHint,
        )
    }

    @Test
    fun olderReleasesDoNotAskForNotificationOrFullScreenAccess() {
        val state = PermissionBanners.resolve(
            sdkInt = 32,
            notificationsGranted = false,
            exactAlarmsAllowed = true,
            canUseFullScreenIntent = false,
        )
        assertNull(state.snoozeHint)
        assertNull(state.fullScreen)
    }

    private fun banners(
        notificationsGranted: Boolean = true,
        exactAlarmsAllowed: Boolean = true,
        canUseFullScreenIntent: Boolean = true,
    ) = PermissionBanners.resolve(
        sdkInt = 34,
        notificationsGranted = notificationsGranted,
        exactAlarmsAllowed = exactAlarmsAllowed,
        canUseFullScreenIntent = canUseFullScreenIntent,
    )
}
