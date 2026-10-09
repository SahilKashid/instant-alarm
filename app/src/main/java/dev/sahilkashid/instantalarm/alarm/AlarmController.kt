package dev.sahilkashid.instantalarm.alarm

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manual trigger wins: any foreground visit starts ringing and cancels a
 * pending snooze, except the same visit that just tapped Snooze.
 */
object AlarmController {
    private val _phase = MutableStateFlow<AlarmPhase>(AlarmPhase.Ringing)
    val phase: StateFlow<AlarmPhase> = _phase.asStateFlow()

    private var suppressRing = false
    private var finisher: (() -> Unit)? = null

    fun attachFinisher(block: () -> Unit) {
        finisher = block
    }

    fun detachFinisher(block: () -> Unit) {
        if (finisher === block) finisher = null
    }

    fun onActivityForeground(context: Context) {
        if (suppressRing) {
            val until = SnoozeScheduler.pendingUntil(context)
            if (until != null && until > System.currentTimeMillis()) {
                _phase.value = AlarmPhase.Snoozed(until, SnoozeScheduler.pendingWasExact(context))
                return
            }
            suppressRing = false
        }
        beginRinging(context)
    }

    /**
     * Launcher "Start alarm" shortcut. Cancels a pending snooze and rings.
     * The service decides whether to show the alarm screen: locked or screen-off
     * opens it, an unlocked phone in use only posts the heads-up.
     */
    fun onShortcutLaunch(context: Context) {
        suppressRing = false
        beginRinging(context, RingingService.REASON_SHORTCUT)
    }

    fun onActivityBackground() {
        suppressRing = false
    }

    fun onSnoozeFired(context: Context) {
        suppressRing = false
        _phase.value = AlarmPhase.Ringing
        ensureRinging(context, RingingService.REASON_SNOOZE)
    }

    fun snooze(context: Context, minutes: Int) {
        val app = context.applicationContext
        AlarmRinger.stop()
        RingingService.stop(app)
        val plan = SnoozeScheduler.schedule(app, minutes)
        suppressRing = true
        _phase.value = AlarmPhase.Snoozed(plan.untilEpochMillis, plan.exact)
        AlarmNotifier.showSnoozeNotice(app, plan.untilEpochMillis)
    }

    fun dismiss(context: Context) {
        val app = context.applicationContext
        suppressRing = false
        AlarmRinger.stop()
        RingingService.stop(app)
        SnoozeScheduler.cancel(app)
        finisher?.invoke()
    }

    private fun beginRinging(
        context: Context,
        reason: String = RingingService.REASON_MANUAL,
    ) {
        SnoozeScheduler.cancel(context)
        _phase.value = AlarmPhase.Ringing
        ensureRinging(context, reason)
    }

    private fun ensureRinging(context: Context, reason: String) {
        try {
            RingingService.start(context, reason)
        } catch (_: RuntimeException) {
            AlarmRinger.start(context.applicationContext)
        }
    }
}
