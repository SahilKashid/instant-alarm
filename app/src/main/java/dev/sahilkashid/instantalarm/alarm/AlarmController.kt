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
     * Launcher "Start alarm" shortcut. Same ring as opening the app, and it
     * wins even when this visit is still showing a snooze.
     */
    fun onShortcutLaunch(context: Context) {
        suppressRing = false
        beginRinging(context)
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

    private fun beginRinging(context: Context) {
        SnoozeScheduler.cancel(context)
        _phase.value = AlarmPhase.Ringing
        ensureRinging(context, RingingService.REASON_MANUAL)
    }

    private fun ensureRinging(context: Context, reason: String) {
        try {
            RingingService.start(context, reason)
        } catch (_: RuntimeException) {
            AlarmRinger.start(context.applicationContext)
        }
    }
}
