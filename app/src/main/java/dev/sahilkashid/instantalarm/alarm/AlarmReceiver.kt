package dev.sahilkashid.instantalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_FIRE -> {
                SnoozeScheduler.markFired(context)
                AlarmController.onSnoozeFired(context)
            }
            ACTION_DISMISS -> AlarmController.dismiss(context)
            ACTION_SNOOZE -> {
                val pending = goAsync()
                try {
                    AlarmController.snooze(context, SnoozeScheduler.minutes(context), pending)
                } catch (thrown: Throwable) {
                    pending.finish()
                    throw thrown
                }
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "dev.sahilkashid.instantalarm.action.FIRE"
        const val ACTION_DISMISS = "dev.sahilkashid.instantalarm.action.DISMISS"
        const val ACTION_SNOOZE = "dev.sahilkashid.instantalarm.action.SNOOZE"
    }
}
