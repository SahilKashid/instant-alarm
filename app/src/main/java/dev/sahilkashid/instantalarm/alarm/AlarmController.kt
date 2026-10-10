package dev.sahilkashid.instantalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context

/**
 * Opening the app or using the Start alarm shortcut rings and cancels a
 * pending snooze. Snooze itself closes the alarm screen.
 */
object AlarmController {
    private var finisher: (() -> Unit)? = null

    fun attachFinisher(block: () -> Unit) {
        finisher = block
    }

    fun detachFinisher(block: () -> Unit) {
        if (finisher === block) finisher = null
    }

    fun onActivityForeground(context: Context) {
        beginRinging(context)
    }

    /**
     * Launcher "Start alarm" shortcut. Cancels a pending snooze and rings.
     * The service decides whether to show the alarm screen: locked or screen-off
     * opens it, an unlocked phone in use only posts the heads-up.
     */
    fun onShortcutLaunch(context: Context) {
        beginRinging(context, RingingService.REASON_SHORTCUT)
    }

    fun onSnoozeFired(context: Context) {
        ensureRinging(context, RingingService.REASON_SNOOZE)
    }

    fun snooze(
        context: Context,
        minutes: Int,
        pending: BroadcastReceiver.PendingResult? = null,
    ) {
        val app = context.applicationContext
        // Same immediate silence as dismiss. setAlarmClock and the shade
        // notice used to run on this stack, before finish was even queued,
        // which is why Snooze sat on screen longer than X.
        AlarmRinger.stop()
        RingingService.stop(app)
        SnoozeCloseOrder.closeThenSchedule(
            closeScreen = {
                if (SnoozePresentation.closesAlarmScreen()) finisher?.invoke()
            },
            schedule = {
                val plan = SnoozeScheduler.schedule(app, minutes)
                if (SnoozePresentation.postsShadeNotice()) {
                    AlarmNotifier.showSnoozeNotice(app, plan.untilEpochMillis)
                }
            },
            handoff = { work -> SnoozeWork.enqueue(app, pending, work) },
        )
    }

    fun dismiss(context: Context) {
        val app = context.applicationContext
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
        ensureRinging(context, reason)
    }

    private fun ensureRinging(context: Context, reason: String) {
        VolumeSnoozeGate.onRingingStarted()
        try {
            RingingService.start(context, reason)
        } catch (_: RuntimeException) {
            AlarmRinger.start(context.applicationContext)
        }
    }
}
