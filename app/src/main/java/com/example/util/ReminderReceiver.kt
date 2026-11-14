package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 7001)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Pengingat TaskFlow"
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""

        when (type) {
            TYPE_POMODORO_DONE -> {
                NotificationHelper.showPomodoroFinishedNotification(context, title, body)
                val playSound = intent.getBooleanExtra(EXTRA_PLAY_SOUND, true)
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        GentleChimePlayer.startLongAlarm(context.applicationContext, playSound)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            TYPE_TASK_DEADLINE,
            TYPE_SCHEDULE_REMINDER -> {
                NotificationHelper.showDeadlineHeadsUpNotification(
                    context = context,
                    notificationId = notificationId,
                    title = title,
                    body = body
                )
            }
        }
    }

    companion object {
        const val EXTRA_TYPE = "extra_reminder_type"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_PLAY_SOUND = "extra_play_sound"

        const val TYPE_POMODORO_DONE = "POMODORO_DONE"
        const val TYPE_TASK_DEADLINE = "TASK_DEADLINE"
        const val TYPE_SCHEDULE_REMINDER = "SCHEDULE_REMINDER"
    }
}
