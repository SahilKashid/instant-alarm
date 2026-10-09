package dev.sahilkashid.instantalarm.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnoozePresentationTest {
    @Test
    fun snoozeClosesTheAlarmScreenAndPostsTheShadeNotice() {
        assertTrue(SnoozePresentation.closesAlarmScreen())
        assertTrue(SnoozePresentation.postsShadeNotice())
        assertFalse(SnoozePresentation.showsInAppSnoozedLabel())
    }

    @Test
    fun permissionHintsStayOnTheRingingScreen() {
        assertTrue(SnoozePresentation.showPermissionHintWhileRinging())
    }

    @Test
    fun shadeDismissCancelsThePendingSnooze() {
        assertEquals(AlarmReceiver.ACTION_DISMISS, SnoozePresentation.shadeDismissAction())
    }
}