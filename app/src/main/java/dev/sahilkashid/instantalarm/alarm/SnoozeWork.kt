package dev.sahilkashid.instantalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.os.PowerManager
import java.util.concurrent.Executors

/**
 * Schedules the snooze and posts the shade notice after the screen has been
 * told to close. A short wake lock and, for the notification action, the
 * receiver's [BroadcastReceiver.goAsync] result keep the process alive until
 * that work finishes.
 */
object SnoozeWork {
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "instant-alarm-snooze")
    }

    fun enqueue(
        context: Context,
        pending: BroadcastReceiver.PendingResult?,
        work: () -> Unit,
    ) {
        val app = context.applicationContext
        executor.execute {
            val lock = wakeLock(app)
            try {
                lock?.acquire(WAKE_TIMEOUT_MS)
                work()
            } finally {
                if (lock?.isHeld == true) lock.release()
                pending?.finish()
            }
        }
    }

    private fun wakeLock(context: Context): PowerManager.WakeLock? {
        val power = context.getSystemService(PowerManager::class.java) ?: return null
        return power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "instantalarm:snooze").apply {
            setReferenceCounted(false)
        }
    }

    private const val WAKE_TIMEOUT_MS = 10_000L
}
