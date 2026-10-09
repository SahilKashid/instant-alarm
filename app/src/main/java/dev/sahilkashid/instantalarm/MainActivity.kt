package dev.sahilkashid.instantalarm

import android.Manifest
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sahilkashid.instantalarm.alarm.AlarmController
import dev.sahilkashid.instantalarm.alarm.AlarmLaunchPolicy
import dev.sahilkashid.instantalarm.alarm.AlarmNotifier
import dev.sahilkashid.instantalarm.alarm.AlarmPhase
import dev.sahilkashid.instantalarm.alarm.AlarmRinger
import dev.sahilkashid.instantalarm.alarm.PermissionBanners
import dev.sahilkashid.instantalarm.alarm.RingingService
import dev.sahilkashid.instantalarm.alarm.SettingsReturn
import dev.sahilkashid.instantalarm.alarm.SnoozeScheduler
import dev.sahilkashid.instantalarm.domain.SnoozeDuration
import dev.sahilkashid.instantalarm.ui.AlarmScreen
import dev.sahilkashid.instantalarm.ui.theme.InstantAlarmTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val finisher = { finish() }
    private var askedForNotifications = false
    private var returningFromSettings = false
    private val permissionEpoch = MutableStateFlow(0)

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { notePermissionChange() }

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { notePermissionChange() }

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
                val banners = rememberPermissionBanners()
                AlarmScreen(
                    phase = phase,
                    snoozeMinutes = snoozeMinutes,
                    permissionHint = if (phase is AlarmPhase.Snoozed) banners.snoozeHint else null,
                    fullScreenPrompt = banners.fullScreenPrompt,
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showOverLockScreen()
        // Tapping the launcher icon is a real open, not a return from settings.
        if (intent.action == Intent.ACTION_MAIN) {
            returningFromSettings = false
        }
    }

    override fun onStart() {
        super.onStart()
        val fromSettings = returningFromSettings
        returningFromSettings = false
        if (SettingsReturn.shouldStartRinging(fromSettings)) {
            AlarmController.onActivityForeground(this)
        }
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
        if (askedForNotifications || notificationsGranted()) return
        askedForNotifications = true
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun notificationsGranted(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notePermissionChange() {
        permissionEpoch.value += 1
    }

    @Composable
    private fun rememberPermissionBanners(): PermissionBannerText {
        val epoch by permissionEpoch.collectAsStateWithLifecycle()
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) notePermissionChange()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    notePermissionChange()
                }
            }
            ContextCompat.registerReceiver(
                this@MainActivity,
                receiver,
                IntentFilter(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                unregisterReceiver(receiver)
            }
        }
        return remember(epoch) { currentPermissionBanners() }
    }

    private fun currentPermissionBanners(): PermissionBannerText {
        val state = PermissionBanners.resolve(
            sdkInt = Build.VERSION.SDK_INT,
            notificationsGranted = notificationsGranted(),
            exactAlarmsAllowed = SnoozeScheduler.canUseExactAlarms(this),
            canUseFullScreenIntent = AlarmNotifier.canUseFullScreenIntent(this),
        )
        val snoozeHint = when (state.snoozeHint) {
            PermissionBanners.NOTIFICATIONS -> getString(R.string.hint_notifications)
            PermissionBanners.EXACT_ALARM -> getString(R.string.hint_exact)
            else -> null
        }
        val fullScreenPrompt = if (state.fullScreen != null) {
            getString(R.string.hint_fullscreen)
        } else {
            null
        }
        return PermissionBannerText(snoozeHint, fullScreenPrompt)
    }

    private fun openHintSettings() {
        val intent = when {
            !notificationsGranted() -> {
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
        launchSettings(intent)
    }

    private fun openFullScreenAccess() {
        val packageUri = Uri.parse("package:$packageName")
        for (action in AlarmLaunchPolicy.fullScreenSettingsActions()) {
            if (launchSettings(Intent(action).apply { data = packageUri })) return
        }
    }

    private fun launchSettings(intent: Intent): Boolean {
        return try {
            returningFromSettings = true
            settingsLauncher.launch(intent)
            true
        } catch (_: ActivityNotFoundException) {
            returningFromSettings = false
            false
        }
    }

    companion object {
        const val EXTRA_FROM_SNOOZE = "from_snooze"
    }
}

private data class PermissionBannerText(
    val snoozeHint: String?,
    val fullScreenPrompt: String?,
)
