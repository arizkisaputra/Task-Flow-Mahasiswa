package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Quadrant
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
import com.example.ui.theme.Slate600
import com.example.util.DateTimeFormatters
import java.util.Calendar
import java.util.TimeZone

private val quickCourseSuggestions = listOf(
    "Algoritma & Pemrograman" to "Algoritma",
    "Pendidikan Agama Islam" to "PAI",
    "Organisasi Mahasiswa" to "Organisasi",
    "Pengembangan Diri" to "Pengembangan Diri"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val isDark = LocalIsDarkTheme.current

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
    val qColors = predictedQuadrant.colors(isDark)

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
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Tambah Tugas Baru",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sistem otomatis mengklasifikasikan ke kuadran Matriks Eisenhower.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Nama Tugas",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Contoh: Resume Bab 4 Sistem Operasi") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_title")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Mata Kuliah / Kategori",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                placeholder = { Text("Contoh: Algoritma & Pemrograman") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_course")
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickCourseSuggestions.forEach { (fullName, shortLabel) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (course == fullName) Sky500.copy(alpha = 0.16f) else {
                            if (isDark) Obsidian800 else Slate100
                        },
                        border = BorderStroke(
                            1.dp,
                            if (course == fullName) Sky500.copy(alpha = 0.45f) else Color.Transparent
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { course = fullName }
                    ) {
                        Text(
                            text = shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (course == fullName) {
                                if (isDark) Sky400 else Sky600
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tenggat Waktu (Deadline)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hari Ini" to 0, "Besok" to 1, "+3 Hari" to 3).forEach { (label, daysOffset) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Obsidian850 else Slate100,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
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
                            }
                            .testTag("quick_deadline_$daysOffset")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Obsidian850 else Slate100.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true }
                        .testTag("pick_date_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Pilih tanggal deadline",
                            tint = if (isDark) Sky400 else Sky600,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateTimeFormatters.formatFullDate(deadlineMillis),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Obsidian850 else Slate100.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
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
                            tint = if (isDark) Sky400 else Sky600,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateTimeFormatters.formatTime(deadlineMillis),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tingkat Kepentingan",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
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
                shape = RoundedCornerShape(16.dp),
                color = qColors.container,
                border = BorderStroke(1.dp, qColors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("predicted_quadrant_preview")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(qColors.dot.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = qColors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Klasifikasi Otomatis: ${predictedQuadrant.code} (${predictedQuadrant.title})",
                            style = MaterialTheme.typography.labelLarge,
                            color = qColors.accent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("cancel_add_task")
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onSaveTask(title, course, deadlineMillis, isImportant, isUrgent)
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Sky400 else Sky600,
                        contentColor = if (isDark) Obsidian950 else PureWhite
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("submit_add_task")
                ) {
                    Text("Simpan ke Matriks", fontWeight = FontWeight.Bold)
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
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            Sky500.copy(alpha = 0.12f)
        } else {
            if (isDark) Obsidian850 else Slate100.copy(alpha = 0.7f)
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Sky500 else MaterialTheme.colorScheme.outlineVariant
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
                    tint = if (isDark) Sky400 else Sky600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) {
                    if (isDark) Sky400 else Sky600
                } else {
                    if (isDark) Slate400 else Slate600
                }
            )
        }
    }
}
