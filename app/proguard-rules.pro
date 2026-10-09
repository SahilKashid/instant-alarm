# Debug builds are not minified. Keep alarm entry points if release minify is turned on later.
-keep class dev.sahilkashid.instantalarm.alarm.AlarmReceiver { *; }
-keep class dev.sahilkashid.instantalarm.alarm.BootReceiver { *; }
-keep class dev.sahilkashid.instantalarm.alarm.RingingService { *; }
