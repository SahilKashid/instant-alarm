package dev.sahilkashid.instantalarm

import android.app.Application
import dev.sahilkashid.instantalarm.alarm.AlarmNotifier

class InstantAlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AlarmNotifier.ensureChannels(this)
    }
}
