package dev.sahilkashid.instantalarm.ui

import android.app.Activity
import android.text.format.DateFormat
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sahilkashid.instantalarm.R
import dev.sahilkashid.instantalarm.alarm.AlarmPhase
import dev.sahilkashid.instantalarm.domain.ClockText
import dev.sahilkashid.instantalarm.domain.SnoozeDuration
import dev.sahilkashid.instantalarm.ui.theme.InstantAlarmTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.Locale

private val AlarmBackground = Brush.verticalGradient(
    colorStops = arrayOf(
        0.00f to Color(0xFF0E0024),
        0.15f to Color(0xFF140234),
        0.32f to Color(0xFF160436),
        0.48f to Color(0xFF2A1244),
        0.62f to Color(0xFF4A2848),
        0.76f to Color(0xFF704853),
        0.88f to Color(0xFF8B5E59),
        1.00f to Color(0xFF9C6D5D),
    ),
)

@Composable
fun AlarmScreen(
    phase: AlarmPhase,
    snoozeMinutes: Int,
    permissionHint: String?,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onDecreaseSnooze: () -> Unit,
    onIncreaseSnooze: () -> Unit,
    onPermissionHintClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val now = rememberNow()
    val is24Hour = DateFormat.is24HourFormat(context)
    val locale = Locale.getDefault()
    val timeText = ClockText.formatTime(now.toLocalTime(), is24Hour, locale)
    val dateText = ClockText.formatDate(now.toLocalDate(), locale)
    val snoozedLabel = (phase as? AlarmPhase.Snoozed)?.let { snoozed ->
        ClockText.formatSnoozedUntil(snoozed.untilEpochMillis, is24Hour, java.time.ZoneId.systemDefault(), locale)
    }

    KeepScreenOn(enabled = phase is AlarmPhase.Ringing)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AlarmBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(36.dp))
            Text(
                text = stringResource(R.string.alarm_label),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = timeText,
                color = Color.White,
                fontSize = 84.sp,
                lineHeight = 88.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateText,
                color = Color.White.copy(alpha = 0.94f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
            )
            if (snoozedLabel != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = snoozedLabel,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                )
            }
            if (permissionHint != null && phase is AlarmPhase.Snoozed) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = permissionHint,
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clickable(onClick = onPermissionHintClick),
                )
            }
            Spacer(Modifier.weight(1.4f))
            DismissButton(onDismiss)
            Spacer(Modifier.weight(0.65f))
            SnoozePill(
                minutes = snoozeMinutes,
                onDecrease = onDecreaseSnooze,
                onIncrease = onIncreaseSnooze,
                onSnooze = onSnooze,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun rememberNow(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000L - (System.currentTimeMillis() % 1_000L))
        }
    }
    return now
}

@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        val window = (view.context as? Activity)?.window
        if (enabled) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

@Composable
private fun DismissButton(onDismiss: () -> Unit) {
    val label = stringResource(R.string.dismiss_alarm)
    Box(
        modifier = Modifier.size(168.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x66F4C9D2),
                        0.30f to Color(0x55E2AAB8),
                        0.46f to Color(0x28C48B9A),
                        0.62f to Color.Transparent,
                    ),
                ),
            )
        }
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF5A182C), Color(0xFF3C0E1C)),
                    ),
                )
                .clickable(onClickLabel = label, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(26.dp)) {
                val stroke = 2.5.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun SnoozePill(
    minutes: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(CircleShape)
            .background(Color(0xFF7776F8))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepCircle(
            symbol = "−",
            description = stringResource(R.string.decrease_snooze),
            enabled = minutes > SnoozeDuration.MIN_MINUTES,
            onClick = onDecrease,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onSnooze),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = SnoozeDuration.label(minutes),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
            )
        }
        StepCircle(
            symbol = "+",
            description = stringResource(R.string.increase_snooze),
            enabled = minutes < SnoozeDuration.MAX_MINUTES,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun StepCircle(
    symbol: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF5558D0))
            .clickable(enabled = enabled, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.35f),
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AlarmScreenPreview() {
    InstantAlarmTheme {
        AlarmScreen(
            phase = AlarmPhase.Ringing,
            snoozeMinutes = 5,
            permissionHint = null,
            onDismiss = {},
            onSnooze = {},
            onDecreaseSnooze = {},
            onIncreaseSnooze = {},
            onPermissionHintClick = {},
        )
    }
}
