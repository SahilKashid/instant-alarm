package dev.sahilkashid.instantalarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dev.sahilkashid.instantalarm.MainActivity
import dev.sahilkashid.instantalarm.R
import dev.sahilkashid.instantalarm.domain.ClockText
import java.time.ZoneId

object AlarmNotifier {
    const val CHANNEL_RINGING = "instant_alarm_ring"
    const val CHANNEL_SNOOZE = "instant_alarm_snooze"
    private const val RINGING_ID = 1001
    private const val SNOOZE_ID = 1002
    private const val REQUEST_CONTENT = 4201
    private const val REQUEST_DISMISS = 4202
    private const val REQUEST_SNOOZE = 4203

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val ringing = NotificationChannel(
            CHANNEL_RINGING,
            context.getString(R.string.notification_channel_ringing),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_ringing_desc)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val snooze = NotificationChannel(
            CHANNEL_SNOOZE,
            context.getString(R.string.notification_channel_snooze),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_snooze_desc)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(ringing)
        manager.createNotificationChannel(snooze)
    }

    fun ringingNotification(context: Context): Notification {
        ensureChannels(context)
        val content = activityIntent(context)
        return NotificationCompat.Builder(context, CHANNEL_RINGING)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_ringing))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSound(null)
            .setVibrate(null)
            .setContentIntent(content)
            .setFullScreenIntent(content, true)
            .addAction(
                0,
                context.getString(R.string.notification_snooze),
                broadcastIntent(context, AlarmReceiver.ACTION_SNOOZE, REQUEST_SNOOZE),
            )
            .addAction(
                0,
                context.getString(R.string.notification_dismiss),
                broadcastIntent(context, AlarmReceiver.ACTION_DISMISS, REQUEST_DISMISS),
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun showSnoozeNotice(context: Context, untilEpochMillis: Long) {
        ensureChannels(context)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
        val label = ClockText.formatSnoozedUntil(
            untilEpochMillis,
            is24Hour,
            ZoneId.systemDefault(),
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_SNOOZE)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(label)
            .setContentIntent(activityIntent(context))
            .setAutoCancel(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        try {
            manager.notify(SNOOZE_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS denied. The scheduled alarm still fires.
        }
    }

    fun cancelSnoozeNotice(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(SNOOZE_ID)
    }

    fun canUseFullScreenIntent(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 34) return true
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return manager.canUseFullScreenIntent()
    }

    private fun activityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun broadcastIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
