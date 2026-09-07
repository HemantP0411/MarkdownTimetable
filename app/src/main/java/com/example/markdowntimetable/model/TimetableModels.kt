package com.example.markdowntimetable.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Data model for a Subtopic (Bullet List Item in Markdown).
 * Uses Compose MutableState<Boolean> for [isDone] so checkbox changes trigger UI updates.
 */
data class Subtopic(
    val title: String,
    val isDone: MutableState<Boolean> = mutableStateOf(false)
)

/**
 * Data model for a Topic (Heading Level 2 in Markdown).
 * Automatically considered done when all its subtopics are done.
 */
data class Topic(
    val title: String,
    val subtopics: List<Subtopic>
) {
    /**
     * Computed property: Returns true if subtopics list is not empty and all subtopics are marked done.
     */
    val isDone: Boolean
        get() = subtopics.isNotEmpty() && subtopics.all { it.isDone.value }
}

/**
 * Data model for a Day (Heading Level 1 in Markdown).
 * Automatically considered done when all its topics are done.
 */
data class Day(
    val title: String,
    val topics: List<Topic>
) {
    /**
     * Computed property: Returns true if topics list is not empty and all topics are marked done.
     */
    val isDone: Boolean
        get() = topics.isNotEmpty() && topics.all { it.isDone }
}
