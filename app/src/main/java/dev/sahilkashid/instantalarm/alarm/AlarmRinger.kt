package dev.sahilkashid.instantalarm.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * One looping alarm player and vibration pattern for the process.
 * [stop] always releases the player so a dismiss cannot leave it running.
 */
object AlarmRinger {
    private val lock = Any()
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var playing = false

    fun isPlaying(): Boolean = synchronized(lock) { playing }

    fun start(context: Context) {
        val app = context.applicationContext
        synchronized(lock) {
            if (playing) return
            playing = true
            startPlayer(app)
            startVibration(app)
        }
    }

    fun stop() {
        synchronized(lock) {
            playing = false
            releasePlayer()
            vibrator?.cancel()
            vibrator = null
        }
    }

    private fun startPlayer(context: Context) {
        for (uri in soundCandidates(context)) {
            val created = MediaPlayer()
            try {
                created.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                created.setDataSource(context, uri)
                created.isLooping = true
                created.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                created.setOnErrorListener { mp, _, _ ->
                    try {
                        mp.release()
                    } catch (_: Exception) {
                    }
                    synchronized(lock) {
                        if (player === mp) player = null
                    }
                    true
                }
                created.prepare()
                created.start()
                player = created
                return
            } catch (_: Exception) {
                created.release()
            }
        }
    }

    private fun soundCandidates(context: Context): List<Uri> {
        val types = intArrayOf(
            RingtoneManager.TYPE_ALARM,
            RingtoneManager.TYPE_NOTIFICATION,
            RingtoneManager.TYPE_RINGTONE,
        )
        val uris = LinkedHashSet<Uri>()
        for (type in types) {
            RingtoneManager.getActualDefaultRingtoneUri(context, type)?.let { uris.add(it) }
        }
        for (type in types) {
            RingtoneManager.getDefaultUri(type)?.let { uris.add(it) }
        }
        return uris.toList()
    }

    private fun startVibration(context: Context) {
        val vibrator = vibrator(context)
        this.vibrator = vibrator
        if (!vibrator.hasVibrator()) return
        val timings = longArrayOf(0, 700, 500, 700, 1_600)
        val effect = if (vibrator.hasAmplitudeControl()) {
            VibrationEffect.createWaveform(
                timings,
                intArrayOf(0, 255, 0, 180, 0),
                0,
            )
        } else {
            VibrationEffect.createWaveform(timings, 0)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
            vibrator.vibrate(effect, attributes)
        } else {
            vibrator.vibrate(effect)
        }
    }

    private fun vibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun releasePlayer() {
        val current = player ?: return
        player = null
        try {
            if (current.isPlaying) current.stop()
        } catch (_: Exception) {
        }
        try {
            current.reset()
        } catch (_: Exception) {
        }
        current.release()
    }
}
