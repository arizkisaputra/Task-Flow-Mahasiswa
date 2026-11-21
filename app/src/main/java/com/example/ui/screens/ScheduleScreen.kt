package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Quadrant
import com.example.data.TaskEntity
import com.example.data.TimeBlockEntity
import com.example.ui.TaskFlowUiState
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Obsidian850
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Q2BorderDark
import com.example.ui.theme.Q2BorderLight
import com.example.ui.theme.Q2ContainerDark
import com.example.ui.theme.Q2ContainerLight
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import kotlinx.coroutines.launch

private data class DayPeriodGroup(
    val shortChip: String,
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val hours: IntRange
)

private val dayPeriods = listOf(
    DayPeriodGroup("Pagi (06–11)", "Pagi (06:00 – 11:00)", Icons.Outlined.WbTwilight, Amber500, 6..10),
    DayPeriodGroup("Siang (11–15)", "Siang (11:00 – 15:00)", Icons.Outlined.WbSunny, Sky500, 11..14),
    DayPeriodGroup("Sore (15–18)", "Sore (15:00 – 18:00)", Icons.Outlined.WbTwilight, Color(0xFFF97316), 15..17),
    DayPeriodGroup("Malam (18–22)", "Malam (18:00 – 22:00)", Icons.Outlined.NightsStay, Color(0xFF818CF8), 18..21)
)

