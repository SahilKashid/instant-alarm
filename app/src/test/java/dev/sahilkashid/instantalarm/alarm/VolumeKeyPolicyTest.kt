package dev.sahilkashid.instantalarm.alarm

import android.media.AudioManager
import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeKeyPolicyTest {
    @Test
    fun firstVolumeDownWhileRingingSnoozesAndConsumesTheKey() {
        val up = VolumeKeyPolicy.forActivityKey(
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            action = KeyEvent.ACTION_DOWN,
            repeatCount = 0,
            ringing = true,
            pressAlreadyAccepted = false,
        )
        val down = VolumeKeyPolicy.forActivityKey(
            keyCode = KeyEvent.KEYCODE_VOLUME_DOWN,
            action = KeyEvent.ACTION_DOWN,
            repeatCount = 0,
            ringing = true,
            pressAlreadyAccepted = false,
        )
        assertTrue(up.snooze)
        assertTrue(up.consume)
        assertTrue(down.snooze)
        assertTrue(down.consume)
    }

    @Test
    fun repeatAndKeyUpDoNotSnoozeAgain() {
        val repeat = VolumeKeyPolicy.forActivityKey(
            keyCode = KeyEvent.KEYCODE_VOLUME_DOWN,
            action = KeyEvent.ACTION_DOWN,
            repeatCount = 4,
            ringing = true,
            pressAlreadyAccepted = true,
        )
        val up = VolumeKeyPolicy.forActivityKey(
            keyCode = KeyEvent.KEYCODE_VOLUME_UP,
            action = KeyEvent.ACTION_UP,
            repeatCount = 0,
            ringing = true,
            pressAlreadyAccepted = true,
        )
        assertFalse(repeat.snooze)
        assertTrue(repeat.consume)
        assertFalse(up.snooze)
        assertTrue(up.consume)
    }

    @Test
    fun volumeKeysPassThroughWhenNothingIsRinging() {
        val decision = VolumeKeyPolicy.forActivityKey(
            keyCode = KeyEvent.KEYCODE_VOLUME_DOWN,
            action = KeyEvent.ACTION_DOWN,
            repeatCount = 0,
            ringing = false,
            pressAlreadyAccepted = false,
        )
        assertFalse(decision.snooze)
        assertFalse(decision.consume)
    }

    @Test
    fun otherKeysAreIgnoredEvenWhileRinging() {
        val decision = VolumeKeyPolicy.forActivityKey(
            keyCode = KeyEvent.KEYCODE_BACK,
            action = KeyEvent.ACTION_DOWN,
            repeatCount = 0,
            ringing = true,
            pressAlreadyAccepted = false,
        )
        assertFalse(decision.snooze)
        assertFalse(decision.consume)
    }

    @Test
    fun remoteVolumeRaiseOrLowerIsOneSnoozePress() {
        assertTrue(VolumeKeyPolicy.remoteAdjustRequestsSnooze(AudioManager.ADJUST_RAISE))
        assertTrue(VolumeKeyPolicy.remoteAdjustRequestsSnooze(AudioManager.ADJUST_LOWER))
        assertFalse(VolumeKeyPolicy.remoteAdjustRequestsSnooze(AudioManager.ADJUST_SAME))
        assertFalse(VolumeKeyPolicy.remoteAdjustRequestsSnooze(AudioManager.ADJUST_MUTE))
    }

    @Test
    fun onePressIsAcceptedOnceUntilTheNextRing() {
        VolumeSnoozeGate.onRingingStarted()
        assertTrue(VolumeSnoozeGate.tryConsume())
        assertFalse(VolumeSnoozeGate.tryConsume())
        assertTrue(VolumeSnoozeGate.isConsumed())

        VolumeSnoozeGate.onRingingStarted()
        assertFalse(VolumeSnoozeGate.isConsumed())
        assertTrue(VolumeSnoozeGate.tryConsume())
    }
}
