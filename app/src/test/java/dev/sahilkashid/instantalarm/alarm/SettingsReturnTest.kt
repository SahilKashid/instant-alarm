package dev.sahilkashid.instantalarm.alarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsReturnTest {
    @Test
    fun returningFromSettingsDoesNotStartRinging() {
        assertFalse(SettingsReturn.shouldStartRinging(returningFromOurSettings = true))
    }

    @Test
    fun aNormalForegroundVisitStillRings() {
        assertTrue(SettingsReturn.shouldStartRinging(returningFromOurSettings = false))
    }
}
