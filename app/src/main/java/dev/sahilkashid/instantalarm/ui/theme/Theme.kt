package dev.sahilkashid.instantalarm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlarmColors = darkColorScheme(
    primary = Color(0xFF7776F8),
    onPrimary = Color.White,
    background = Color(0xFF0E0024),
    surface = Color(0xFF0E0024),
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun InstantAlarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlarmColors,
        content = content,
    )
}
