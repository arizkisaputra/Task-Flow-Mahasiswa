package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Quadrant
import com.example.data.TaskEntity
import com.example.ui.TaskFilter
import com.example.ui.TaskFlowUiState
import com.example.ui.components.DailyEvaluationCard
import com.example.ui.components.MotivationHeaderCard
import com.example.ui.theme.LocalIsDarkTheme
import com.example.util.DateTimeFormatters

@Composable
fun MatrixScreen(
    uiState: TaskFlowUiState,
    isExpandedWidth: Boolean,
    onSelectFilter: (TaskFilter) -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenPrototypeInfo: () -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onMoveToFocus: (TaskEntity) -> Unit,
    onQuickSchedule: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("matrix_screen_list"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "motivation_header") {
            MotivationHeaderCard(onOpenPrototypeInfo = onOpenPrototypeInfo)
        }

        item(key = "filter_bar") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Matriks Prioritas Eisenhower",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Pilah tugas berdasarkan kepentingan & urgensi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "filter_chips") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.taskFilter == filter,
                        onClick = { onSelectFilter(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onOpenAddTask,
                    modifier = Modifier.testTag("inline_add_task_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah")
                }
            }
        }

        if (isExpandedWidth) {
            item(key = "quadrant_row_1") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    QuadrantCard(
                        quadrant = Quadrant.Q1_DO_FIRST,
                        tasks = uiState.tasksByQuadrant[Quadrant.Q1_DO_FIRST].orEmpty(),
                        activeFocusTaskId = uiState.pomodoro.selectedTaskId,
                        onToggleComplete = onToggleComplete,
                        onMoveToFocus = onMoveToFocus,
                        onQuickSchedule = onQuickSchedule,
                        onDeleteTask = onDeleteTask,
                        modifier = Modifier.weight(1f)
                    )
                    QuadrantCard(
                        quadrant = Quadrant.Q2_SCHEDULE,
                        tasks = uiState.tasksByQuadrant[Quadrant.Q2_SCHEDULE].orEmpty(),
                        activeFocusTaskId = uiState.pomodoro.selectedTaskId,
                        onToggleComplete = onToggleComplete,
                        onMoveToFocus = onMoveToFocus,
                        onQuickSchedule = onQuickSchedule,
                        onDeleteTask = onDeleteTask,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item(key = "quadrant_row_2") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    QuadrantCard(
                        quadrant = Quadrant.Q3_DELEGATE,
                        tasks = uiState.tasksByQuadrant[Quadrant.Q3_DELEGATE].orEmpty(),
                        activeFocusTaskId = uiState.pomodoro.selectedTaskId,
                        onToggleComplete = onToggleComplete,
                        onMoveToFocus = onMoveToFocus,
                        onQuickSchedule = onQuickSchedule,
                        onDeleteTask = onDeleteTask,
                        modifier = Modifier.weight(1f)
                    )
                    QuadrantCard(
                        quadrant = Quadrant.Q4_ELIMINATE,
                        tasks = uiState.tasksByQuadrant[Quadrant.Q4_ELIMINATE].orEmpty(),
                        activeFocusTaskId = uiState.pomodoro.selectedTaskId,
                        onToggleComplete = onToggleComplete,
                        onMoveToFocus = onMoveToFocus,
                        onQuickSchedule = onQuickSchedule,
                        onDeleteTask = onDeleteTask,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Quadrant.entries.forEach { quadrant ->
                item(key = "quadrant_${quadrant.name}") {
                    QuadrantCard(
                        quadrant = quadrant,
                        tasks = uiState.tasksByQuadrant[quadrant].orEmpty(),
                        activeFocusTaskId = uiState.pomodoro.selectedTaskId,
                        onToggleComplete = onToggleComplete,
                        onMoveToFocus = onMoveToFocus,
                        onQuickSchedule = onQuickSchedule,
                        onDeleteTask = onDeleteTask,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item(key = "daily_evaluation_footer") {
            DailyEvaluationCard(
                evaluation = uiState.evaluation,
                allTasks = uiState.allTasks,
                focusSessionsToday = uiState.pomodoro.focusSessionsToday
            )
        }
    }
}

@Composable
private fun QuadrantCard(
    quadrant: Quadrant,
    tasks: List<TaskEntity>,
    activeFocusTaskId: Long?,
    onToggleComplete: (TaskEntity) -> Unit,
    onMoveToFocus: (TaskEntity) -> Unit,
    onQuickSchedule: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalIsDarkTheme.current
    val colors = quadrant.colors(darkTheme)

    Surface(
        modifier = modifier.testTag("quadrant_card_${quadrant.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        color = colors.container,
        border = BorderStroke(1.dp, colors.border),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${quadrant.code} • ${quadrant.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${quadrant.criteriaLabel} — ${quadrant.actionHint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = colors.accent.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "${tasks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (tasks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircleOutline,
                            contentDescription = null,
                            tint = colors.accent.copy(alpha = 0.65f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Belum ada tugas di kuadran ini. Tetap tenang & teratur.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        QuadrantTaskCard(
                            task = task,
                            accentColor = colors.accent,
                            isActiveFocus = task.id == activeFocusTaskId,
                            onToggleComplete = { onToggleComplete(task) },
                            onMoveToFocus = { onMoveToFocus(task) },
                            onQuickSchedule = { onQuickSchedule(task) },
                            onDeleteTask = { onDeleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuadrantTaskCard(
    task: TaskEntity,
    accentColor: androidx.compose.ui.graphics.Color,
    isActiveFocus: Boolean,
    onToggleComplete: () -> Unit,
    onMoveToFocus: () -> Unit,
    onQuickSchedule: () -> Unit,
    onDeleteTask: () -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "task_card_bg"
    )
    val isOverdue = DateTimeFormatters.isDeadlineOverdue(task.deadlineMillis, task.isCompleted)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        border = BorderStroke(
            width = if (isActiveFocus && !task.isCompleted) 1.5.dp else 1.dp,
            color = if (isActiveFocus && !task.isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Accessible >= 48dp touch target checkbox
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleComplete)
                        .testTag("toggle_task_complete_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (task.isCompleted) accentColor else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Tugas selesai",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = task.course,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Tenggat waktu",
                                tint = if (isOverdue) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = DateTimeFormatters.formatDeadlineLabel(task.deadlineMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteTask,
                    modifier = Modifier.testTag("delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus tugas",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActiveFocus) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onMoveToFocus)
                        .testTag("focus_task_${task.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = "Mulai sesi fokus Pomodoro untuk tugas ini",
                            tint = if (isActiveFocus) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isActiveFocus) "Sedang Dipilih" else "Sesi Fokus",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActiveFocus) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onQuickSchedule)
                        .testTag("schedule_task_${task.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Tambahkan ke Jadwal Harian",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jadwal",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
