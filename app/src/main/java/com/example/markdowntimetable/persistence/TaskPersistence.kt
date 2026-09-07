package com.example.markdowntimetable.persistence

import android.content.Context
import android.content.SharedPreferences
import com.example.markdowntimetable.model.SavedTimetable
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TaskPersistence(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var hasCreatedInitialSample: Boolean
        get() = prefs.getBoolean(KEY_HAS_INITIAL_SAMPLE, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_INITIAL_SAMPLE, value).apply()

    fun getAllTimetables(): List<SavedTimetable> {
        val jsonString = prefs.getString(KEY_TIMETABLES, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<SavedTimetable>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(jsonToTimetable(obj))
            }
            list.sortedWith(
                compareByDescending<SavedTimetable> { it.isFavorite }
                    .thenBy { it.orderIndex }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getTimetableById(id: String): SavedTimetable? {
        return getAllTimetables().find { it.id == id }
    }

    fun saveTimetable(timetable: SavedTimetable) {
        val list = getAllTimetables().toMutableList()
        val index = list.indexOfFirst { it.id == timetable.id }
        if (index != -1) {
            list[index] = timetable
        } else {
            list.add(timetable)
        }

        saveAllTimetables(list)
    }

    fun saveAllTimetables(list: List<SavedTimetable>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(timetableToJson(it)) }
        prefs.edit().putString(KEY_TIMETABLES, jsonArray.toString()).apply()
    }

    fun deleteTimetable(id: String) {
        val list = getAllTimetables().filter { it.id != id }
        saveAllTimetables(list)
    }

    fun saveTaskDoneState(
        timetableId: String,
        dayTitle: String,
        topicTitle: String,
        subtopicTitle: String,
        isDone: Boolean
    ) {
        val key = buildTaskKey(timetableId, dayTitle, topicTitle, subtopicTitle)
        prefs.edit().putBoolean(key, isDone).apply()
    }

    fun isTaskDone(
        timetableId: String,
        dayTitle: String,
        topicTitle: String,
        subtopicTitle: String,
        defaultDone: Boolean
    ): Boolean {
        val key = buildTaskKey(timetableId, dayTitle, topicTitle, subtopicTitle)
        return prefs.getBoolean(key, defaultDone)
    }

    private fun buildTaskKey(
        timetableId: String,
        dayTitle: String,
        topicTitle: String,
        subtopicTitle: String
    ): String {
        return "task_done|${timetableId}|${dayTitle.trim()}|${topicTitle.trim()}|${subtopicTitle.trim()}"
    }

    private fun timetableToJson(item: SavedTimetable): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("content", item.content)
            put("isFavorite", item.isFavorite)
            put("orderIndex", item.orderIndex)
            put("isReminderEnabled", item.isReminderEnabled)
            put("reminderHour", item.reminderHour)
            put("reminderMinute", item.reminderMinute)
            put("snoozeMinutes", item.snoozeMinutes)
        }
    }

    private fun jsonToTimetable(obj: JSONObject): SavedTimetable {
        return SavedTimetable(
            id = obj.optString("id", UUID.randomUUID().toString()),
            title = obj.optString("title", "Timetable.md"),
            content = obj.optString("content", ""),
            isFavorite = obj.optBoolean("isFavorite", false),
            orderIndex = obj.optInt("orderIndex", 0),
            isReminderEnabled = obj.optBoolean("isReminderEnabled", true),
            reminderHour = obj.optInt("reminderHour", 20),
            reminderMinute = obj.optInt("reminderMinute", 0),
            snoozeMinutes = obj.optInt("snoozeMinutes", 15)
        )
    }

    companion object {
        private const val PREF_NAME = "timetable_multi_persistence"
        private const val KEY_TIMETABLES = "saved_timetables_list"
        private const val KEY_HAS_INITIAL_SAMPLE = "has_created_initial_sample"
    }
}
