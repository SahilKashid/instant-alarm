package dev.sahilkashid.instantalarm.alarm

import android.app.Activity
import android.os.Bundle

/**
 * Target of the launcher "Start alarm" shortcut. It never draws a window:
 * it starts the ringing service and finishes. [RingingService] opens the
 * alarm screen only when the phone is locked or the screen is off. While the
 * phone is unlocked and in use, the service posts the heads-up notification.
 */
class StartAlarmActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlarmController.onShortcutLaunch(applicationContext)
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}
