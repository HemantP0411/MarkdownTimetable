package com.example.markdowntimetable.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val timetableId = intent?.getStringExtra(EXTRA_TIMETABLE_ID) ?: return
        val timetableTitle = intent.getStringExtra(EXTRA_TIMETABLE_TITLE) ?: "Timetable"
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 15)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        Log.d(TAG, "Snooze clicked for $timetableTitle ($timetableId), snoozing for $snoozeMinutes mins")

        // Cancel the current active notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        // Schedule snooze alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val targetIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TIMETABLE_ID, timetableId)
            putExtra(EXTRA_TIMETABLE_TITLE, timetableTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timetableId.hashCode() + SNOOZE_OFFSET,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)
        Log.d(TAG, "Scheduling snooze notification at millis: $triggerTime")

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        Toast.makeText(
            context,
            "Snoozed $timetableTitle for $snoozeMinutes mins",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val TAG = "SnoozeReceiver"
        private const val SNOOZE_OFFSET = 99999
        const val EXTRA_TIMETABLE_ID = "extra_timetable_id"
        const val EXTRA_TIMETABLE_TITLE = "extra_timetable_title"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
