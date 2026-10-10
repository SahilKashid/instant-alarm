package dev.sahilkashid.instantalarm.alarm

import android.media.AudioManager
import android.view.KeyEvent
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Hardware volume up/down snoozes a ringing alarm. One press snoozes once:
 * key-up and key-repeat do not snooze again, and the key is consumed so the
 * alarm volume does not change. When nothing is ringing, the keys are left
 * alone.
 */
object VolumeKeyPolicy {
    data class ActivityDecision(
        val snooze: Boolean,
        val consume: Boolean,
    )

    fun forActivityKey(
        keyCode: Int,
        action: Int,
        repeatCount: Int,
        ringing: Boolean,
        pressAlreadyAccepted: Boolean,
    ): ActivityDecision {
        if (!isVolumeKey(keyCode)) return ActivityDecision(snooze = false, consume = false)
        if (!ringing && !pressAlreadyAccepted) return ActivityDecision(snooze = false, consume = false)
        val firstPress = action == KeyEvent.ACTION_DOWN && repeatCount == 0
        return ActivityDecision(snooze = ringing && firstPress, consume = true)
    }

    /** Remote volume adjustments from a media session. Same, mute, and absolute sets are not a press. */
    fun remoteAdjustRequestsSnooze(direction: Int): Boolean {
        return direction == AudioManager.ADJUST_RAISE || direction == AudioManager.ADJUST_LOWER
    }

    private fun isVolumeKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
    }
}

/**
 * One accepted volume press per ring, whether it arrives from the alarm
 * screen, the media session, or both.
 */
object VolumeSnoozeGate {
    private val consumed = AtomicBoolean(false)

    fun onRingingStarted() {
        consumed.set(false)
    }

    fun isConsumed(): Boolean = consumed.get()

    /** True only for the first caller during this ring. */
    fun tryConsume(): Boolean = consumed.compareAndSet(false, true)
}
