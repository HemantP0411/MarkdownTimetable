package com.example.markdowntimetable.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.markdowntimetable.MainActivity
import com.example.markdowntimetable.R

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = ReminderPreferences(context)
        if (prefs.isReminderEnabled && prefs.hasUncompletedTasks) {
            sendNotification(
                context,
                "Incomplete Timetable Tasks!",
                "You still have pending tasks/topics remaining for today. Tap to check them off!"
            )
        }
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "timetable_reminder_channel_v2"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.yoooooooo}")
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Timetable Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifies when today's timetable tasks or topics remain incomplete"
                    setSound(soundUri, audioAttributes)
                    enableVibration(true)
                    enableLights(true)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun sendNotification(
            context: Context,
            title: String,
            content: String,
            timetableId: String = "default",
            snoozeMinutes: Int = 15
        ) {
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_timetable_id", timetableId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                timetableId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Snooze Intent & Action
            val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
                putExtra(SnoozeReceiver.EXTRA_TIMETABLE_ID, timetableId)
                putExtra(SnoozeReceiver.EXTRA_TIMETABLE_TITLE, title)
                putExtra(SnoozeReceiver.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
                putExtra(SnoozeReceiver.EXTRA_NOTIFICATION_ID, timetableId.hashCode())
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                timetableId.hashCode() + 5000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val snoozeAction = NotificationCompat.Action.Builder(
                R.drawable.ic_app_icon,
                "Snooze (${snoozeMinutes}m)",
                snoozePendingIntent
            ).build()

            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.yoooooooo}")
            val largeIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.ic_app_icon)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setLargeIcon(largeIconBitmap)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .setContentIntent(pendingIntent)
                .addAction(snoozeAction)
                .setAutoCancel(true)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(timetableId.hashCode(), builder.build())
        }

        private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
            return try {
                val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(128),
                    drawable.intrinsicHeight.coerceAtLeast(128),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
}
