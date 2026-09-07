package com.example.markdowntimetable

import androidx.compose.runtime.mutableStateOf
import com.example.markdowntimetable.model.Day
import com.example.markdowntimetable.model.Subtopic
import com.example.markdowntimetable.model.Topic
import com.example.markdowntimetable.parser.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTimetableTest {

    @Test
    fun testComputedCompletionState() {
        val subtopic1 = Subtopic("Task 1", mutableStateOf(false))
        val subtopic2 = Subtopic("Task 2", mutableStateOf(false))
        val topic = Topic("Topic 1", listOf(subtopic1, subtopic2))
        val day = Day("Day 1", listOf(topic))

        // Initially not done
        assertFalse(topic.isDone)
        assertFalse(day.isDone)

        // Mark task 1 done
        subtopic1.isDone.value = true
        assertFalse(topic.isDone)
        assertFalse(day.isDone)

        // Mark task 2 done
        subtopic2.isDone.value = true
        assertTrue(topic.isDone)
        assertTrue(day.isDone)

        // Uncheck task 1
        subtopic1.isDone.value = false
        assertFalse(topic.isDone)
        assertFalse(day.isDone)
    }

    @Test
    fun testMarkdownParser() {
        val markdown = """
            # Day 1: Basics
            ## Topic 1: Setup
            * Task 1
            * Task 2

            # Day 2: Advanced
            ## Topic 2: Jetpack Compose
            * Task 3
        """.trimIndent()

        val days = MarkdownParser.parse(markdown)

        assertEquals(2, days.size)

        // Day 1
        val day1 = days[0]
        assertEquals("Day 1: Basics", day1.title)
        assertEquals(1, day1.topics.size)
        val topic1 = day1.topics[0]
        assertEquals("Topic 1: Setup", topic1.title)
        assertEquals(2, topic1.subtopics.size)
        assertEquals("Task 1", topic1.subtopics[0].title)
        assertEquals("Task 2", topic1.subtopics[1].title)

        // Day 2
        val day2 = days[1]
        assertEquals("Day 2: Advanced", day2.title)
        assertEquals(1, day2.topics.size)
        val topic2 = day2.topics[0]
        assertEquals("Topic 2: Jetpack Compose", topic2.title)
        assertEquals(1, topic2.subtopics.size)
        assertEquals("Task 3", topic2.subtopics[0].title)
    }

    @Test
    fun testTaskCheckboxParsing() {
        val markdown = """
            # Day 1: Testing Checkboxes
            ## Topic 1: Task List
            * [x] Completed task
            * [ ] Pending task
            * Plain task
        """.trimIndent()

        val days = MarkdownParser.parse(markdown)
        assertEquals(1, days.size)
        val subtopics = days[0].topics[0].subtopics
        assertEquals(3, subtopics.size)

        assertEquals("Completed task", subtopics[0].title)
        assertTrue(subtopics[0].isDone.value)

        assertEquals("Pending task", subtopics[1].title)
        assertFalse(subtopics[1].isDone.value)

        assertEquals("Plain task", subtopics[2].title)
        assertFalse(subtopics[2].isDone.value)
    }

    @Test
    fun testMarkdownLinkParsing() {
        val markdown = """
            # Day 1
            ## DSA Problems
            * [ ] Easy: Two Sum - [Link](https://leetcode.com/problems/two-sum/)[cite: 1]
        """.trimIndent()

        val days = MarkdownParser.parse(markdown)
        assertEquals(1, days.size)
        val subtopicTitle = days[0].topics[0].subtopics[0].title
        assertTrue(subtopicTitle.contains("[Link](https://leetcode.com/problems/two-sum/)"))
    }
}
