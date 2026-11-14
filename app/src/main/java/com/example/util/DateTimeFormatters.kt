package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeFormatters {
    private val indoLocale = Locale("id", "ID")
    private val timeFormat = SimpleDateFormat("HH:mm", indoLocale)
    private val dateTimeFormat = SimpleDateFormat("dd MMM, HH:mm", indoLocale)
    private val dateOnlyFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", indoLocale)

    fun formatDeadlineLabel(deadlineMillis: Long): String {
        val target = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        val timeText = timeFormat.format(Date(deadlineMillis))
        return when {
            isSameDay(target, today) -> "Hari Ini, $timeText"
            isSameDay(target, tomorrow) -> "Besok, $timeText"
            else -> dateTimeFormat.format(Date(deadlineMillis))
        }
    }

    fun formatFullDate(millis: Long): String = dateOnlyFormat.format(Date(millis))

    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

    fun isDeadlineOverdue(deadlineMillis: Long, isCompleted: Boolean): Boolean {
        if (isCompleted) return false
        return deadlineMillis < System.currentTimeMillis()
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}
