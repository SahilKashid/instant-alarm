package dev.sahilkashid.instantalarm

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sahilkashid.instantalarm.alarm.AlarmController
import dev.sahilkashid.instantalarm.alarm.AlarmLaunchPolicy
import dev.sahilkashid.instantalarm.alarm.AlarmNotifier
import dev.sahilkashid.instantalarm.alarm.AlarmPhase
import dev.sahilkashid.instantalarm.alarm.AlarmRinger
import dev.sahilkashid.instantalarm.alarm.RingingService
import dev.sahilkashid.instantalarm.alarm.SnoozeScheduler
import dev.sahilkashid.instantalarm.alarm.StartAlarmShortcut
import dev.sahilkashid.instantalarm.domain.SnoozeDuration
import dev.sahilkashid.instantalarm.ui.AlarmScreen
import dev.sahilkashid.instantalarm.ui.theme.InstantAlarmTheme

class MainActivity : ComponentActivity() {
    private val finisher = { finish() }
    private var askedForNotifications = false

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* The alarm still rings if notifications are denied. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before the window is created, so a full-screen intent can show this
        // activity over the lock screen and turn the display on.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        AlarmController.attachFinisher(finisher)
        setContent {
            InstantAlarmTheme {
                val phase by AlarmController.phase.collectAsStateWithLifecycle()
                var snoozeMinutes by remember {
                    mutableIntStateOf(SnoozeScheduler.minutes(this@MainActivity))
                }
                AlarmScreen(
                    phase = phase,
                    snoozeMinutes = snoozeMinutes,
                    permissionHint = if (phase is AlarmPhase.Snoozed) snoozeHint() else null,
                    fullScreenPrompt = fullScreenPrompt(),
                    onDismiss = { AlarmController.dismiss(this@MainActivity) },
                    onSnooze = { AlarmController.snooze(this@MainActivity, snoozeMinutes) },
                    onDecreaseSnooze = {
                        snoozeMinutes = SnoozeDuration.decrement(snoozeMinutes)
                        SnoozeScheduler.setMinutes(this@MainActivity, snoozeMinutes)
                    },
                    onIncreaseSnooze = {
                        snoozeMinutes = SnoozeDuration.increment(snoozeMinutes)
                        SnoozeScheduler.setMinutes(this@MainActivity, snoozeMinutes)
                    },
                    onPermissionHintClick = { openHintSettings() },
                    onFullScreenPromptClick = { openFullScreenAccess() },
                )
            }
        }
        requestNotificationPermission()
        deliverShortcut(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showOverLockScreen()
        deliverShortcut(intent)
    }

    private fun deliverShortcut(intent: Intent?) {
        if (!StartAlarmShortcut.matches(intent?.action)) return
        AlarmController.onShortcutLaunch(this)
        // Drop the action so a later recreate, such as rotation, does not ring again.
        intent?.action = Intent.ACTION_MAIN
    }

    override fun onStart() {
        super.onStart()
        AlarmController.onActivityForeground(this)
    }

    override fun onStop() {
        if (!isChangingConfigurations) {
            AlarmController.onActivityBackground()
        }
        super.onStop()
    }

    override fun onDestroy() {
        AlarmController.detachFinisher(finisher)
        if (isFinishing && !isChangingConfigurations) {
            AlarmRinger.stop()
            RingingService.stop(applicationContext)
        }
        super.onDestroy()
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        )
    }

    private fun requestNotificationPermission() {
        if (askedForNotifications || Build.VERSION.SDK_INT < 33) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        askedForNotifications = true
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun snoozeHint(): String? {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return getString(R.string.hint_notifications)
        }
        if (!SnoozeScheduler.canUseExactAlarms(this)) {
            return getString(R.string.hint_exact)
        }
        return null
    }

    private fun fullScreenPrompt(): String? {
        val offer = AlarmLaunchPolicy.shouldOfferFullScreenAccess(
            Build.VERSION.SDK_INT,
            AlarmNotifier.canUseFullScreenIntent(this),
        )
        return if (offer) getString(R.string.hint_fullscreen) else null
    }

    private fun openHintSettings() {
        val intent = when {
            Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED -> {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
            }
            !SnoozeScheduler.canUseExactAlarms(this) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
            }
            else -> return
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun openFullScreenAccess() {
        val packageUri = Uri.parse("package:$packageName")
        for (action in AlarmLaunchPolicy.fullScreenSettingsActions()) {
            try {
                startActivity(Intent(action).apply { data = packageUri })
                return
            } catch (_: ActivityNotFoundException) {
            }
        }
    }

    companion object {
        const val EXTRA_FROM_SNOOZE = "from_snooze"
    }
}
