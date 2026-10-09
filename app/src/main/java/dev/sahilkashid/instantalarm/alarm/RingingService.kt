package dev.sahilkashid.instantalarm.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dev.sahilkashid.instantalarm.MainActivity

/**
 * Keeps the process alive while the alarm is ringing and posts the full-screen
 * alarm notification. Sound itself lives in [AlarmRinger], which is released
 * when this service stops.
 */
class RingingService : Service() {
    private var mediaSession: MediaSession? = null
    private var ownsRinger = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = AlarmNotifier.ringingNotification(this)
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } catch (_: RuntimeException) {
            // Foreground start can be rejected when exact-alarm exemptions are
            // missing. Still ring if the process is alive; dismiss releases it.
            ownsRinger = false
            AlarmRinger.start(applicationContext)
            if (intent?.getStringExtra(EXTRA_REASON) == REASON_SNOOZE) {
                wakeScreen()
                launchAlarmScreen()
            }
            stopSelf()
            return START_NOT_STICKY
        }

        ownsRinger = true
        AlarmRinger.start(applicationContext)
        activateSession()
        if (intent?.getStringExtra(EXTRA_REASON) == REASON_SNOOZE) {
            wakeScreen()
            launchAlarmScreen()
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        AlarmRinger.stop()
        ownsRinger = false
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.let { session ->
            session.isActive = false
            session.release()
        }
        mediaSession = null
        if (ownsRinger) {
            AlarmRinger.stop()
            ownsRinger = false
        }
        super.onDestroy()
    }

    private fun activateSession() {
        if (mediaSession != null) return
        mediaSession = MediaSession(this, "InstantAlarm").apply {
            setPlaybackState(
                PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PLAYING, 0L, 1f)
                    .build(),
            )
            isActive = true
        }
    }

    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        val powerManager = getSystemService(PowerManager::class.java) ?: return
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
            "instantalarm:screen",
        )
        wakeLock.acquire(5_000L)
    }

    private fun launchAlarmScreen() {
        val launch = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
            putExtra(MainActivity.EXTRA_FROM_SNOOZE, true)
        }
        try {
            startActivity(launch)
        } catch (_: RuntimeException) {
            // Full-screen intent on the notification is the fallback.
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_REASON = "reason"
        const val REASON_MANUAL = "manual"
        const val REASON_SNOOZE = "snooze"

        fun start(context: Context, reason: String) {
            val app = context.applicationContext
            val intent = Intent(app, RingingService::class.java).apply {
                putExtra(EXTRA_REASON, reason)
            }
            ContextCompat.startForegroundService(app, intent)
        }

        fun stop(context: Context) {
            context.applicationContext.stopService(Intent(context, RingingService::class.java))
        }
    }
}
