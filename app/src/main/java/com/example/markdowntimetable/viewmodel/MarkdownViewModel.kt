package com.example.markdowntimetable.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.markdowntimetable.model.Day
import com.example.markdowntimetable.model.SavedTimetable
import com.example.markdowntimetable.model.Subtopic
import com.example.markdowntimetable.notification.ReminderScheduler
import com.example.markdowntimetable.parser.MarkdownParser
import com.example.markdowntimetable.persistence.TaskPersistence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MarkdownViewModel : ViewModel() {

    private val _timetables = MutableStateFlow<List<SavedTimetable>>(emptyList())
    val timetables: StateFlow<List<SavedTimetable>> = _timetables.asStateFlow()

    private val _activeTimetable = MutableStateFlow<SavedTimetable?>(null)
    val activeTimetable: StateFlow<SavedTimetable?> = _activeTimetable.asStateFlow()

    private val _days = MutableStateFlow<List<Day>>(emptyList())
    val days: StateFlow<List<Day>> = _days.asStateFlow()

    private val _newlyAddedId = MutableStateFlow<String?>(null)
    val newlyAddedId: StateFlow<String?> = _newlyAddedId.asStateFlow()

    fun initialize(context: Context) {
        loadAllTimetables(context)
    }

    fun loadAllTimetables(context: Context) {
        val persistence = TaskPersistence(context)
        var list = persistence.getAllTimetables()

        if (list.isEmpty() && !persistence.hasCreatedInitialSample) {
            persistence.hasCreatedInitialSample = true
            val sampleItem = SavedTimetable(
                title = "Sample_Timetable.md",
                content = getSampleMarkdown(),
                isFavorite = false,
                orderIndex = 0,
                isReminderEnabled = true,
                reminderHour = 20,
                reminderMinute = 0,
                snoozeMinutes = 15
            )
            persistence.saveTimetable(sampleItem)
            if (sampleItem.isReminderEnabled) {
                ReminderScheduler.scheduleDailyReminder(
                    context,
                    sampleItem.id,
                    sampleItem.reminderHour,
                    sampleItem.reminderMinute
                )
            }
            list = listOf(sampleItem)
        }

        _timetables.value = list
    }

    fun selectTimetable(context: Context, timetable: SavedTimetable) {
        _activeTimetable.value = timetable
        val parsedDays = MarkdownParser.parse(timetable.content)

        val persistence = TaskPersistence(context)
        parsedDays.forEach { day ->
            day.topics.forEach { topic ->
                topic.subtopics.forEach { subtopic ->
                    val savedDone = persistence.isTaskDone(
                        timetable.id,
                        day.title,
                        topic.title,
                        subtopic.title,
                        subtopic.isDone.value
                    )
                    subtopic.isDone.value = savedDone
                }
            }
        }

        _days.value = parsedDays
    }

    fun navigateToHomeScreen() {
        _activeTimetable.value = null
        _days.value = emptyList()
    }

    fun addTimetable(
        context: Context,
        title: String,
        content: String
    ) {
        val persistence = TaskPersistence(context)
        val list = persistence.getAllTimetables()
        val newItem = SavedTimetable(
            title = title.ifBlank { "Custom_Timetable.md" },
            content = content,
            isFavorite = false,
            orderIndex = list.size,
            isReminderEnabled = true,
            reminderHour = 20,
            reminderMinute = 0,
            snoozeMinutes = 15
        )
        persistence.saveTimetable(newItem)
        ReminderScheduler.scheduleDailyReminder(context, newItem.id, newItem.reminderHour, newItem.reminderMinute)

        _newlyAddedId.value = newItem.id
        loadAllTimetables(context)
    }

    fun clearNewlyAddedId() {
        _newlyAddedId.value = null
    }

    fun deleteTimetable(context: Context, id: String) {
        val persistence = TaskPersistence(context)
        persistence.deleteTimetable(id)
        ReminderScheduler.cancelReminder(context, id)

        if (_activeTimetable.value?.id == id) {
            navigateToHomeScreen()
        }
        loadAllTimetables(context)
    }

    fun toggleFavorite(context: Context, timetableId: String) {
        val persistence = TaskPersistence(context)
        val item = persistence.getTimetableById(timetableId) ?: return
        val updated = item.copy(isFavorite = !item.isFavorite, orderIndex = 0)
        persistence.saveTimetable(updated)
        normalizeOrderIndexes(context, updated.isFavorite)
        loadAllTimetables(context)
    }

    fun renameTimetable(context: Context, timetableId: String, newTitle: String) {
        val persistence = TaskPersistence(context)
        val item = persistence.getTimetableById(timetableId) ?: return
        val cleanTitle = newTitle.ifBlank { "Timetable.md" }
        val updated = item.copy(title = cleanTitle)
        persistence.saveTimetable(updated)

        if (_activeTimetable.value?.id == timetableId) {
            _activeTimetable.value = updated
        }

        loadAllTimetables(context)
    }

    fun moveTimetableUp(context: Context, item: SavedTimetable) {
        val persistence = TaskPersistence(context)
        val list = persistence.getAllTimetables()
        val section = list.filter { it.isFavorite == item.isFavorite }.sortedBy { it.orderIndex }.toMutableList()
        val currentIndex = section.indexOfFirst { it.id == item.id }

        if (currentIndex > 0) {
            val prevItem = section[currentIndex - 1]
            section[currentIndex - 1] = item
            section[currentIndex] = prevItem

            section.forEachIndexed { idx, t ->
                persistence.saveTimetable(t.copy(orderIndex = idx))
            }
            loadAllTimetables(context)
        }
    }

    fun moveTimetableDown(context: Context, item: SavedTimetable) {
        val persistence = TaskPersistence(context)
        val list = persistence.getAllTimetables()
        val section = list.filter { it.isFavorite == item.isFavorite }.sortedBy { it.orderIndex }.toMutableList()
        val currentIndex = section.indexOfFirst { it.id == item.id }

        if (currentIndex != -1 && currentIndex < section.size - 1) {
            val nextItem = section[currentIndex + 1]
            section[currentIndex + 1] = item
            section[currentIndex] = nextItem

            section.forEachIndexed { idx, t ->
                persistence.saveTimetable(t.copy(orderIndex = idx))
            }
            loadAllTimetables(context)
        }
    }

    private fun normalizeOrderIndexes(context: Context, isFavorite: Boolean) {
        val persistence = TaskPersistence(context)
        val all = persistence.getAllTimetables()
        val section = all.filter { it.isFavorite == isFavorite }.sortedBy { it.orderIndex }
        section.forEachIndexed { idx, item ->
            val updated = item.copy(orderIndex = idx)
            persistence.saveTimetable(updated)
        }
    }

    fun updateTimetableReminderSettings(
        context: Context,
        timetableId: String,
        isReminderEnabled: Boolean,
        hour: Int,
        minute: Int,
        snoozeMinutes: Int
    ) {
        val persistence = TaskPersistence(context)
        val currentItem = persistence.getTimetableById(timetableId) ?: return
        val updatedItem = currentItem.copy(
            isReminderEnabled = isReminderEnabled,
            reminderHour = hour,
            reminderMinute = minute,
            snoozeMinutes = snoozeMinutes
        )
        persistence.saveTimetable(updatedItem)

        if (updatedItem.id == _activeTimetable.value?.id) {
            _activeTimetable.value = updatedItem
        }

        if (isReminderEnabled) {
            ReminderScheduler.scheduleDailyReminder(context, updatedItem.id, hour, minute)
        } else {
            ReminderScheduler.cancelReminder(context, updatedItem.id)
        }

        loadAllTimetables(context)
    }

    fun onSubtopicToggled(
        context: Context,
        dayTitle: String,
        topicTitle: String,
        subtopic: Subtopic,
        isDone: Boolean
    ) {
        val currentActive = _activeTimetable.value ?: return
        subtopic.isDone.value = isDone

        val persistence = TaskPersistence(context)
        persistence.saveTaskDoneState(
            currentActive.id,
            dayTitle,
            topicTitle,
            subtopic.title,
            isDone
        )
    }

    private fun getSampleMarkdown(): String {
        return """
            # Day 1: Kotlin & Compose Core
            ## Topic 1: Environment & Project Setup
            * Install Android Studio and Java Development Kit
            * Configure build.gradle dependencies
            * Add org.commonmark:commonmark library

            ## Topic 2: Data Models & Reactivity
            * Create Subtopic data class with MutableState<Boolean>
            * Add computed isDone property to Topic
            * Add computed isDone property to Day

            # Day 2: Parser & UI Components
            ## Topic 1: Markdown Parsing
            * Parse Heading level 1 into Day
            * Parse Heading level 2 into Topic
            * Parse Bullet List items into Subtopics

            ## Topic 2: Expandable Compose Layout
            * Implement ActivityResultContracts.GetContent file picker
            * Build LazyColumn with expandable Day cards
            * Add checkmarks and strikethrough for cascading done states
        """.trimIndent()
    }
}
