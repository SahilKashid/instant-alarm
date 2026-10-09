package dev.sahilkashid.instantalarm.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StartAlarmShortcutTest {
    @Test
    fun onlyTheStartAlarmActionIsTheShortcut() {
        assertTrue(StartAlarmShortcut.matches(StartAlarmShortcut.ACTION))
        assertFalse(StartAlarmShortcut.matches("android.intent.action.MAIN"))
        assertFalse(StartAlarmShortcut.matches(null))
        assertFalse(StartAlarmShortcut.matches(""))
    }

    @Test
    fun staticShortcutIsDeclaredOnTheLauncherActivity() {
        val shortcuts = readProjectFile("src/main/res/xml/shortcuts.xml").readText()
        val manifest = readProjectFile("src/main/AndroidManifest.xml").readText()

        assertTrue(shortcuts.contains("android:shortcutId=\"${StartAlarmShortcut.ID}\""))
        assertTrue(shortcuts.contains("android:action=\"${StartAlarmShortcut.ACTION}\""))
        assertTrue(
            shortcuts.contains(
                "android:targetClass=\"dev.sahilkashid.instantalarm.alarm.StartAlarmActivity\"",
            ),
        )
        assertFalse(shortcuts.contains("MainActivity"))
        assertTrue(shortcuts.contains("@string/shortcut_start_alarm"))
        assertTrue(shortcuts.contains("@drawable/ic_shortcut_start_alarm"))

        assertTrue(manifest.contains("android.app.shortcuts"))
        assertTrue(manifest.contains("@xml/shortcuts"))
        assertTrue(manifest.contains(".alarm.StartAlarmActivity"))
        assertTrue(manifest.contains("android:excludeFromRecents=\"true\""))
        assertTrue(manifest.contains("android:noHistory=\"true\""))
        assertTrue(manifest.contains("android:taskAffinity=\"\""))
        assertTrue(manifest.contains("@android:style/Theme.NoDisplay"))
        assertTrue(
            manifest.contains("android.intent.action.MAIN") &&
                manifest.contains("android.intent.category.LAUNCHER"),
        )
        assertEquals("start_alarm", StartAlarmShortcut.ID)
    }

    private fun readProjectFile(relative: String): File {
        val candidates = listOf(File(relative), File("app/$relative"))
        return candidates.firstOrNull { it.isFile }
            ?: error("Could not find $relative from ${File(".").absolutePath}")
    }
}
