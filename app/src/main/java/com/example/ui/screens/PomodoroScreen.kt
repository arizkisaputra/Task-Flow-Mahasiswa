package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.ui.PomodoroMode
import com.example.ui.TaskFlowUiState
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian850
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.util.DateTimeFormatters

@Composable
fun PomodoroScreen(
    uiState: TaskFlowUiState,
    onSwitchMode: (PomodoroMode) -> Unit,
    onSetFocusMinutes: (Int) -> Unit,
    onToggleRunning: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectTask: (Long?) -> Unit,
    onToggleSelectedTaskDone: (TaskEntity) -> Unit,
    onToggleSound: () -> Unit,
    onTestChime: () -> Unit,
    onDismissCompletionModal: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val pomodoro = uiState.pomodoro
    val selectedTask = uiState.allTasks.firstOrNull { it.id == pomodoro.selectedTaskId }
    var showTaskSelector by remember { mutableStateOf(false) }

    val isDark = LocalIsDarkTheme.current
    val isFocus = pomodoro.mode == PomodoroMode.FOCUS
    val accentColor = if (isFocus) {
        if (isDark) Sky400 else Sky600
    } else {
        if (isDark) Emerald400 else Emerald600
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideStudio = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pomodoro_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "pomodoro_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Timer Fokus Pomodoro",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Konsisten tanpa cemas • ")
                                withStyle(
                                    SpanStyle(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Sky400 else Sky600
                                    )
                                ) {
                                    append("${pomodoro.focusSessionsToday} sesi fokus")
                                }
                                append(" hari ini")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Obsidian850 else PureWhite,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onTestChime)
                                .testTag("test_chime_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VolumeUp,
                                    contentDescription = "Uji suara dering",
                                    tint = Sky500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cek Dering",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleSound,
                            modifier = Modifier.testTag("toggle_chime_sound")
                        ) {
                            Icon(
                                imageVector = if (pomodoro.soundEnabled) {
                                    Icons.Outlined.NotificationsActive
                                } else {
                                    Icons.Outlined.NotificationsOff
                                },
                                contentDescription = if (pomodoro.soundEnabled) {
                                    "Matikan suara dering"
                                } else {
                                    "Aktifkan suara dering"
                                },
                                tint = if (pomodoro.soundEnabled) accentColor else Slate400
                            )
                        }
                    }
                }
            }

            if (isWideStudio) {
                item(key = "studio_split") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        MainTimerStudioCard(
                            uiState = uiState,
                            accentColor = accentColor,
                            onSwitchMode = onSwitchMode,
                            onSetFocusMinutes = onSetFocusMinutes,
                            onToggleRunning = onToggleRunning,
                            onResetTimer = onResetTimer,
                            modifier = Modifier.weight(7f)
                        )
                        Column(
                            modifier = Modifier.weight(5f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ActiveFocusTaskCard(
                                selectedTask = selectedTask,
                                onOpenTaskPicker = { showTaskSelector = true },
                                onToggleSelectedTaskDone = onToggleSelectedTaskDone
                            )
                            CalmStudyGuidanceCard()
                        }
                    }
                }
            } else {
                item(key = "main_timer_card") {
                    MainTimerStudioCard(
                        uiState = uiState,
                        accentColor = accentColor,
                        onSwitchMode = onSwitchMode,
                        onSetFocusMinutes = onSetFocusMinutes,
                        onToggleRunning = onToggleRunning,
                        onResetTimer = onResetTimer
                    )
                }

                item(key = "selected_task_card") {
                    ActiveFocusTaskCard(
                        selectedTask = selectedTask,
                        onOpenTaskPicker = { showTaskSelector = true },
                        onToggleSelectedTaskDone = onToggleSelectedTaskDone
                    )
                }

                item(key = "calm_study_guidance") {
                    CalmStudyGuidanceCard()
                }
            }
        }
    }

    if (showTaskSelector) {
        TaskPickerSheet(
            tasks = uiState.allTasks,
            selectedTaskId = pomodoro.selectedTaskId,
            onSelect = { id ->
                onSelectTask(id)
                showTaskSelector = false
            },
            onDismiss = { showTaskSelector = false }
        )
    }

    val finishedMode = pomodoro.completionModalMode
    if (finishedMode != null) {
        AlertDialog(
            onDismissRequest = { onDismissCompletionModal(false) },
            containerColor = if (isDark) Obsidian900 else PureWhite,
            title = {
                Text(
                    text = if (finishedMode == PomodoroMode.FOCUS) {
                        "Alhamdulillah, Sesi Fokus Selesai!"
                    } else {
                        "Waktu Istirahat Selesai"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (finishedMode == PomodoroMode.FOCUS) {
                        "Kerja bagus! Istirahat sejenak 5 menit untuk menjaga kesehatan mental dan ketenangan pikiran."
                    } else {
                        "Pikiran lebih segar? Mari lanjutkan langkah berikutnya dengan niat amanah."
                    },
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                Button(
                    onClick = { onDismissCompletionModal(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Sky400 else Sky600,
                        contentColor = if (isDark) Obsidian950 else PureWhite
                    )
                ) {
                    Text(
                        if (finishedMode == PomodoroMode.FOCUS) "Mulai Istirahat 5m" else "Lanjut Fokus 25m",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissCompletionModal(false) }) {
                    Text("Matikan Dering")
                }
            }
        )
    }
}

@Composable
private fun MainTimerStudioCard(
    uiState: TaskFlowUiState,
    accentColor: Color,
    onSwitchMode: (PomodoroMode) -> Unit,
    onSetFocusMinutes: (Int) -> Unit,
    onToggleRunning: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pomodoro = uiState.pomodoro
    val isDark = LocalIsDarkTheme.current
    val isFocus = pomodoro.mode == PomodoroMode.FOCUS
    val remainingFraction = (1f - pomodoro.progressFraction).coerceIn(0f, 1f)

    val animatedSweepFraction by animateFloatAsState(
        targetValue = remainingFraction,
        animationSpec = tween(durationMillis = 350),
        label = "pomodoro_ring_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "breathing_halo")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val haloAlpha = if (pomodoro.isRunning) pulseAlpha else 0.10f
    val ringTrackColor = if (isDark) Obsidian900 else Slate100
    val innerHairlineColor = if (isDark) Color.White.copy(alpha = 0.06f) else Slate200.copy(alpha = 0.8f)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Obsidian850 else PureWhite,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Switcher (Focus vs Break)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Obsidian900 else Slate100,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    PomodoroMode.entries.forEach { mode ->
                        val selected = pomodoro.mode == mode
                        val modeAccent = if (mode == PomodoroMode.FOCUS) {
                            if (isDark) Sky400 else Sky600
                        } else {
                            if (isDark) Emerald300 else Emerald600
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                if (isDark) Obsidian800 else PureWhite
                            } else {
                                Color.Transparent
                            },
                            shadowElevation = if (selected && !isDark) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSwitchMode(mode) }
                                .testTag("pomodoro_mode_${mode.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val durationLabel = if (mode == PomodoroMode.FOCUS) {
                                    "${pomodoro.focusDurationMinutes} mnt"
                                } else {
                                    "${pomodoro.breakDurationMinutes} mnt"
                                }
                                Text(
                                    text = "${mode.label} ($durationLabel)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) modeAccent else Slate400,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Duration Preset Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(if (isFocus) 1f else 0.4f)
            ) {
                Text(
                    text = "Durasi:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400
                )
                listOf(15, 25, 45).forEach { mins ->
                    val selected = pomodoro.focusDurationMinutes == mins
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) {
                            Emerald500.copy(alpha = 0.14f)
                        } else {
                            if (isDark) Obsidian900 else Slate100
                        },
                        border = BorderStroke(
                            1.dp,
                            if (selected) Emerald500.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = isFocus) { onSetFocusMinutes(mins) }
                            .testTag("pomodoro_preset_$mins")
                    ) {
                        Text(
                            text = "$mins mnt",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selected) {
                                if (isDark) Emerald300 else Emerald700
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Circular Radial Timer Display with Breathing Halo
            Box(
                modifier = Modifier
                    .size(252.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = haloAlpha), Color.Transparent)
                            ),
                            radius = size.minDimension * 0.56f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Outer track ring
                    drawArc(
                        color = ringTrackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Subtle inner border ring
                    drawCircle(
                        color = innerHairlineColor,
                        radius = (diameter / 2f) - strokeWidth,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Active countdown ring
                    if (animatedSweepFraction > 0f) {
                        drawArc(
                            color = accentColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedSweepFraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = if (isFocus) "SESI FOKUS" else "JEDA ISTIRAHAT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = pomodoro.formattedTime,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("pomodoro_countdown_text")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when {
                            pomodoro.isRunning && isFocus -> "Fokus berlangsung tenang..."
                            pomodoro.isRunning && !isFocus -> "Nikmati jeda istirahat..."
                            pomodoro.remainingSeconds < pomodoro.totalSeconds -> "Dijeda sementara"
                            else -> "Siap dimulai"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2:3 Split Reset & Start/Pause Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Obsidian900 else Slate100,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onResetTimer)
                        .testTag("pomodoro_reset_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset timer",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Button(
                    onClick = onToggleRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (isDark) Obsidian950 else PureWhite
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(3f)
                        .height(52.dp)
                        .testTag("pomodoro_start_pause_button")
                ) {
                    Icon(
                        imageVector = if (pomodoro.isRunning) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = if (pomodoro.isRunning) "Jeda timer" else "Mulai timer",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (pomodoro.isRunning) "Jeda" else "Mulai",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFocusTaskCard(
    selectedTask: TaskEntity?,
    onOpenTaskPicker: () -> Unit,
    onToggleSelectedTaskDone: (TaskEntity) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Obsidian850 else PureWhite,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pomodoro_selected_task_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TUGAS YANG SEDANG DIKERJAKAN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Slate400
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenTaskPicker)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("change_pomodoro_task_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SwapHoriz,
                        contentDescription = null,
                        tint = if (isDark) Sky400 else Sky600,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pilih Tugas",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Sky400 else Sky600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTask != null) {
                val qColors = selectedTask.quadrant.colors(isDark)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = qColors.container,
                    border = BorderStroke(1.dp, qColors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = qColors.dot.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, qColors.dot.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = selectedTask.quadrant.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = qColors.accent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = selectedTask.course,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedTask.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tenggat: ${DateTimeFormatters.formatDeadlineLabel(selectedTask.deadlineMillis)}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onToggleSelectedTaskDone(selectedTask) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("mark_pomodoro_task_done"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTask.isCompleted) "Tugas Sudah Selesai ✓" else "Tandai Tugas Ini Selesai",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Obsidian900.copy(alpha = 0.6f) else Slate100.copy(alpha = 0.6f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada tugas dipilih. Ketuk \"Pilih Tugas\" atau tombol \"Sesi Fokus\" pada kartu tugas di Matriks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CalmStudyGuidanceCard() {
    val isDark = LocalIsDarkTheme.current
    val gradient = if (isDark) {
        listOf(Obsidian850, Obsidian900)
    } else {
        listOf(Slate100, PureWhite)
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Amber500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Coffee,
                    contentDescription = null,
                    tint = if (isDark) Amber400 else Amber600,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Ritme Jeda Sadar (Mindful Break)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Setelah 1 sesi fokus selesai, jauhkan pandangan dari layar selama 5 menit, minum air putih, dan regangkan bahu sebelum lanjut.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskPickerSheet(
    tasks: List<TaskEntity>,
    selectedTaskId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = LocalIsDarkTheme.current
    val sortedTasks = remember(tasks) {
        tasks.sortedWith(compareBy<TaskEntity> { it.isCompleted }.thenBy { it.quadrant.ordinal })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Obsidian900 else PureWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pilih Tugas untuk Fokus",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tautkan satu tugas aktif pada timer Pomodoro",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (sortedTasks.isEmpty()) {
                Text(
                    text = "Belum ada tugas di matriks. Tambahkan tugas terlebih dahulu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sortedTasks.forEach { task ->
                    val qColors = task.quadrant.colors(isDark)
                    val isSelected = task.id == selectedTaskId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = qColors.container,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Sky500 else qColors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelect(task.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(qColors.dot)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${task.quadrant.code} • ${task.course}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = qColors.accent
                                )
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
