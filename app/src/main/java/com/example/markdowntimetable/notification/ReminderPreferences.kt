package com.example.markdowntimetable.notification

import android.content.Context
import android.content.SharedPreferences

class ReminderPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var reminderHour: Int
        get() = prefs.getInt(KEY_HOUR, 20) // Default 8:00 PM
        set(value) = prefs.edit().putInt(KEY_HOUR, value).apply()

    var reminderMinute: Int
        get() = prefs.getInt(KEY_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_MINUTE, value).apply()

    var hasUncompletedTasks: Boolean
        get() = prefs.getBoolean(KEY_HAS_UNCOMPLETED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAS_UNCOMPLETED, value).apply()

    companion object {
        private const val PREF_NAME = "timetable_reminder_prefs"
        private const val KEY_ENABLED = "reminder_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
        private const val KEY_HAS_UNCOMPLETED = "has_uncompleted_tasks"
    }
}
