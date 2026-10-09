package dev.sahilkashid.instantalarm.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SnoozeCloseOrderTest {
    @Test
    fun screenClosesBeforeSchedulingStarts() {
        val events = mutableListOf<String>()
        var handedOff: (() -> Unit)? = null

        SnoozeCloseOrder.closeThenSchedule(
            closeScreen = { events += "close" },
            schedule = { events += "schedule" },
            handoff = { work -> handedOff = work },
        )

        assertEquals(listOf("close"), events)
        assertNotNull(handedOff)
        handedOff?.invoke()
        assertEquals(listOf("close", "schedule"), events)
    }
}
