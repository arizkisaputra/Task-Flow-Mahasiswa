package com.example.ui

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PrefsSnapshot
import com.example.data.Quadrant
import com.example.data.TaskEntity
import com.example.data.TaskFlowRepository
import com.example.data.ThemeMode
import com.example.data.TimeBlockEntity
import com.example.util.DateTimeFormatters
import com.example.util.DeadlineScheduler
import com.example.util.GentleChimePlayer
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.EnumMap

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

@Immutable
data class QuadrantProgress(
    val done: Int = 0,
    val total: Int = 0
)

@Immutable
data class DailyEvaluationSummary(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val progressFraction: Float = 0f,
    val percentage: Int = 0,
    val encouragementMessage: String = "Mari mulai satu langkah kecil dengan tenang.",
    val quadrantProgress: Map<Quadrant, QuadrantProgress> = emptyMap()
)

@Immutable
data class PomodoroState(
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val totalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val selectedTaskId: Long? = null,
    val focusSessionsToday: Int = 0,
    val soundEnabled: Boolean = true,
    val completionModalMode: PomodoroMode? = null
)

@Immutable
data class TaskFlowUiState(
    val currentTab: MainTab = MainTab.MATRIX,
    val taskFilter: TaskFilter = TaskFilter.ALL,
    val selectedQuadrantFilter: Quadrant? = null,
    val allTasks: List<TaskEntity> = emptyList(),
    val tasksByQuadrant: Map<Quadrant, List<TaskEntity>> = emptyMap(),
    val timeBlocksByHour: Map<Int, TimeBlockEntity> = emptyMap(),
    val evaluation: DailyEvaluationSummary = DailyEvaluationSummary(),
    val pomodoro: PomodoroState = PomodoroState(),
    val showAddTaskSheet: Boolean = false,
    val showPrototypeInfoSheet: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val scheduleTargetTask: TaskEntity? = null,
    val editingHourSlot: Int? = null,
    val hasRequestedNotifications: Boolean? = null,
    val themeMode: ThemeMode = ThemeMode.DARK
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

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var alarmTimeoutJob: Job? = null

    @Volatile
    private var cachedNotifiedKeys: MutableSet<String>? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialDataSeeded()
        }

        // UI state pipeline: compute groupings & evaluation off the main thread
        viewModelScope.launch {
            combine(
                repository.allTasks,
                repository.allTimeBlocks,
                repository.prefsSnapshot,
                _uiState.map { it.taskFilter }.distinctUntilChanged()
            ) { tasks, blocks, prefs, filter ->
                buildUiPayload(tasks, blocks, prefs, filter)
            }
                .flowOn(Dispatchers.Default)
                .collect { payload ->
                    if (_themeMode.value != payload.prefs.themeMode) {
                        _themeMode.value = payload.prefs.themeMode
                    }
                    _uiState.update { current ->
                        val selectedTaskStillExists =
                            current.pomodoro.selectedTaskId != null &&
                                payload.tasks.any { it.id == current.pomodoro.selectedTaskId }
                        val fallbackTaskId = if (selectedTaskStillExists) {
                            current.pomodoro.selectedTaskId
                        } else {
                            payload.defaultFocusTaskId
                        }

                        current.copy(
                            allTasks = payload.tasks,
                            tasksByQuadrant = payload.tasksByQuadrant,
                            timeBlocksByHour = payload.blocksByHour,
                            evaluation = payload.evaluation,
                            hasRequestedNotifications = payload.prefs.hasRequestedNotifications,
                            themeMode = payload.prefs.themeMode,
                            pomodoro = current.pomodoro.copy(
                                selectedTaskId = fallbackTaskId,
                                focusSessionsToday = payload.prefs.focusSessionsToday,
                                soundEnabled = payload.prefs.soundEnabled
                            )
                        )
                    }
                }
        }

        // Background alarm sync pipeline: runs ONLY when tasks or blocks change, never on theme/sound changes
        viewModelScope.launch(Dispatchers.Default) {
            combine(repository.allTasks, repository.allTimeBlocks) { tasks, blocks ->
                tasks to blocks
            }
                .distinctUntilChanged()
                .collect { (tasks, blocks) ->
                    val blocksMap = blocks.associateBy { it.hourStart }
                    DeadlineScheduler.syncTaskDeadlineAlarms(appContext, tasks)
                    DeadlineScheduler.syncScheduleAlarms(appContext, blocksMap)
                    checkAndPushImminentDeadlines(tasks, blocks)
                }
        }

        // Periodic 60s imminent-deadline check on background dispatcher; skips work if nothing pending
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(60_000L)
                val state = _uiState.value
                val tasks = state.allTasks
                val blocksMap = state.timeBlocksByHour
                if (tasks.isNotEmpty() || blocksMap.isNotEmpty()) {
                    checkAndPushImminentDeadlines(
                        tasks = tasks,
                        blocks = blocksMap.values
                    )
                }
            }
        }
    }

    private fun buildUiPayload(
        tasks: List<TaskEntity>,
        blocks: List<TimeBlockEntity>,
        prefs: PrefsSnapshot,
        filter: TaskFilter
    ): UiPayload {
        val tasksByQuadrant = groupTasksByQuadrant(tasks, filter)
        val evaluation = buildEvaluationSummary(tasks)
        val blocksByHour = if (blocks.isEmpty()) emptyMap() else blocks.associateBy { it.hourStart }

        var firstQ1Active: Long? = null
        var firstAnyActive: Long? = null
        for (task in tasks) {
            if (!task.isCompleted) {
                if (firstAnyActive == null) firstAnyActive = task.id
                if (firstQ1Active == null && task.quadrant == Quadrant.Q1_DO_FIRST) {
                    firstQ1Active = task.id
                    break
                }
            }
        }

        return UiPayload(
            tasks = tasks,
            tasksByQuadrant = tasksByQuadrant,
            blocksByHour = blocksByHour,
            evaluation = evaluation,
            prefs = prefs,
            defaultFocusTaskId = firstQ1Active ?: firstAnyActive
        )
    }

    private fun groupTasksByQuadrant(
        tasks: List<TaskEntity>,
        filter: TaskFilter
    ): Map<Quadrant, List<TaskEntity>> {
        val map = EnumMap<Quadrant, MutableList<TaskEntity>>(Quadrant::class.java)
        for (q in Quadrant.entries) {
            map[q] = ArrayList()
        }
        for (task in tasks) {
            val include = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.ACTIVE -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }
            if (include) {
                map[task.quadrant]?.add(task)
            }
        }
        return map
    }

    private suspend fun checkAndPushImminentDeadlines(
        tasks: List<TaskEntity>,
        blocks: Collection<TimeBlockEntity>
    ) {
        if (!NotificationHelper.areNotificationsEnabled(appContext)) return

        val hasPendingTask = tasks.any { !it.isCompleted }
        val hasPendingBlock = blocks.any { !it.isCompleted }
        if (!hasPendingTask && !hasPendingBlock) return

        val keys = cachedNotifiedKeys ?: withContext(Dispatchers.IO) {
            repository.getNotifiedReminderKeys().toMutableSet().also { cachedNotifiedKeys = it }
        }

        val now = System.currentTimeMillis()
        val twoHoursMs = 2 * 60 * 60 * 1000L
        val thirtyMinMs = 30 * 60 * 1000L
        val fifteenMinMs = 15 * 60 * 1000L
        val fiveMinMs = 5 * 60 * 1000L

        if (hasPendingTask) {
            for (task in tasks) {
                if (task.isCompleted) continue
                val diff = task.deadlineMillis - now
                when {
                    diff in (-thirtyMinMs)..thirtyMinMs -> {
                        val key = "task_${task.id}_30m"
                        if (key !in keys) {
                            keys.add(key)
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
                        if (key !in keys) {
                            keys.add(key)
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
        }

        if (hasPendingBlock) {
            val todayKey = DateTimeFormatters.todayStorageKey(now)
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            for (block in blocks) {
                if (block.isCompleted) continue
                cal.set(Calendar.HOUR_OF_DAY, block.hourStart)
                val diff = cal.timeInMillis - now
                if (diff in (-fiveMinMs)..fifteenMinMs) {
                    val key = "block_${todayKey}_${block.hourStart}"
                    if (key !in keys) {
                        keys.add(key)
                        repository.addNotifiedReminderKey(key)
                        val timeStr = DateTimeFormatters.formatHourSlot(block.hourStart)
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
    }

    private fun buildEvaluationSummary(tasks: List<TaskEntity>): DailyEvaluationSummary {
        val total = tasks.size
        var completed = 0
        val doneCounts = IntArray(Quadrant.entries.size)
        val totalCounts = IntArray(Quadrant.entries.size)

        for (task in tasks) {
            val idx = task.quadrant.ordinal
            totalCounts[idx]++
            if (task.isCompleted) {
                completed++
                doneCounts[idx]++
            }
        }

        val progressMap = EnumMap<Quadrant, QuadrantProgress>(Quadrant::class.java)
        for (q in Quadrant.entries) {
            val idx = q.ordinal
            progressMap[q] = QuadrantProgress(done = doneCounts[idx], total = totalCounts[idx])
        }

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
            encouragementMessage = message,
            quadrantProgress = progressMap
        )
    }

    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setTaskFilter(filter: TaskFilter) {
        _uiState.update { state ->
            if (state.taskFilter == filter) return@update state
            state.copy(
                taskFilter = filter,
                tasksByQuadrant = groupTasksByQuadrant(state.allTasks, filter)
            )
        }
    }

    fun setQuadrantFilter(quadrant: Quadrant?) {
        _uiState.update { it.copy(selectedQuadrantFilter = quadrant) }
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
        val focusTotal = _uiState.value.pomodoro.focusDurationMinutes * 60
        if (!_uiState.value.pomodoro.isRunning) {
            _remainingSeconds.value = focusTotal
        }
        _uiState.update { state ->
            state.copy(
                currentTab = if (switchTab) MainTab.POMODORO else state.currentTab,
                pomodoro = state.pomodoro.copy(
                    selectedTaskId = task.id,
                    mode = PomodoroMode.FOCUS,
                    totalSeconds = focusTotal
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
                    "Dijadwalkan pukul ${DateTimeFormatters.formatHourSlot(hourStart)}: ${task.title}"
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
        val minutes = if (mode == PomodoroMode.FOCUS) {
            _uiState.value.pomodoro.focusDurationMinutes
        } else {
            _uiState.value.pomodoro.breakDurationMinutes
        }
        val seconds = minutes * 60
        _remainingSeconds.value = seconds
        _uiState.update { state ->
            state.copy(
                pomodoro = state.pomodoro.copy(
                    mode = mode,
                    totalSeconds = seconds,
                    isRunning = false
                )
            )
        }
    }

    fun setFocusPresetMinutes(minutes: Int) {
        stopLongAlarm()
        DeadlineScheduler.cancelPomodoroFinish(appContext)
        timerJob?.cancel()
        val seconds = minutes * 60
        _remainingSeconds.value = seconds
        _uiState.update { state ->
            state.copy(
                pomodoro = state.pomodoro.copy(
                    mode = PomodoroMode.FOCUS,
                    focusDurationMinutes = minutes,
                    totalSeconds = seconds,
                    isRunning = false
                )
            )
        }
    }

    fun toggleTimerRunning() {
        if (_uiState.value.pomodoro.isRunning) {
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
        val currentRemaining = _remainingSeconds.value
        val selectedTaskTitle = _uiState.value.allTasks
            .firstOrNull { it.id == currentPomodoro.selectedTaskId }
            ?.title
        val (notifTitle, notifBody) = buildPomodoroNotificationCopy(
            mode = currentPomodoro.mode,
            selectedTaskTitle = selectedTaskTitle
        )
        DeadlineScheduler.schedulePomodoroFinish(
            context = appContext,
            remainingSeconds = currentRemaining,
            title = notifTitle,
            body = notifBody,
            playSound = currentPomodoro.soundEnabled
        )

        timerJob = viewModelScope.launch {
            while (_uiState.value.pomodoro.isRunning && _remainingSeconds.value > 0) {
                delay(1000L)
                if (!_uiState.value.pomodoro.isRunning) break
                _remainingSeconds.value = (_remainingSeconds.value - 1).coerceAtLeast(0)
            }
            if (_remainingSeconds.value == 0 && _uiState.value.pomodoro.isRunning) {
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
        val pomodoro = _uiState.value.pomodoro
        val minutes = if (pomodoro.mode == PomodoroMode.FOCUS) {
            pomodoro.focusDurationMinutes
        } else {
            pomodoro.breakDurationMinutes
        }
        val seconds = minutes * 60
        _remainingSeconds.value = seconds
        _uiState.update { state ->
            state.copy(
                pomodoro = state.pomodoro.copy(
                    isRunning = false,
                    totalSeconds = seconds
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
            val (notifTitle, notifBody) = buildPomodoroNotificationCopy(
                mode = completedMode,
                selectedTaskTitle = selectedTaskTitle
            )
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

    fun testChimeSound() {
        viewModelScope.launch {
            GentleChimePlayer.playPreviewChime()
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

    private data class UiPayload(
        val tasks: List<TaskEntity>,
        val tasksByQuadrant: Map<Quadrant, List<TaskEntity>>,
        val blocksByHour: Map<Int, TimeBlockEntity>,
        val evaluation: DailyEvaluationSummary,
        val prefs: PrefsSnapshot,
        val defaultFocusTaskId: Long?
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
