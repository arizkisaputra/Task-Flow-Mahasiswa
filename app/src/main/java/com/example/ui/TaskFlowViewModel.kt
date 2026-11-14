package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Quadrant
import com.example.data.TaskEntity
import com.example.data.TaskFlowRepository
import com.example.data.ThemeMode
import com.example.data.TimeBlockEntity
import com.example.util.DateTimeFormatters
import com.example.util.DeadlineScheduler
import com.example.util.GentleChimePlayer
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MainTab(val label: String) {
    MATRIX("Matriks"),
    POMODORO("Fokus"),
    SCHEDULE("Jadwal")
}

enum class TaskFilter(val label: String) {
    ALL("Semua"),
    ACTIVE("Aktif"),
    COMPLETED("Selesai")
}

enum class PomodoroMode(val label: String, val defaultMinutes: Int) {
    FOCUS("Sesi Fokus", 25),
    BREAK("Istirahat", 5)
}

data class DailyEvaluationSummary(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val progressFraction: Float = 0f,
    val percentage: Int = 0,
    val encouragementMessage: String = "Mari mulai satu langkah kecil dengan tenang."
)

data class PomodoroState(
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val selectedTaskId: Long? = null,
    val focusSessionsToday: Int = 0,
    val soundEnabled: Boolean = true,
    val completionModalMode: PomodoroMode? = null
) {
    val progressFraction: Float
        get() = if (totalSeconds > 0) {
            1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
        } else 0f

    val formattedTime: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}

