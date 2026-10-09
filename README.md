# Instant Alarm

Instant Alarm is an Android alarm that does **not** go off at a scheduled time. Opening the app is the trigger: every cold start, and every time the app is brought back to the foreground, starts the alarm sound and vibration and shows the alarm screen. Long-pressing the icon shows a **Start alarm** shortcut that rings immediately without opening the screen while the phone is unlocked and in use.

There is no time picker, no repeating schedule, and no alarm list.

## Alarm screen

The screen is edge-to-edge. A dark indigo gradient runs from near-black at the top through purple into a warm mauve/peach at the bottom. It shows:

- the label **Alarm**
- the current time, large, updating every second, in the device 12-hour or 24-hour format (`5:00` or `17:00`)
- the date (`Fri, October 9`)
- a round dismiss button
- a snooze pill, default **Snooze 5 mins**

Long-pressing the launcher icon shows **Start alarm**. It starts ringing immediately, including from a cold start and when a snooze is pending, and it cancels that snooze. While the phone is unlocked and in use it does not open the app: a foreground service rings in the background and posts the same heads-up notification (Dismiss, Snooze, and a tap that opens the alarm screen). If the phone is locked or the screen is off, the shortcut still opens the full-screen alarm. The shortcut activity itself never draws a window and does not stay in Recents. Opening the app from its icon still opens the alarm screen and rings.

**−** and **+** change the snooze length from 1 to 30 minutes. Below 5 they step by 1 minute: from 5, **−** goes 4, 3, 2, 1 and then stops. At 5 and above they step by 5: from 5, **+** goes 10, 15, and so on up to 30, and from 10, **−** goes to 5. The label is **Snooze 1 min** or **Snooze 5 mins**. Tapping the middle of the pill snoozes. While snoozed, the screen shows **Snoozed until HH:MM** and the sound stops. The X stops sound and vibration, cancels a pending snooze, and closes the app. The next open rings again.

## Behavior

- Sound uses the default alarm ringtone (`RingtoneManager.TYPE_ALARM`), then the notification sound, then the phone ringtone. Playback is looping on the alarm stream (`USAGE_ALARM`). A waveform vibration repeats until dismiss or snooze. The player is released when ringing stops.
- The alarm activity can draw over the lock screen and turn the screen on (`showWhenLocked`, `turnScreenOn`, plus the legacy window flags).
- Snooze is an exact `AlarmManager.setAlarmClock` alarm, so it rings even if the app is backgrounded. When it fires, a media-playback foreground service starts the sound and posts a high-importance alarm notification (`CATEGORY_ALARM`) with Dismiss and Snooze actions. Reopening the app during a snooze cancels that alarm and rings immediately.
- If the phone is locked or the screen is off, that notification’s full-screen intent opens this alarm screen over the lock screen and turns the screen on. The service also starts the activity in that case when the system allows a direct launch. If the phone is unlocked and in use, the screen is not taken over: the same notification is a heads-up, and tapping it opens the alarm screen. The app does not start the activity itself while the phone is unlocked and in use.
- If exact alarms are not allowed (Android 12+), snooze falls back to `setAndAllowWhileIdle` and the snoozed screen says the alarm may be delayed. Tapping that line opens the exact-alarm setting.
- Notification permission (Android 13+) is requested the same way. On Android 14+, full-screen alarms need the full-screen intent special access. When it is missing, the alarm screen shows **Allow full-screen alarms on the lock screen**. Tapping it opens that setting, or the app’s system page if that setting screen is not available. The full-screen line, and the notification and exact-alarm lines, are read again as soon as the screen resumes and when the exact-alarm permission changes, so a grant shows up the moment you return from settings. That return does not start or repeat the ring. Denying these does not block the in-app ring; it only limits how a background snooze can take over a locked phone.
- A reboot restores a still-pending snooze. Swiping the app away while it is ringing stops the sound.

## Build

Requirements:

- JDK 17 or newer (Android Gradle Plugin 9.4). The project compiles Java 17 bytecode. JDK 21 is fine.
- Android SDK, compile/target API 37, build-tools 36. Point the SDK at the project with `ANDROID_HOME` or a gitignored `local.properties`:

```properties
sdk.dir=/path/to/Android/sdk
```

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`.

`minSdk` is 26. Compile SDK is 37.2 (latest stable platform) and `targetSdk` is 37. `applicationId` is `dev.sahilkashid.instantalarm`. The current version is `0.1.0-debug` (`versionCode` 5).

Unit tests cover snooze duration (1–30 minutes, 1-minute steps below 5 and 5-minute steps from 5 up), when a firing snooze or the **Start alarm** shortcut may open the alarm screen (locked or screen off) versus a heads-up only (unlocked and in use), permission banners clearing when access is granted, a return from settings not counting as a new open, the shortcut trampoline, and clock / “Snoozed until” formatting. They do not need a device.

## Releases

Debug builds are published as a **temporary GitHub prerelease** tagged `debug`:

1. `./gradlew assembleDebug`
2. Upload the full debug APK (`app/build/outputs/apk/debug/app-debug.apk`) to a GitHub prerelease whose tag is `debug`.
3. When a newer debug build is published, move the `debug` tag to that commit and replace the APK asset. The tag is temporary, not a versioned release.

Versioned releases (a version tag, release notes, and a signed artifact) are published **only when explicitly requested**. Until then the app stays on `0.1.0-debug`.

Do not commit APKs, `local.properties`, or keystores. They are listed in `.gitignore`.
