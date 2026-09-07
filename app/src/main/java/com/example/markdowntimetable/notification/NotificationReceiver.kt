package com.example.markdowntimetable.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.markdowntimetable.parser.MarkdownParser
import com.example.markdowntimetable.persistence.TaskPersistence

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val timetableId = intent?.getStringExtra(EXTRA_TIMETABLE_ID)
        Log.d(TAG, "NotificationReceiver broadcast received for timetableId: $timetableId")

        val persistence = TaskPersistence(context)
        val timetables = if (timetableId != null) {
            listOfNotNull(persistence.getTimetableById(timetableId))
        } else {
            persistence.getAllTimetables().filter { it.isReminderEnabled }
        }

        timetables.forEach { item ->
            if (item.isReminderEnabled) {
                val days = MarkdownParser.parse(item.content)
                val hasIncomplete = days.any { day ->
                    day.topics.any { topic ->
                        topic.subtopics.any { subtopic ->
                            !persistence.isTaskDone(
                                item.id,
                                day.title,
                                topic.title,
                                subtopic.title,
                                subtopic.isDone.value
                            )
                        }
                    }
                }

                if (hasIncomplete) {
                    ReminderWorker.sendNotification(
                        context = context,
                        title = item.title,
                        content = "Incomplete tasks remaining in ${item.title}. Tap to view!",
                        timetableId = item.id,
                        snoozeMinutes = item.snoozeMinutes
                    )
                }

                // Reschedule for next day
                ReminderScheduler.scheduleDailyReminder(
                    context,
                    item.id,
                    item.reminderHour,
                    item.reminderMinute
                )
            }
        }
    }

    companion object {
        private const val TAG = "NotificationReceiver"
        const val EXTRA_TIMETABLE_ID = "extra_timetable_id"
    }
}
