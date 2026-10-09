# Instant Alarm

Instant Alarm is an Android alarm that does **not** go off at a scheduled time. Opening the app is the trigger: every cold start, and every time the app is brought back to the foreground, starts the alarm sound and vibration and shows the alarm screen.

There is no time picker, no repeating schedule, and no alarm list.

## Alarm screen

The screen is edge-to-edge. A dark indigo gradient runs from near-black at the top through purple into a warm mauve/peach at the bottom. It shows:

- the label **Alarm**
- the current time, large, updating every second, in the device 12-hour or 24-hour format (`5:00` or `17:00`)
- the date (`Fri, October 9`)
- a round dismiss button
- a snooze pill, default **Snooze 5 mins**

**−** and **+** change the snooze length from 1 to 30 minutes in steps of 1. Tapping the middle of the pill snoozes. While snoozed, the screen shows **Snoozed until HH:MM** and the sound stops. The X stops sound and vibration, cancels a pending snooze, and closes the app. The next open rings again.

## Behavior

- Sound uses the default alarm ringtone (`RingtoneManager.TYPE_ALARM`), then the notification sound, then the phone ringtone. Playback is looping on the alarm stream (`USAGE_ALARM`). A waveform vibration repeats until dismiss or snooze. The player is released when ringing stops.
- The activity shows over the lock screen and turns the screen on (`showWhenLocked`, `turnScreenOn`, plus the legacy window flags on API 26).
- Snooze is an exact `AlarmManager.setAlarmClock` alarm, so it rings even if the app is backgrounded. When it fires, a media-playback foreground service starts the sound and posts a full-screen alarm notification that opens this screen. Reopening the app during a snooze cancels that alarm and rings immediately.
- If exact alarms are not allowed (Android 12+), snooze falls back to `setAndAllowWhileIdle` and the snoozed screen says the alarm may be delayed. Tapping that line opens the exact-alarm setting.
- Notification permission (Android 13+) and full-screen intent access (Android 14+) are requested or explained the same way. Denying them does not block the in-app ring; it only limits how a background snooze can wake the screen.
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

`minSdk` is 26. Compile SDK is 37.2 (latest stable platform) and `targetSdk` is 37. `applicationId` is `dev.sahilkashid.instantalarm`. The current version is `0.1.0-debug` (`versionCode` 1).

Unit tests cover snooze duration bounds (1–30 minutes) and clock / “Snoozed until” formatting. They do not need a device.

## Releases

Debug builds are published as a **temporary GitHub prerelease** tagged `debug`:

1. `./gradlew assembleDebug`
2. Upload the full debug APK (`app/build/outputs/apk/debug/app-debug.apk`) to a GitHub prerelease whose tag is `debug`.
3. When a newer debug build is published, move the `debug` tag to that commit and replace the APK asset. The tag is temporary, not a versioned release.

Versioned releases (a version tag, release notes, and a signed artifact) are published **only when explicitly requested**. Until then the app stays on `0.1.0-debug`.

Do not commit APKs, `local.properties`, or keystores. They are listed in `.gitignore`.
