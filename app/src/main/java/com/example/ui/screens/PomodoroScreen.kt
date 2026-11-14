package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TaskEntity
import com.example.ui.PomodoroMode
import com.example.ui.TaskFlowUiState
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
    onDismissCompletionModal: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val pomodoro = uiState.pomodoro
    val selectedTask = uiState.allTasks.firstOrNull { it.id == pomodoro.selectedTaskId }
    var showTaskSelector by remember { mutableStateOf(false) }

    val darkTheme = isSystemInDarkTheme()
    val ringColor = if (pomodoro.mode == PomodoroMode.FOCUS) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val ringTrackColor = MaterialTheme.colorScheme.surfaceVariant
    val animatedProgress by animateFloatAsState(
        targetValue = pomodoro.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "pomodoro_ring_progress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("pomodoro_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "pomodoro_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Timer Fokus Pomodoro",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Konsisten tanpa cemas • ${pomodoro.focusSessionsToday} sesi fokus hari ini",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            "Matikan suara lonceng lembut"
                        } else {
                            "Aktifkan suara lonceng lembut"
                        },
                        tint = if (pomodoro.soundEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        item(key = "mode_selector") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PomodoroMode.entries.forEach { mode ->
                        val selected = pomodoro.mode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                            },
                            shadowElevation = if (selected) 2.dp else 0.dp,
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
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (pomodoro.mode == PomodoroMode.FOCUS) {
            item(key = "focus_duration_presets") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Durasi:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf(15, 25, 45).forEach { mins ->
                        FilterChip(
                            selected = pomodoro.focusDurationMinutes == mins,
                            onClick = { onSetFocusMinutes(mins) },
                            label = { Text("$mins menit") },
                            modifier = Modifier.testTag("pomodoro_preset_$mins")
                        )
                    }
                }
            }
        }

        item(key = "timer_ring") {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 26.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(
                                (size.width - diameter) / 2f,
                                (size.height - diameter) / 2f
                            )
                            val arcSize = Size(diameter, diameter)

                            drawArc(
                                color = ringTrackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress.coerceAtLeast(0.01f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = pomodoro.mode.label.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = ringColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pomodoro.formattedTime,
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("pomodoro_countdown_text")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (pomodoro.isRunning) "Tetap tenang & fokus..." else "Siap dimulai",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetTimer,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("pomodoro_reset_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Reset timer",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }

                        Button(
                            onClick = onToggleRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = ringColor),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("pomodoro_start_pause_button"),
                            shape = RoundedCornerShape(14.dp)
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
                            Text(if (pomodoro.isRunning) "Jeda" else "Mulai")
                        }
                    }
                }
            }
        }

        item(key = "selected_task_card") {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pomodoro_selected_task_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tugas yang Sedang Dikerjakan",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { showTaskSelector = true },
                            modifier = Modifier.testTag("change_pomodoro_task_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pilih Tugas")
                        }
                    }

                    if (selectedTask != null) {
                        val qColors = selectedTask.quadrant.colors(darkTheme)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
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
                                        shape = RoundedCornerShape(6.dp),
                                        color = qColors.accent.copy(alpha = 0.16f)
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = selectedTask.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tenggat: ${DateTimeFormatters.formatDeadlineLabel(selectedTask.deadlineMillis)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { onToggleSelectedTaskDone(selectedTask) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("mark_pomodoro_task_done"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (selectedTask.isCompleted) "Tugas Sudah Selesai ✓" else "Tandai Tugas Ini Selesai"
                            )
                        }
                    } else {
                        Text(
                            text = "Belum ada tugas dipilih. Ketuk \"Pilih Tugas\" atau tombol \"Sesi Fokus\" pada kartu tugas di Matriks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            title = {
                Text(
                    text = if (finishedMode == PomodoroMode.FOCUS) {
                        "Alhamdulillah, Sesi Fokus Selesai!"
                    } else {
                        "Waktu Istirahat Selesai"
                    }
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
                Button(onClick = { onDismissCompletionModal(true) }) {
                    Text(
                        if (finishedMode == PomodoroMode.FOCUS) "Mulai Istirahat 5m" else "Lanjut Fokus 25m"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskPickerSheet(
    tasks: List<TaskEntity>,
    selectedTaskId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val darkTheme = isSystemInDarkTheme()
    val sortedTasks = remember(tasks) {
        tasks.sortedWith(compareBy<TaskEntity> { it.isCompleted }.thenBy { it.quadrant.ordinal })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pilih Tugas untuk Sesi Fokus",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (sortedTasks.isEmpty()) {
                Text(
                    text = "Belum ada tugas di matriks. Tambahkan tugas terlebih dahulu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sortedTasks.forEach { task ->
                    val qColors = task.quadrant.colors(darkTheme)
                    val isSelected = task.id == selectedTaskId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else qColors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(task.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(qColors.accent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${task.quadrant.code} • ${task.course}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
