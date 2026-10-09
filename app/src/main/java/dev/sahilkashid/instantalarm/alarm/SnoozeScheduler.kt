package dev.sahilkashid.instantalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.sahilkashid.instantalarm.MainActivity
import dev.sahilkashid.instantalarm.domain.SnoozeDuration

data class SnoozePlan(
    val untilEpochMillis: Long,
    val exact: Boolean,
)

/**
 * Wall-clock snooze via [AlarmManager.setAlarmClock], so it can fire while the
 * app is backgrounded. Falls back to an idle-allowed inexact alarm when exact
 * alarms are unavailable.
 */
object SnoozeScheduler {
    private const val PREFS = "instant_alarm"
    private const val KEY_UNTIL = "snooze_until"
    private const val KEY_EXACT = "snooze_exact"
    private const val KEY_MINUTES = "snooze_minutes"
    private const val REQUEST_FIRE = 4101
    private const val REQUEST_SHOW = 4102

    fun minutes(context: Context): Int {
        val stored = prefs(context).getInt(KEY_MINUTES, SnoozeDuration.DEFAULT_MINUTES)
        return SnoozeDuration.coerce(stored)
    }

    fun setMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_MINUTES, SnoozeDuration.coerce(minutes)).apply()
    }

    fun canUseExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
        return canExact(alarmManager)
    }

    fun pendingUntil(context: Context): Long? {
        val until = prefs(context).getLong(KEY_UNTIL, 0L)
        return until.takeIf { it > 0L }
    }

    fun pendingWasExact(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_EXACT, false)
    }

    fun schedule(context: Context, minutes: Int): SnoozePlan {
        val coerced = SnoozeDuration.coerce(minutes)
        setMinutes(context, coerced)
        val until = SnoozeDuration.triggerAtMillis(System.currentTimeMillis(), coerced)
        return scheduleAt(context, until)
    }

    fun scheduleAt(context: Context, untilEpochMillis: Long): SnoozePlan {
        val app = context.applicationContext
        val alarmManager = app.getSystemService(AlarmManager::class.java)
        val operation = fireIntent(app)
        var usedExact = false
        if (alarmManager != null && canExact(alarmManager)) {
            try {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(untilEpochMillis, showIntent(app)),
                    operation,
                )
                usedExact = true
            } catch (_: SecurityException) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, untilEpochMillis, operation)
            }
        } else {
            alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, untilEpochMillis, operation)
        }
        prefs(app).edit()
            .putLong(KEY_UNTIL, untilEpochMillis)
            .putBoolean(KEY_EXACT, usedExact)
            .apply()
        return SnoozePlan(untilEpochMillis, usedExact)
    }

    fun cancel(context: Context) {
        val app = context.applicationContext
        app.getSystemService(AlarmManager::class.java)?.cancel(fireIntent(app))
        prefs(app).edit().remove(KEY_UNTIL).remove(KEY_EXACT).apply()
        AlarmNotifier.cancelSnoozeNotice(app)
    }

    fun markFired(context: Context) {
        val app = context.applicationContext
        prefs(app).edit().remove(KEY_UNTIL).remove(KEY_EXACT).apply()
        AlarmNotifier.cancelSnoozeNotice(app)
    }

    fun restoreAfterBoot(context: Context) {
        val until = pendingUntil(context) ?: return
        val trigger = if (until <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 1_500L
        } else {
            until
        }
        scheduleAt(context, trigger)
    }

    private fun canExact(alarmManager: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun fireIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_FIRE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun showIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_SHOW,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
