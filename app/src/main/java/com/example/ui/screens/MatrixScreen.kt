package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Quadrant
import com.example.data.TaskEntity
import com.example.ui.TaskFilter
import com.example.ui.TaskFlowUiState
import com.example.ui.components.DailyEvaluationCard
import com.example.ui.components.MotivationHeaderCard
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian850
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose400
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.util.DateTimeFormatters

@Composable
fun MatrixScreen(
    uiState: TaskFlowUiState,
    isExpandedWidth: Boolean,
    onSelectFilter: (TaskFilter) -> Unit,
    onSelectQuadrantFilter: (Quadrant?) -> Unit,
    onOpenPrototypeInfo: () -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onMoveToFocus: (TaskEntity) -> Unit,
    onQuickSchedule: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val visibleQuadrants = uiState.selectedQuadrantFilter?.let { listOf(it) } ?: Quadrant.entries

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("matrix_screen_list"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bento Row: Mindful Reflection Carousel + Elevated Daily Achievement Card
        if (isExpandedWidth) {
            item(key = "top_bento_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MotivationHeaderCard(
                        onOpenPrototypeInfo = onOpenPrototypeInfo,
                        modifier = Modifier
                            .weight(7f)
                            .fillMaxHeight()
                    )
                    DailyEvaluationCard(
                        evaluation = uiState.evaluation,
                        allTasks = uiState.allTasks,
                        focusSessionsToday = uiState.pomodoro.focusSessionsToday,
                        modifier = Modifier
                            .weight(5f)
                            .fillMaxHeight()
                    )
                }
            }
        } else {
            item(key = "motivation_header") {
                MotivationHeaderCard(onOpenPrototypeInfo = onOpenPrototypeInfo)
            }
            item(key = "daily_evaluation_elevated") {
                DailyEvaluationCard(
                    evaluation = uiState.evaluation,
                    allTasks = uiState.allTasks,
                    focusSessionsToday = uiState.pomodoro.focusSessionsToday
                )
            }
        }

        // Section Header & Unified Status Filter Bar
        item(key = "filter_bar") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Matriks Prioritas Eisenhower",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pilah tugas kuliah & organisasi berdasarkan tingkat kepentingan dan urgensi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Segmented Status Filter Pill Container
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Obsidian850 else Slate200.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TaskFilter.entries.forEach { filter ->
                            val selected = uiState.taskFilter == filter
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) {
                                    if (isDark) Sky400 else Sky600
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectFilter(filter) }
                                    .testTag("filter_chip_${filter.name.lowercase()}")
                            ) {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (selected) {
                                        if (isDark) Obsidian950 else PureWhite
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Quadrant Focus Bar (Zoom into a single quadrant or view all 4)
        item(key = "quadrant_focus_bar") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item(key = "qfilter_all") {
                    val selected = uiState.selectedQuadrantFilter == null
                    QuadrantZoomChip(
                        label = "Semua Kuadran (4)",
                        dotColor = null,
                        selected = selected,
                        onClick = { onSelectQuadrantFilter(null) },
                        modifier = Modifier.testTag("quadrant_filter_all")
                    )
                }
                items(Quadrant.entries, key = { it.name }) { quadrant ->
                    val selected = uiState.selectedQuadrantFilter == quadrant
                    val qColor = quadrant.colors(isDark)
                    QuadrantZoomChip(
                        label = "${quadrant.shortCode} • ${quadrant.criteriaLabel}",
                        dotColor = qColor.dot,
                        selected = selected,
                        onClick = {
                            onSelectQuadrantFilter(if (selected) null else quadrant)
                        },
                        modifier = Modifier.testTag("quadrant_filter_${quadrant.shortCode.lowercase()}")
                    )
                }
            }
        }

        // Responsive 2x2 Bento Matrix Grid (or 1-column when focused on a single quadrant or mobile)
        if (isExpandedWidth && visibleQuadrants.size > 1) {
            item(key = "quadrant_row_1") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            items(visibleQuadrants, key = { "quadrant_${it.name}" }) { quadrant ->
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
}

@Composable
private fun QuadrantZoomChip(
    label: String,
    dotColor: Color?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            Sky500.copy(alpha = 0.13f)
        } else {
            if (isDark) Obsidian850 else PureWhite
        },
        border = BorderStroke(
            1.dp,
            if (selected) Sky500.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) {
                    if (isDark) Sky400 else Sky600
                } else {
                    if (isDark) Slate300 else Slate600
                },
                maxLines = 1
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
    val isDark = LocalIsDarkTheme.current
    val colors = quadrant.colors(isDark)

    Surface(
        modifier = modifier.testTag("quadrant_card_${quadrant.name.lowercase()}"),
        shape = RoundedCornerShape(24.dp),
        color = colors.container,
        border = BorderStroke(1.dp, colors.border),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.dot)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${quadrant.code} • ${quadrant.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.accent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${quadrant.criteriaLabel} — ${quadrant.actionHint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = CircleShape,
                    color = colors.dot.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.dot.copy(alpha = 0.25f)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${tasks.size}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = colors.accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tasks.isEmpty()) {
                val dashColor = if (isDark) Color.White.copy(alpha = 0.12f) else Slate300
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                color = dashColor,
                                style = Stroke(
                                    width = 1.2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                ),
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )
                        }
                        .padding(vertical = 22.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada tugas pada filter ini",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Slate500 else Slate400
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tasks.forEach { task ->
                        QuadrantTaskCard(
                            task = task,
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
    isActiveFocus: Boolean,
    onToggleComplete: () -> Unit,
    onMoveToFocus: () -> Unit,
    onQuickSchedule: () -> Unit,
    onDeleteTask: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val cardColor by animateColorAsState(
        targetValue = if (isDark) Obsidian900.copy(alpha = 0.92f) else PureWhite.copy(alpha = 0.96f),
        label = "task_card_bg"
    )
    val isOverdue = DateTimeFormatters.isDeadlineOverdue(task.deadlineMillis, task.isCompleted)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(
            width = if (isActiveFocus && !task.isCompleted) 1.5.dp else 1.dp,
            color = if (isActiveFocus && !task.isCompleted) {
                Sky500.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Accessible 48dp touch-target circular check button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleComplete)
                        .testTag("toggle_task_complete_${task.id}"),
                    contentAlignment = Alignment.TopStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp, start = 2.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (task.isCompleted) Emerald500 else Color.Transparent)
                            .then(
                                if (!task.isCompleted) {
                                    Modifier.drawBehind {
                                        drawCircle(
                                            color = if (isDark) Slate500 else Slate300,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Tugas selesai",
                                tint = PureWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 1.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) {
                            if (isDark) Slate500 else Slate400
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Obsidian800 else Slate100,
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.06f) else Slate200
                            )
                        ) {
                            Text(
                                text = task.course,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Slate300 else Slate600,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Tenggat waktu",
                                tint = if (isOverdue) Rose400 else Slate400,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DateTimeFormatters.formatDeadlineLabel(task.deadlineMillis),
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = if (isOverdue) Rose400 else {
                                    if (isDark) Slate400 else Slate500
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
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = if (isDark) Color.White.copy(alpha = 0.05f) else Slate100
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActiveFocus) {
                        if (isDark) Sky400 else Sky600
                    } else {
                        Sky500.copy(alpha = 0.11f)
                    },
                    border = if (isActiveFocus) null else BorderStroke(1.dp, Sky500.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                                if (isDark) Obsidian950 else PureWhite
                            } else {
                                if (isDark) Sky400 else Sky600
                            },
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isActiveFocus) "Sedang Dipilih" else "Sesi Fokus",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isActiveFocus) {
                                if (isDark) Obsidian950 else PureWhite
                            } else {
                                if (isDark) Sky400 else Sky600
                            },
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald500.copy(alpha = 0.11f),
                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                            tint = if (isDark) Emerald300 else Emerald700,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jadwalkan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Emerald300 else Emerald700,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