@Composable
fun ScheduleScreen(
    uiState: TaskFlowUiState,
    onEditHourSlot: (Int) -> Unit,
    onToggleBlockCompleted: (TimeBlockEntity) -> Unit,
    onClearBlock: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val filledCount = uiState.timeBlocksByHour.size
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Compute LazyColumn item indices for each period header:
    // index 0 = schedule_header, index 1 = period_jump_chips, then each period banner + hours
    val periodStartIndices = remember {
        var idx = 2
        dayPeriods.map { period ->
            val start = idx
            idx += 1 + period.hours.count()
            start
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("schedule_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp)
    ) {
        item(key = "schedule_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Text(
                        text = "Jadwal Harian",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Petakan jam belajar, kuliah, dan istirahat dari pagi hingga malam",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Emerald500.copy(alpha = 0.11f),
                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.22f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            tint = if (isDark) Emerald300 else Emerald700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$filledCount/16 slot terisi",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Emerald300 else Emerald700
                        )
                    }
                }
            }
        }

        // Quick Jump Time-of-Day Pills
        item(key = "period_jump_chips") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                itemsIndexed(dayPeriods, key = { _, p -> p.shortChip }) { index, period ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Obsidian850 else PureWhite,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(periodStartIndices[index])
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = period.icon,
                                contentDescription = null,
                                tint = period.iconTint,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = period.shortChip,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        dayPeriods.forEachIndexed { periodIdx, period ->
            item(key = "period_${period.title}") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (periodIdx == 0) 0.dp else 18.dp, bottom = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Obsidian850 else Sky500.copy(alpha = 0.10f),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Sky500.copy(alpha = 0.22f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = period.icon,
                                contentDescription = null,
                                tint = period.iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = period.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            val hoursList = period.hours.toList()
            hoursList.forEachIndexed { hIdx, hour ->
                item(key = "hour_$hour") {
                    val block = uiState.timeBlocksByHour[hour]
                    val isLastInPeriod = hIdx == hoursList.lastIndex
                    TimeBlockSlotRow(
                        hourStart = hour,
                        isLastInPeriod = isLastInPeriod,
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
    isLastInPeriod: Boolean,
    block: TimeBlockEntity?,
    onTapSlot: () -> Unit,
    onToggleCompleted: () -> Unit,
    onClear: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val timeLabel = String.format("%02d:00", hourStart)
    val qColors = block?.quadrant?.colors(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .testTag("time_slot_$hourStart")
    ) {
        // Left Time Rail with Continuous Vertical Line
        Column(
            modifier = Modifier
                .width(54.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeLabel,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                fontWeight = if (block != null) FontWeight.Bold else FontWeight.Medium,
                color = if (block != null) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    if (isDark) Slate500 else Slate400
                },
                modifier = Modifier.padding(top = if (block != null) 14.dp else 12.dp)
            )
            if (!isLastInPeriod) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .weight(1f)
                        .background(
                            if (block != null) {
                                if (isDark) Color.White.copy(alpha = 0.09f) else Slate200
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.05f) else Slate200.copy(alpha = 0.7f)
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (block == null) {
            val dashColor = if (isDark) Color.White.copy(alpha = 0.10f) else Slate200
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .drawBehind {
                        drawRoundRect(
                            color = dashColor,
                            style = Stroke(
                                width = 1.1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    }
                    .clickable(onClick = onTapSlot)
                    .testTag("empty_slot_$hourStart"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Isi slot waktu $timeLabel",
                        tint = if (isDark) Slate500 else Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ketuk untuk jadwalkan tugas atau aktivitas",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Slate500 else Slate400
                    )
                }
            }
        } else {
            val containerColor = qColors?.container ?: if (isDark) Q2ContainerDark else Q2ContainerLight
            val borderColor = qColors?.border ?: if (isDark) Q2BorderDark else Q2BorderLight

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onTapSlot)
                    .testTag("filled_slot_$hourStart")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
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
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (block.isCompleted) Emerald500 else Color.Transparent)
                                .then(
                                    if (!block.isCompleted) {
                                        Modifier.drawBehind {
                                            drawCircle(
                                                color = if (isDark) Slate500 else Slate400.copy(alpha = 0.65f),
                                                style = Stroke(width = 2.dp.toPx())
                                            )
                                        }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (block.isCompleted) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Blok waktu selesai",
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = block.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (block.isCompleted) {
                                if (isDark) Slate500 else Slate400
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            },
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
                            tint = Slate400,
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
    val isDark = LocalIsDarkTheme.current
    var customTitle by remember(hourStart) { mutableStateOf(existingBlock?.title ?: "") }
    var customNote by remember(hourStart) { mutableStateOf(existingBlock?.subtitle ?: "") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Sky500,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedContainerColor = if (isDark) Obsidian850 else Slate100.copy(alpha = 0.55f),
        unfocusedContainerColor = if (isDark) Obsidian850 else Slate100.copy(alpha = 0.55f)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Obsidian900 else PureWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Jadwalkan Jam ${String.format("%02d:00", hourStart)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (tasks.isNotEmpty()) {
                Text(
                    text = "Pilih dari Tugas Matriks",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isDark) Sky400 else Sky600
                )
                tasks.filter { !it.isCompleted }.take(5).forEach { task ->
                    val qColors = task.quadrant.colors(isDark)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = qColors.container,
                        border = BorderStroke(1.dp, qColors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
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
                                    .background(qColors.dot)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${task.quadrant.shortCode} • ${task.course}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = qColors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                text = "+ Pasang",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Sky400 else Sky600
                            )
                        }
                    }
                }
            }

            Text(
                text = "Atau Tulis Aktivitas / Rutinitas Baru",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = customTitle,
                onValueChange = { customTitle = it },
                label = { Text("Nama Aktivitas") },
                placeholder = { Text("Contoh: Istirahat & Shalat Dzuhur") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = customNote,
                onValueChange = { customNote = it },
                label = { Text("Catatan Singkat (Opsional)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existingBlock != null) {
                    OutlinedButton(
                        onClick = { onClearSlot(hourStart) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Kosongkan")
                    }
                } else {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Emerald400 else Emerald600,
                        contentColor = if (isDark) Obsidian950 else PureWhite
                    ),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                ) {
                    Text("Simpan Jadwal", fontWeight = FontWeight.Bold)
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
    val isDark = LocalIsDarkTheme.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Obsidian900 else PureWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pilih Jam Pelaksanaan",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Menjadwalkan: ${task.title}",
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
                            shape = RoundedCornerShape(14.dp),
                            color = if (isOccupied) {
                                if (isDark) Obsidian850 else Slate100
                            } else {
                                Emerald500.copy(alpha = 0.12f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isOccupied) {
                                    MaterialTheme.colorScheme.outlineVariant
                                } else {
                                    Emerald500.copy(alpha = 0.30f)
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onAssignHour(hour) }
                                .testTag("quick_assign_hour_$hour")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = String.format("%02d:00", hour),
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOccupied) {
                                        Slate400
                                    } else {
                                        if (isDark) Emerald300 else Emerald700
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
