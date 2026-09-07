package com.example.markdowntimetable.model

import java.util.UUID

data class SavedTimetable(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val isFavorite: Boolean = false,
    val orderIndex: Int = 0,
    val isReminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val snoozeMinutes: Int = 15
)
