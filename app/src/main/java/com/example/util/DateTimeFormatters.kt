package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeFormatters {
    private val indoLocale = Locale("id", "ID")
    private val formatLock = Any()
    private val timeFormat = SimpleDateFormat("HH:mm", indoLocale)
    private val dateTimeFormat = SimpleDateFormat("dd MMM, HH:mm", indoLocale)
    private val dateOnlyFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", indoLocale)
    private val storageDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val hourLabels: Array<String> = Array(24) { hour ->
        buildString(5) {
            append((hour / 10).digitToChar())
            append((hour % 10).digitToChar())
            append(":00")
        }
    }

    @Volatile
    private var cachedTodayStart: Long = 0L
    @Volatile
    private var cachedTomorrowStart: Long = 0L
    @Volatile
    private var cachedDayAfterStart: Long = 0L

    private fun ensureDayBounds(now: Long) {
        if (now in cachedTodayStart until cachedTomorrowStart) return
        synchronized(formatLock) {
            if (now in cachedTodayStart until cachedTomorrowStart) return
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val today = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayAfter = cal.timeInMillis

            cachedTodayStart = today
            cachedTomorrowStart = tomorrow
            cachedDayAfterStart = dayAfter
        }
    }

    fun formatDeadlineLabel(deadlineMillis: Long): String {
        val now = System.currentTimeMillis()
        ensureDayBounds(now)
        val date = Date(deadlineMillis)
        return when {
            deadlineMillis in cachedTodayStart until cachedTomorrowStart -> {
                val timeText = synchronized(formatLock) { timeFormat.format(date) }
                "Hari Ini, $timeText"
            }
            deadlineMillis in cachedTomorrowStart until cachedDayAfterStart -> {
                val timeText = synchronized(formatLock) { timeFormat.format(date) }
                "Besok, $timeText"
            }
            else -> synchronized(formatLock) { dateTimeFormat.format(date) }
        }
    }

    fun formatFullDate(millis: Long): String =
        synchronized(formatLock) { dateOnlyFormat.format(Date(millis)) }

    fun formatTime(millis: Long): String =
        synchronized(formatLock) { timeFormat.format(Date(millis)) }

    fun todayStorageKey(now: Long = System.currentTimeMillis()): String =
        synchronized(formatLock) { storageDateFormat.format(Date(now)) }

    fun formatHourSlot(hour: Int): String =
        if (hour in 0..23) hourLabels[hour] else "${hour.toString().padStart(2, '0')}:00"

    fun formatTimerSeconds(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val mins = safe / 60
        val secs = safe % 60
        return buildString(5) {
            if (mins < 10) append('0')
            append(mins)
            append(':')
            if (secs < 10) append('0')
            append(secs)
        }
    }

    fun isDeadlineOverdue(deadlineMillis: Long, isCompleted: Boolean): Boolean {
        if (isCompleted) return false
        return deadlineMillis < System.currentTimeMillis()
    }
}

