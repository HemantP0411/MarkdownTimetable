package com.example.markdowntimetable.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.markdowntimetable.persistence.TaskPersistence

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val persistence = TaskPersistence(context)
            val timetables = persistence.getAllTimetables()

            timetables.forEach { item ->
                if (item.isReminderEnabled) {
                    ReminderScheduler.scheduleDailyReminder(
                        context,
                        item.id,
                        item.reminderHour,
                        item.reminderMinute
                    )
                }
            }
        }
    }
}
