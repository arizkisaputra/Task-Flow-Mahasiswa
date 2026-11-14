package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskFlowRepository(
    private val taskDao: TaskDao,
    private val timeBlockDao: TimeBlockDao,
    private val preferencesManager: PreferencesManager
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.observeAllTasks()
    val allTimeBlocks: Flow<List<TimeBlockEntity>> = timeBlockDao.observeAllBlocks()
    val soundEnabled: Flow<Boolean> = preferencesManager.soundEnabled
    val hasRequestedNotifications: Flow<Boolean> = preferencesManager.hasRequestedNotifications
    val focusSessionsToday: Flow<Int> = preferencesManager.focusSessionsToday

    suspend fun ensureInitialDataSeeded() {
        if (!preferencesManager.isSeeded() && taskDao.countTasks() == 0) {
            seedDemoData()
            preferencesManager.markSeeded()
        }
    }

    suspend fun resetDemoData() {
        taskDao.clearAllTasks()
        timeBlockDao.clearAllBlocks()
        seedDemoData()
        preferencesManager.markSeeded()
    }

    private suspend fun seedDemoData() {
        val tomorrowEvening = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val fiveDaysLater = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 5)
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayTonight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayLate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val q1Id = taskDao.insertTask(
            TaskEntity(
                title = "Laporan Praktikum Algoritma",
                course = "Algoritma & Pemrograman",
                deadlineMillis = tomorrowEvening,
                isImportant = true,
                isUrgent = true,
                isCompleted = false
            )
        )
        val q2Id = taskDao.insertTask(
            TaskEntity(
                title = "Cicil Makalah Pendidikan Agama Islam",
                course = "Pendidikan Agama Islam",
                deadlineMillis = fiveDaysLater,
                isImportant = true,
                isUrgent = false,
                isCompleted = false
            )
        )
        val q3Id = taskDao.insertTask(
            TaskEntity(
                title = "Balas Chat Grup Kepanitiaan",
                course = "Organisasi Mahasiswa",
                deadlineMillis = todayTonight,
                isImportant = false,
                isUrgent = true,
                isCompleted = false
            )
        )
        taskDao.insertTask(
            TaskEntity(
                title = "Kurangi Scrolling Media Sosial",
                course = "Pengendalian Diri",
                deadlineMillis = todayLate,
                isImportant = false,
                isUrgent = false,
                isCompleted = false
            )
        )

        // Seed realistic student time-blocks from morning to evening
        val sampleBlocks = listOf(
            TimeBlockEntity(
                hourStart = 7,
                taskId = null,
                title = "Persiapan Kuliah & Sarapan Tenang",
                subtitle = "Rutinitas Pagi",
                quadrantOrdinal = Quadrant.Q2_SCHEDULE.ordinal,
                isCompleted = true
            ),
            TimeBlockEntity(
                hourStart = 9,
                taskId = q1Id,
                title = "Laporan Praktikum Algoritma",
                subtitle = "Algoritma & Pemrograman • Sesi Fokus",
                quadrantOrdinal = Quadrant.Q1_DO_FIRST.ordinal,
                isCompleted = false
            ),
            TimeBlockEntity(
                hourStart = 13,
                taskId = q2Id,
                title = "Cicil Makalah Pendidikan Agama Islam",
                subtitle = "Pendidikan Agama Islam • Riset & Kerangka",
                quadrantOrdinal = Quadrant.Q2_SCHEDULE.ordinal,
                isCompleted = false
            ),
            TimeBlockEntity(
                hourStart = 16,
                taskId = q3Id,
                title = "Balas Chat Grup Kepanitiaan",
                subtitle = "Dibatasi 20 menit agar tidak mengganggu kuliah",
                quadrantOrdinal = Quadrant.Q3_DELEGATE.ordinal,
                isCompleted = false
            ),
            TimeBlockEntity(
                hourStart = 19,
                taskId = null,
                title = "Review Materi & Istirahat Terjadwal",
                subtitle = "Jaga Kesehatan Mental & Keseimbangan",
                quadrantOrdinal = Quadrant.Q2_SCHEDULE.ordinal,
                isCompleted = false
            )
        )
        timeBlockDao.upsertBlocks(sampleBlocks)
    }

    suspend fun addTask(
        title: String,
        course: String,
        deadlineMillis: Long,
        isImportant: Boolean,
        isUrgent: Boolean
    ): Long {
        return taskDao.insertTask(
            TaskEntity(
                title = title.trim(),
                course = course.trim().ifEmpty { "Tugas Kuliah" },
                deadlineMillis = deadlineMillis,
                isImportant = isImportant,
                isUrgent = isUrgent
            )
        )
    }

    suspend fun restoreTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun setTaskCompleted(taskId: Long, completed: Boolean) {
        taskDao.setTaskCompleted(taskId, completed)
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
        timeBlockDao.clearBlocksForTask(taskId)
    }

    suspend fun assignTaskToTimeBlock(hourStart: Int, task: TaskEntity) {
        timeBlockDao.upsertBlock(
            TimeBlockEntity(
                hourStart = hourStart,
                taskId = task.id,
                title = task.title,
                subtitle = task.course,
                quadrantOrdinal = task.quadrant.ordinal,
                isCompleted = task.isCompleted
            )
        )
    }

    suspend fun saveCustomTimeBlock(
        hourStart: Int,
        title: String,
        subtitle: String,
        quadrant: Quadrant?
    ) {
        timeBlockDao.upsertBlock(
            TimeBlockEntity(
                hourStart = hourStart,
                taskId = null,
                title = title.trim(),
                subtitle = subtitle.trim(),
                quadrantOrdinal = quadrant?.ordinal,
                isCompleted = false
            )
        )
    }

    suspend fun setTimeBlockCompleted(hourStart: Int, completed: Boolean) {
        timeBlockDao.setBlockCompleted(hourStart, completed)
    }

    suspend fun clearTimeBlock(hourStart: Int) {
        timeBlockDao.clearBlock(hourStart)
    }

    suspend fun recordCompletedFocusSession() {
        preferencesManager.incrementFocusSessionToday()
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        preferencesManager.setSoundEnabled(enabled)
    }

    suspend fun markNotificationsRequested() {
        preferencesManager.markNotificationsRequested()
    }

    suspend fun getNotifiedReminderKeys(): Set<String> {
        return preferencesManager.getNotifiedReminderKeys()
    }

    suspend fun addNotifiedReminderKey(key: String) {
        preferencesManager.addNotifiedReminderKey(key)
    }
}
