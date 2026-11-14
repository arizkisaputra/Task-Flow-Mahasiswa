package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Quadrant
import com.example.util.DateTimeFormatters
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    onSaveTask: (
        title: String,
        course: String,
        deadlineMillis: Long,
        isImportant: Boolean,
        isUrgent: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val darkTheme = isSystemInDarkTheme()

    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(true) }
    var isUrgent by remember { mutableStateOf(true) }

    val defaultDeadline = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    var deadlineMillis by remember { mutableLongStateOf(defaultDeadline) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val predictedQuadrant = Quadrant.fromImportanceAndUrgency(isImportant, isUrgent)
    val qColors = predictedQuadrant.colors(darkTheme)

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
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Tambah Tugas Baru",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sistem akan otomatis mengklasifikasikan tugas ke kuadran Matriks Eisenhower.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nama Tugas") },
                placeholder = { Text("Contoh: Resume Jurnal Sistem Informasi") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_title")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                label = { Text("Mata Kuliah / Kategori") },
                placeholder = { Text("Contoh: Rekayasa Perangkat Lunak") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_course")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tenggat Waktu (Deadline)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hari Ini" to 0, "Besok" to 1, "+3 Hari" to 3).forEach { (label, daysOffset) ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = deadlineMillis
                                val hour = get(Calendar.HOUR_OF_DAY)
                                val min = get(Calendar.MINUTE)
                                timeInMillis = System.currentTimeMillis()
                                add(Calendar.DAY_OF_YEAR, daysOffset)
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, min)
                                set(Calendar.SECOND, 0)
                            }
                            deadlineMillis = cal.timeInMillis
                        },
                        label = { Text(label) },
                        modifier = Modifier.testTag("quick_deadline_$daysOffset")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .testTag("pick_date_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Pilih tanggal deadline",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateTimeFormatters.formatFullDate(deadlineMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showTimePicker = true }
                        .testTag("pick_time_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Pilih jam deadline",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateTimeFormatters.formatTime(deadlineMillis),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tingkat Kepentingan",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClassificationToggleButton(
                    label = "Penting",
                    selected = isImportant,
                    onClick = { isImportant = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_important_true")
                )
                ClassificationToggleButton(
                    label = "Tidak Penting",
                    selected = !isImportant,
                    onClick = { isImportant = false },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_important_false")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tingkat Urgensi",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClassificationToggleButton(
                    label = "Mendesak",
                    selected = isUrgent,
                    onClick = { isUrgent = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_urgent_true")
                )
                ClassificationToggleButton(
                    label = "Tidak Mendesak",
                    selected = !isUrgent,
                    onClick = { isUrgent = false },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_urgent_false")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Automatic Quadrant Classification Preview
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = qColors.container,
                border = BorderStroke(1.dp, qColors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("predicted_quadrant_preview")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(qColors.accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = qColors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Klasifikasi Otomatis: ${predictedQuadrant.code} (${predictedQuadrant.title})",
                            style = MaterialTheme.typography.labelLarge,
                            color = qColors.accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${predictedQuadrant.criteriaLabel} • ${predictedQuadrant.actionHint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_add_task")
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        onSaveTask(title, course, deadlineMillis, isImportant, isUrgent)
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(48.dp)
                        .testTag("submit_add_task")
                ) {
                    Text("Simpan ke Matriks")
                }
            }
        }
    }

    if (showDatePicker) {
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            val local = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = utcCalendar.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedUtc = datePickerState.selectedDateMillis
                    if (selectedUtc != null) {
                        val utcPicked = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = selectedUtc
                        }
                        val localMerged = Calendar.getInstance().apply {
                            timeInMillis = deadlineMillis
                            set(Calendar.YEAR, utcPicked.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcPicked.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcPicked.get(Calendar.DAY_OF_MONTH))
                        }
                        deadlineMillis = localMerged.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Pilih Jam Tenggat") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val updated = Calendar.getInstance().apply {
                        timeInMillis = deadlineMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    deadlineMillis = updated.timeInMillis
                    showTimePicker = false
                }) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ClassificationToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
