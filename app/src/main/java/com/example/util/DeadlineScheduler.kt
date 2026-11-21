package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.TaskEntity
import com.example.data.TimeBlockEntity
import java.util.Calendar

object DeadlineScheduler {

    private const val REQ_POMODORO_FINISH = 70001
    private const val REQ_TASK_2H_BASE = 81000
    private const val REQ_TASK_30M_BASE = 82000
    private const val REQ_SCHEDULE_BASE = 91000

    private const val TWO_HOURS_MS = 2 * 60 * 60 * 1000L
    private const val THIRTY_MINUTES_MS = 30 * 60 * 1000L
    private const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L

    fun schedulePomodoroFinish(
        context: Context,
        remainingSeconds: Int,
        title: String,
        body: String,
        playSound: Boolean
    ) {
        if (remainingSeconds <= 0) return
        val triggerAt = System.currentTimeMillis() + remainingSeconds * 1000L
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_POMODORO_DONE)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, NotificationHelper.POMODORO_NOTIFICATION_ID)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_BODY, body)
            putExtra(ReminderReceiver.EXTRA_PLAY_SOUND, playSound)
        }
        scheduleAlarm(context, REQ_POMODORO_FINISH, triggerAt, intent)
    }

    fun cancelPomodoroFinish(context: Context) {
        cancelAlarm(context, REQ_POMODORO_FINISH)
    }

    fun cancelTaskAlarms(context: Context, taskId: Long) {
        val req2h = REQ_TASK_2H_BASE + (taskId.toInt() and 0x0FFF)
        val req30m = REQ_TASK_30M_BASE + (taskId.toInt() and 0x0FFF)
        cancelAlarm(context, req2h)
        cancelAlarm(context, req30m)
    }

    fun syncTaskDeadlineAlarms(context: Context, tasks: List<TaskEntity>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()
        tasks.forEach { task ->
            val req2h = REQ_TASK_2H_BASE + (task.id.toInt() and 0x0FFF)
            val req30m = REQ_TASK_30M_BASE + (task.id.toInt() and 0x0FFF)

            if (task.isCompleted) {
                cancelAlarm(context, alarmManager, req2h)
                cancelAlarm(context, alarmManager, req30m)
            } else {
                val formattedTime = DateTimeFormatters.formatTime(task.deadlineMillis)
                val trigger2h = task.deadlineMillis - TWO_HOURS_MS
                if (trigger2h > now) {
                    val intent = Intent(context, ReminderReceiver::class.java).apply {
                        putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_TASK_DEADLINE)
                        putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, 8000 + task.id.toInt())
                        putExtra(ReminderReceiver.EXTRA_TITLE, "Deadline Matriks 2 Jam Lagi")
                        putExtra(
                            ReminderReceiver.EXTRA_BODY,
                            "${task.quadrant.code}: \"${task.title}\" (${task.course}) jatuh tempo pukul $formattedTime."
                        )
                    }
                    scheduleAlarm(context, alarmManager, req2h, trigger2h, intent)
                } else {
                    cancelAlarm(context, alarmManager, req2h)
                }

                val trigger30m = task.deadlineMillis - THIRTY_MINUTES_MS
                if (trigger30m > now) {
                    val intent = Intent(context, ReminderReceiver::class.java).apply {
                        putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_TASK_DEADLINE)
                        putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, 8500 + task.id.toInt())
                        putExtra(ReminderReceiver.EXTRA_TITLE, "Deadline Sangat Dekat (30 Menit)!")
                        putExtra(
                            ReminderReceiver.EXTRA_BODY,
                            "Segera tuntaskan \"${task.title}\" (${task.course}) sebelum $formattedTime."
                        )
                    }
                    scheduleAlarm(context, alarmManager, req30m, trigger30m, intent)
                } else {
                    cancelAlarm(context, alarmManager, req30m)
                }
            }
        }
    }

    fun syncScheduleAlarms(context: Context, blocksByHour: Map<Int, TimeBlockEntity>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        for (hour in 6..21) {
            val reqCode = REQ_SCHEDULE_BASE + hour
            val block = blocksByHour[hour]
            if (block == null || block.isCompleted) {
                cancelAlarm(context, alarmManager, reqCode)
                continue
            }

            cal.set(Calendar.HOUR_OF_DAY, hour)
            val triggerAt = cal.timeInMillis - FIFTEEN_MINUTES_MS
            if (triggerAt > now) {
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_SCHEDULE_REMINDER)
                    putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, 9000 + hour)
                    putExtra(
                        ReminderReceiver.EXTRA_TITLE,
                        "Jadwal Harian ${DateTimeFormatters.formatHourSlot(hour)} Sebentar Lagi"
                    )
                    putExtra(
                        ReminderReceiver.EXTRA_BODY,
                        "15 menit lagi: ${block.title}. Siapkan dirimu dengan tenang."
                    )
                }
                scheduleAlarm(context, alarmManager, reqCode, triggerAt, intent)
            } else {
                cancelAlarm(context, alarmManager, reqCode)
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        intent: Intent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        scheduleAlarm(context, alarmManager, requestCode, triggerAtMillis, intent)
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerAtMillis: Long,
        intent: Intent
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (_: SecurityException) {
        }
    }

    private fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        cancelAlarm(context, alarmManager, requestCode)
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
