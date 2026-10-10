# Components the system and notifications start by class name. R8 must keep
# them, including the shortcut trampoline, the ringing service, and the
# receivers that notification actions and the snooze alarm deliver to.

-keep class dev.sahilkashid.instantalarm.InstantAlarmApp { *; }
-keep class dev.sahilkashid.instantalarm.MainActivity { *; }
-keep class dev.sahilkashid.instantalarm.alarm.StartAlarmActivity { *; }
-keep class dev.sahilkashid.instantalarm.alarm.RingingService { *; }
-keep class dev.sahilkashid.instantalarm.alarm.RingingService$AlarmVolumeProvider { *; }
-keep class dev.sahilkashid.instantalarm.alarm.AlarmReceiver { *; }
-keep class dev.sahilkashid.instantalarm.alarm.BootReceiver { *; }

# Notification Dismiss and Snooze, and the snooze fire, match these action names.
-keepclassmembers class dev.sahilkashid.instantalarm.alarm.AlarmReceiver {
    public static final java.lang.String ACTION_FIRE;
    public static final java.lang.String ACTION_DISMISS;
    public static final java.lang.String ACTION_SNOOZE;
}