data class TaskFlowUiState(
    val currentTab: MainTab = MainTab.MATRIX,
    val taskFilter: TaskFilter = TaskFilter.ALL,
    val allTasks: List<TaskEntity> = emptyList(),
    val tasksByQuadrant: Map<Quadrant, List<TaskEntity>> = Quadrant.entries.associateWith { emptyList() },
    val timeBlocksByHour: Map<Int, TimeBlockEntity> = emptyMap(),
    val evaluation: DailyEvaluationSummary = DailyEvaluationSummary(),
    val pomodoro: PomodoroState = PomodoroState(),
    val showAddTaskSheet: Boolean = false,
    val showPrototypeInfoSheet: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val scheduleTargetTask: TaskEntity? = null,
    val editingHourSlot: Int? = null,
    val hasRequestedNotifications: Boolean? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

sealed interface UiEvent {
    data class TaskDeleted(val task: TaskEntity) : UiEvent
    data class ShowMessage(val message: String) : UiEvent
}

class TaskFlowViewModel(
    private val repository: TaskFlowRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFlowUiState())
    val uiState: StateFlow<TaskFlowUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var alarmTimeoutJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialDataSeeded()
        }

        viewModelScope.launch {
            val dataFlow = combine(repository.allTasks, repository.allTimeBlocks) { tasks, blocks ->
                tasks to blocks
            }
            val prefsFlow = combine(
                repository.focusSessionsToday,
                repository.soundEnabled,
                repository.hasRequestedNotifications,
                repository.themeMode
            ) { sessionsToday, soundEnabled, hasRequestedNotif, themeMode ->
                PrefsSnapshot(sessionsToday, soundEnabled, hasRequestedNotif, themeMode)
            }

            combine(dataFlow, prefsFlow) { (tasks, blocks), prefs ->
                CombinedSnapshot(tasks, blocks, prefs)
            }.collect { snapshot ->
                val tasks = snapshot.tasks
                val blocks = snapshot.blocks
                val prefs = snapshot.prefs

                _uiState.update { current ->
                    val filtered = when (current.taskFilter) {
                        TaskFilter.ALL -> tasks
                        TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
                        TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
                    }
                    val grouped = Quadrant.entries.associateWith { quadrant ->
                        filtered.filter { it.quadrant == quadrant }
                    }
                    val evaluation = buildEvaluationSummary(tasks)
                    val selectedTaskStillExists = tasks.any { it.id == current.pomodoro.selectedTaskId }
                    val fallbackTaskId = if (selectedTaskStillExists) {
                        current.pomodoro.selectedTaskId
                    } else {
                        tasks.firstOrNull { !it.isCompleted && it.quadrant == Quadrant.Q1_DO_FIRST }?.id
                            ?: tasks.firstOrNull { !it.isCompleted }?.id
                    }

                    current.copy(
                        allTasks = tasks,
                        tasksByQuadrant = grouped,
                        timeBlocksByHour = blocks.associateBy { it.hourStart },
                        evaluation = evaluation,
                        hasRequestedNotifications = prefs.hasRequestedNotifications,
                        themeMode = prefs.themeMode,
                        pomodoro = current.pomodoro.copy(
                            selectedTaskId = fallbackTaskId,
                            focusSessionsToday = prefs.sessionsToday,
                            soundEnabled = prefs.soundEnabled
                        )
                    )
                }
                val blocksMap = blocks.associateBy { it.hourStart }
                DeadlineScheduler.syncTaskDeadlineAlarms(appContext, tasks)
                DeadlineScheduler.syncScheduleAlarms(appContext, blocksMap)
                checkAndPushImminentDeadlines(tasks, blocks)
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(60_000L)
                val state = _uiState.value
                checkAndPushImminentDeadlines(
                    tasks = state.allTasks,
                    blocks = state.timeBlocksByHour.values.toList()
                )
            }
        }
    }

    private suspend fun checkAndPushImminentDeadlines(
        tasks: List<TaskEntity>,
        blocks: List<TimeBlockEntity>
    ) {
        if (!NotificationHelper.areNotificationsEnabled(appContext)) return
        val now = System.currentTimeMillis()
        val notifiedKeys = repository.getNotifiedReminderKeys()
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))

        val twoHoursMs = 2 * 60 * 60 * 1000L
        val thirtyMinMs = 30 * 60 * 1000L
        val fifteenMinMs = 15 * 60 * 1000L
        val fiveMinMs = 5 * 60 * 1000L

        tasks.filter { !it.isCompleted }.forEach { task ->
            val diff = task.deadlineMillis - now
            when {
                diff in (-thirtyMinMs)..thirtyMinMs -> {
                    val key = "task_${task.id}_30m"
                    if (key !in notifiedKeys) {
                        repository.addNotifiedReminderKey(key)
                        NotificationHelper.showDeadlineHeadsUpNotification(
                            context = appContext,
                            notificationId = 8500 + task.id.toInt(),
                            title = "Deadline Sangat Dekat!",
                            body = "Segera tuntaskan \"${task.title}\" (${task.course}) — tenggat pukul ${DateTimeFormatters.formatTime(task.deadlineMillis)}."
                        )
                    }
                }
                diff in (thirtyMinMs + 1)..twoHoursMs -> {
                    val key = "task_${task.id}_2h"
                    if (key !in notifiedKeys) {
                        repository.addNotifiedReminderKey(key)
                        NotificationHelper.showDeadlineHeadsUpNotification(
                            context = appContext,
                            notificationId = 8000 + task.id.toInt(),
                            title = "Deadline Matriks Mendekat (< 2 Jam)",
                            body = "${task.quadrant.code}: \"${task.title}\" (${task.course}) tenggat pukul ${DateTimeFormatters.formatTime(task.deadlineMillis)}."
                        )
                    }
                }
            }
        }

        blocks.filter { !it.isCompleted }.forEach { block ->
            val blockStartMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, block.hourStart)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val diff = blockStartMillis - now
            if (diff in (-fiveMinMs)..fifteenMinMs) {
                val key = "block_${todayKey}_${block.hourStart}"
                if (key !in notifiedKeys) {
                    repository.addNotifiedReminderKey(key)
                    val timeStr = String.format("%02d:00", block.hourStart)
                    NotificationHelper.showDeadlineHeadsUpNotification(
                        context = appContext,
                        notificationId = 9000 + block.hourStart,
                        title = "Jadwal Harian Sebentar Lagi ($timeStr)",
                        body = "Waktunya: ${block.title}. Siapkan dirimu dengan tenang."
                    )
                }
            }
        }
    }

    private fun buildEvaluationSummary(tasks: List<TaskEntity>): DailyEvaluationSummary {
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val fraction = if (total > 0) completed.toFloat() / total.toFloat() else 0f
        val percent = (fraction * 100).toInt()
        val message = when {
            total == 0 -> "Tambahkan tugas pertamamu agar jadwal lebih terarah dan tenang."
            completed == 0 -> "Mulailah dari satu tugas Kuadran I. Langkah kecil meredakan cemas."
            completed == total -> "MasyaAllah, semua tugas tuntas! Berikan waktu istirahat yang layak."
            percent >= 50 -> "Progres luar biasa! Pertahankan ritme belajar yang sehat & bertahap."
            else -> "Alhamdulillah, $completed dari $total tugas selesai. Lanjutkan dengan tenang."
        }
        return DailyEvaluationSummary(
            totalTasks = total,
            completedTasks = completed,
            progressFraction = fraction,
            percentage = percent,
            encouragementMessage = message
        )
    }

    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setTaskFilter(filter: TaskFilter) {
        _uiState.update { state ->
            val filtered = when (filter) {
                TaskFilter.ALL -> state.allTasks
                TaskFilter.ACTIVE -> state.allTasks.filter { !it.isCompleted }
                TaskFilter.COMPLETED -> state.allTasks.filter { it.isCompleted }
            }
            state.copy(
                taskFilter = filter,
                tasksByQuadrant = Quadrant.entries.associateWith { quadrant ->
                    filtered.filter { it.quadrant == quadrant }
                }
            )
        }
    }

    fun openAddTaskSheet(open: Boolean) {
        _uiState.update { it.copy(showAddTaskSheet = open) }
    }

    fun openPrototypeInfoSheet(open: Boolean) {
        _uiState.update { it.copy(showPrototypeInfoSheet = open) }
    }

    fun addTask(
        title: String,
        course: String,
        deadlineMillis: Long,
        isImportant: Boolean,
        isUrgent: Boolean
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newId = repository.addTask(title, course, deadlineMillis, isImportant, isUrgent)
            val quadrant = Quadrant.fromImportanceAndUrgency(isImportant, isUrgent)
            _uiState.update { it.copy(showAddTaskSheet = false) }
            if (_uiState.value.pomodoro.selectedTaskId == null) {
                selectPomodoroTask(newId)
            }
            _events.emit(UiEvent.ShowMessage("Tugas masuk ke ${quadrant.code} (${quadrant.title})"))
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskEntity) {
        DeadlineScheduler.cancelTaskAlarms(appContext, task.id)
        viewModelScope.launch {
            repository.deleteTask(task.id)
            _events.emit(UiEvent.TaskDeleted(task))
        }
    }

    fun undoDeleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.restoreTask(task)
        }
    }

    fun moveTaskToFocus(task: TaskEntity, switchTab: Boolean = true) {
        _uiState.update { state ->
            state.copy(
                currentTab = if (switchTab) MainTab.POMODORO else state.currentTab,
                pomodoro = state.pomodoro.copy(
                    selectedTaskId = task.id,
                    mode = PomodoroMode.FOCUS,
                    totalSeconds = state.pomodoro.focusDurationMinutes * 60,
                    remainingSeconds = if (state.pomodoro.isRunning) {
                        state.pomodoro.remainingSeconds
                    } else {
                        state.pomodoro.focusDurationMinutes * 60
                    }
                )
            )
        }
        if (!switchTab) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("Fokus diarahkan ke: ${task.title}"))
            }
        }
    }

    fun openQuickScheduleForTask(task: TaskEntity?) {
        _uiState.update { it.copy(scheduleTargetTask = task) }
    }

    fun assignTaskToHour(hourStart: Int, task: TaskEntity) {
        viewModelScope.launch {
            repository.assignTaskToTimeBlock(hourStart, task)
            _uiState.update { it.copy(scheduleTargetTask = null) }
            _events.emit(
                UiEvent.ShowMessage(
                    "Dijadwalkan pukul ${String.format("%02d:00", hourStart)}: ${task.title}"
                )
            )
        }
    }

    fun openEditHourSlot(hourStart: Int?) {
        _uiState.update { it.copy(editingHourSlot = hourStart) }
    }

    fun saveCustomTimeBlock(hourStart: Int, title: String, subtitle: String, quadrant: Quadrant?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.saveCustomTimeBlock(hourStart, title, subtitle, quadrant)
            _uiState.update { it.copy(editingHourSlot = null) }
        }
    }

    fun toggleTimeBlockCompleted(block: TimeBlockEntity) {
        viewModelScope.launch {
            repository.setTimeBlockCompleted(block.hourStart, !block.isCompleted)
        }
    }

    fun clearTimeBlock(hourStart: Int) {
        viewModelScope.launch {
            repository.clearTimeBlock(hourStart)
            _uiState.update { it.copy(editingHourSlot = null) }
        }
    }

    // Pomodoro Controls
    fun selectPomodoroTask(taskId: Long?) {
        _uiState.update { it.copy(pomodoro = it.pomodoro.copy(selectedTaskId = taskId)) }
    }

    fun switchPomodoroMode(mode: PomodoroMode) {
        stopLongAlarm()
        DeadlineScheduler.cancelPomodoroFinish(appContext)
        timerJob?.cancel()
        _uiState.update { state ->
            val minutes = if (mode == PomodoroMode.FOCUS) {
                state.pomodoro.focusDurationMinutes
            } else {
                state.pomodoro.breakDurationMinutes
            }
            state.copy(
                pomodoro = state.pomodoro.copy(
                    mode = mode,
                    totalSeconds = minutes * 60,
                    remainingSeconds = minutes * 60,
                    isRunning = false
                )
            )
        }
    }

    fun setFocusPresetMinutes(minutes: Int) {
        stopLongAlarm()
        DeadlineScheduler.cancelPomodoroFinish(appContext)
        timerJob?.cancel()
        _uiState.update { state ->
            state.copy(
                pomodoro = state.pomodoro.copy(
                    mode = PomodoroMode.FOCUS,
                    focusDurationMinutes = minutes,
                    totalSeconds = minutes * 60,
                    remainingSeconds = minutes * 60,
                    isRunning = false
                )
            )
        }
    }

    fun toggleTimerRunning() {
        val currentRunning = _uiState.value.pomodoro.isRunning
        if (currentRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        stopLongAlarm()
        timerJob?.cancel()
        _uiState.update { it.copy(pomodoro = it.pomodoro.copy(isRunning = true)) }

        val currentPomodoro = _uiState.value.pomodoro
        val selectedTaskTitle = _uiState.value.allTasks
            .firstOrNull { it.id == currentPomodoro.selectedTaskId }
            ?.title
        val (notifTitle, notifBody) = buildPomodoroNotificationCopy(
            mode = currentPomodoro.mode,
            selectedTaskTitle = selectedTaskTitle
        )
        DeadlineScheduler.schedulePomodoroFinish(
            context = appContext,
            remainingSeconds = currentPomodoro.remainingSeconds,
            title = notifTitle,
            body = notifBody,
            playSound = currentPomodoro.soundEnabled
        )

        timerJob = viewModelScope.launch {
            while (_uiState.value.pomodoro.isRunning && _uiState.value.pomodoro.remainingSeconds > 0) {
                delay(1000L)
                _uiState.update { state ->
                    val next = (state.pomodoro.remainingSeconds - 1).coerceAtLeast(0)
                    state.copy(pomodoro = state.pomodoro.copy(remainingSeconds = next))
                }
            }
            if (_uiState.value.pomodoro.remainingSeconds == 0 && _uiState.value.pomodoro.isRunning) {
                onTimerCompleted()
            }
        }
    }

    private fun pauseTimer() {
        DeadlineScheduler.cancelPomodoroFinish(appContext)
        timerJob?.cancel()
        _uiState.update { it.copy(pomodoro = it.pomodoro.copy(isRunning = false)) }
    }

    fun resetTimer() {
        stopLongAlarm()
        DeadlineScheduler.cancelPomodoroFinish(appContext)
        timerJob?.cancel()
        _uiState.update { state ->
            val minutes = if (state.pomodoro.mode == PomodoroMode.FOCUS) {
                state.pomodoro.focusDurationMinutes
            } else {
                state.pomodoro.breakDurationMinutes
            }
            state.copy(
                pomodoro = state.pomodoro.copy(
                    isRunning = false,
                    totalSeconds = minutes * 60,
                    remainingSeconds = minutes * 60
                )
            )
        }
    }

    private fun buildPomodoroNotificationCopy(
        mode: PomodoroMode,
        selectedTaskTitle: String?
    ): Pair<String, String> {
        val title = if (mode == PomodoroMode.FOCUS) {
            "Sesi Fokus Selesai"
        } else {
            "Waktu Istirahat Selesai"
        }
        val body = if (mode == PomodoroMode.FOCUS) {
            if (selectedTaskTitle != null) {
                "Alhamdulillah! Sesi untuk \"$selectedTaskTitle\" selesai. Istirahat sejenak 5 menit."
            } else {
                "Alhamdulillah! Sesi fokus selesai. Istirahat sejenak 5 menit untuk menjaga ketenangan."
            }
        } else {
            "Pikiran lebih segar? Mari lanjutkan langkah berikutnya dengan tenang."
        }
        return title to body
    }

    private fun onTimerCompleted() {
        DeadlineScheduler.cancelPomodoroFinish(appContext)
        val completedMode = _uiState.value.pomodoro.mode
        val soundOn = _uiState.value.pomodoro.soundEnabled
        _uiState.update { state ->
            state.copy(
                pomodoro = state.pomodoro.copy(
                    isRunning = false,
                    completionModalMode = completedMode
                )
            )
        }
        viewModelScope.launch {
            if (completedMode == PomodoroMode.FOCUS) {
                repository.recordCompletedFocusSession()
            }
            val selectedTaskTitle = _uiState.value.allTasks
                .firstOrNull { it.id == _uiState.value.pomodoro.selectedTaskId }
                ?.title
            val notifTitle = if (completedMode == PomodoroMode.FOCUS) {
                "Sesi Fokus Selesai"
            } else {
                "Waktu Istirahat Selesai"
            }
            val notifBody = if (completedMode == PomodoroMode.FOCUS) {
                if (selectedTaskTitle != null) {
                    "Alhamdulillah! Sesi untuk \"$selectedTaskTitle\" selesai. Istirahat sejenak 5 menit."
                } else {
                    "Alhamdulillah! Sesi fokus selesai. Istirahat sejenak 5 menit untuk menjaga ketenangan."
                }
            } else {
                "Pikiran lebih segar? Mari lanjutkan langkah berikutnya dengan tenang."
            }
            NotificationHelper.showPomodoroFinishedNotification(appContext, notifTitle, notifBody)
            GentleChimePlayer.startLongAlarm(appContext, soundOn)
        }

        alarmTimeoutJob?.cancel()
        alarmTimeoutJob = viewModelScope.launch {
            delay(30_000L)
            GentleChimePlayer.stopAlarm(appContext)
        }
    }

    private fun stopLongAlarm() {
        alarmTimeoutJob?.cancel()
        alarmTimeoutJob = null
        GentleChimePlayer.stopAlarm(appContext)
    }

    fun dismissCompletionModal(switchToOppositeMode: Boolean) {
        stopLongAlarm()
        val finishedMode = _uiState.value.pomodoro.completionModalMode
        _uiState.update { state ->
            state.copy(pomodoro = state.pomodoro.copy(completionModalMode = null))
        }
        if (switchToOppositeMode && finishedMode != null) {
            val nextMode = if (finishedMode == PomodoroMode.FOCUS) PomodoroMode.BREAK else PomodoroMode.FOCUS
            switchPomodoroMode(nextMode)
        } else {
            resetTimer()
        }
    }

    fun toggleSoundEnabled() {
        val next = !_uiState.value.pomodoro.soundEnabled
        if (!next) {
            stopLongAlarm()
        }
        viewModelScope.launch {
            repository.setSoundEnabled(next)
        }
    }

    fun markNotificationsRequested() {
        viewModelScope.launch {
            repository.markNotificationsRequested()
        }
    }

    fun openSettingsSheet(open: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = open) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            _uiState.update { it.copy(showPrototypeInfoSheet = false) }
            _events.emit(UiEvent.ShowMessage("4 contoh tugas mahasiswa & jadwal berhasil dimuat ulang."))
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        stopLongAlarm()
        super.onCleared()
    }

    private data class PrefsSnapshot(
        val sessionsToday: Int,
        val soundEnabled: Boolean,
        val hasRequestedNotifications: Boolean,
        val themeMode: ThemeMode
    )

    private data class CombinedSnapshot(
        val tasks: List<TaskEntity>,
        val blocks: List<TimeBlockEntity>,
        val prefs: PrefsSnapshot
    )

    class Factory(
        private val repository: TaskFlowRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskFlowViewModel(repository, appContext.applicationContext) as T
        }
    }
}
