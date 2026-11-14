package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Quadrant
import com.example.data.TaskEntity
import com.example.data.TimeBlockEntity
import com.example.ui.TaskFlowUiState
import com.example.ui.theme.JetBrainsMonoFamily

private data class DayPeriodGroup(
    val title: String,
    val icon: ImageVector,
    val hours: IntRange
)

private val dayPeriods = listOf(
    DayPeriodGroup("Pagi (06:00 - 11:00)", Icons.Outlined.WbTwilight, 6..10),
    DayPeriodGroup("Siang (11:00 - 15:00)", Icons.Outlined.WbSunny, 11..14),
    DayPeriodGroup("Sore (15:00 - 18:00)", Icons.Outlined.WbTwilight, 15..17),
    DayPeriodGroup("Malam (18:00 - 22:00)", Icons.Outlined.NightsStay, 18..21)
)

@Composable
fun ScheduleScreen(
    uiState: TaskFlowUiState,
    onEditHourSlot: (Int) -> Unit,
    onToggleBlockCompleted: (TimeBlockEntity) -> Unit,
    onClearBlock: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val filledCount = uiState.timeBlocksByHour.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("schedule_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "schedule_header") {
            Column {
                Text(
                    text = "Jadwal Harian",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Petakan jam belajar, kuliah, dan istirahat dari pagi hingga malam ($filledCount/16 slot terisi).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        dayPeriods.forEach { period ->
            item(key = "period_${period.title}") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = period.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = period.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            period.hours.forEach { hour ->
                item(key = "hour_$hour") {
                    val block = uiState.timeBlocksByHour[hour]
                    TimeBlockSlotRow(
                        hourStart = hour,
                        block = block,
                        onTapSlot = { onEditHourSlot(hour) },
                        onToggleCompleted = { block?.let(onToggleBlockCompleted) },
                        onClear = { onClearBlock(hour) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeBlockSlotRow(
    hourStart: Int,
    block: TimeBlockEntity?,
    onTapSlot: () -> Unit,
    onToggleCompleted: () -> Unit,
    onClear: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val timeLabel = String.format("%02d:00", hourStart)
    val qColors = block?.quadrant?.colors(darkTheme)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("time_slot_$hourStart"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeLabel,
            fontFamily = JetBrainsMonoFamily,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp)
        )

        if (block == null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onTapSlot)
                    .testTag("empty_slot_$hourStart")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Isi slot waktu $timeLabel",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ketuk untuk jadwalkan tugas atau aktivitas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val containerColor = qColors?.container ?: MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            val borderColor = qColors?.border ?: MaterialTheme.colorScheme.outlineVariant
            val accentColor = qColors?.accent ?: MaterialTheme.colorScheme.secondary

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onTapSlot)
                    .testTag("filled_slot_$hourStart")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onToggleCompleted)
                            .testTag("toggle_block_$hourStart"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    if (block.isCompleted) accentColor else MaterialTheme.colorScheme.surface
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (block.isCompleted) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Blok waktu selesai",
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = block.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (block.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (block.subtitle.isNotBlank()) {
                            Text(
                                text = block.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.testTag("clear_block_$hourStart")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Kosongkan slot $timeLabel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimeBlockSheet(
    hourStart: Int,
    existingBlock: TimeBlockEntity?,
    tasks: List<TaskEntity>,
    onAssignTask: (Int, TaskEntity) -> Unit,
    onSaveCustom: (Int, String, String, Quadrant?) -> Unit,
    onClearSlot: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val darkTheme = isSystemInDarkTheme()
    var customTitle by remember(hourStart) { mutableStateOf(existingBlock?.title ?: "") }
    var customNote by remember(hourStart) { mutableStateOf(existingBlock?.subtitle ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Atur Slot ${String.format("%02d:00 - %02d:00", hourStart, hourStart + 1)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (tasks.isNotEmpty()) {
                Text(
                    text = "Pilih dari Tugas Matriks",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                tasks.filter { !it.isCompleted }.take(5).forEach { task ->
                    val qColors = task.quadrant.colors(darkTheme)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = qColors.container,
                        border = BorderStroke(1.dp, qColors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAssignTask(hourStart, task) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(qColors.accent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
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

            Text(
                text = "Atau Aktivitas Khusus (Kuliah, Ibadah, Istirahat)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = customTitle,
                onValueChange = { customTitle = it },
                label = { Text("Nama Aktivitas") },
                placeholder = { Text("Contoh: Kuliah Struktur Data / Istirahat") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = customNote,
                onValueChange = { customNote = it },
                label = { Text("Catatan Singkat (Opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existingBlock != null) {
                    OutlinedButton(
                        onClick = { onClearSlot(hourStart) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Kosongkan")
                    }
                } else {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Batal")
                    }
                }

                Button(
                    onClick = {
                        onSaveCustom(hourStart, customTitle, customNote, Quadrant.Q2_SCHEDULE)
                    },
                    enabled = customTitle.isNotBlank(),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                ) {
                    Text("Simpan Jadwal")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAssignTaskToHourSheet(
    task: TaskEntity,
    occupiedHours: Set<Int>,
    onAssignHour: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Atur Jadwal Harian",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Pilih jam pengerjaan untuk: ${task.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            (6..21).chunked(4).forEach { rowHours ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowHours.forEach { hour ->
                        val isOccupied = hour in occupiedHours
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOccupied) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isOccupied) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onAssignHour(hour) }
                                .testTag("quick_assign_hour_$hour")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = String.format("%02d:00", hour),
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isOccupied) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
