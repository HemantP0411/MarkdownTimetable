package com.example.markdowntimetable.parser

import androidx.compose.runtime.mutableStateOf
import com.example.markdowntimetable.model.Day
import com.example.markdowntimetable.model.Subtopic
import com.example.markdowntimetable.model.Topic
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text
import org.commonmark.parser.Parser

object MarkdownParser {

    private class DayBuilder(val title: String) {
        val topics = mutableListOf<TopicBuilder>()
    }

    private class TopicBuilder(val title: String) {
        val subtopics = mutableListOf<Subtopic>()
    }

    /**
     * Parses a raw Markdown string into a list of [Day] objects.
     * Heading Level 1 -> Day
     * Heading Level 2 -> Topic
     * Bullet List items -> Subtopics
     */
    fun parse(markdownText: String): List<Day> {
        // Strip citation markers like [cite: 1] BEFORE CommonMark AST parsing
        val sanitizedMarkdown = markdownText.replace(Regex("\\[cite:\\s*\\d+]"), "")

        val parser = Parser.builder().build()
        val document = parser.parse(sanitizedMarkdown)

        val dayBuilders = mutableListOf<DayBuilder>()
        var currentDayBuilder: DayBuilder? = null
        var currentTopicBuilder: TopicBuilder? = null

        fun ensureCurrentDay(): DayBuilder {
            return currentDayBuilder ?: DayBuilder("Day 1").also {
                dayBuilders.add(it)
                currentDayBuilder = it
            }
        }

        fun ensureCurrentTopic(): TopicBuilder {
            val day = ensureCurrentDay()
            return currentTopicBuilder ?: TopicBuilder("General Tasks").also {
                day.topics.add(it)
                currentTopicBuilder = it
            }
        }

        fun processNode(node: Node) {
            when (node) {
                is Heading -> {
                    val titleText = extractText(node)
                    if (titleText.isNotEmpty()) {
                        when (node.level) {
                            1 -> {
                                val newDay = DayBuilder(titleText)
                                dayBuilders.add(newDay)
                                currentDayBuilder = newDay
                                currentTopicBuilder = null
                            }
                            2 -> {
                                val day = ensureCurrentDay()
                                val newTopic = TopicBuilder(titleText)
                                day.topics.add(newTopic)
                                currentTopicBuilder = newTopic
                            }
                            else -> {
                                val day = ensureCurrentDay()
                                val newTopic = TopicBuilder(titleText)
                                day.topics.add(newTopic)
                                currentTopicBuilder = newTopic
                            }
                        }
                    }
                }
                is BulletList, is OrderedList -> {
                    var listItem = node.firstChild
                    while (listItem != null) {
                        if (listItem is ListItem) {
                            val rawText = extractText(listItem)
                            if (rawText.isNotEmpty()) {
                                val topic = ensureCurrentTopic()
                                val (cleanText, initialDone) = parseCheckmark(rawText)
                                topic.subtopics.add(
                                    Subtopic(
                                        title = cleanText,
                                        isDone = mutableStateOf(initialDone)
                                    )
                                )
                            }
                        }
                        listItem = listItem.next
                    }
                }
                else -> {
                    var child = node.firstChild
                    while (child != null) {
                        processNode(child)
                        child = child.next
                    }
                }
            }
        }

        var rootChild = document.firstChild
        while (rootChild != null) {
            processNode(rootChild)
            rootChild = rootChild.next
        }

        return dayBuilders.map { dayBuilder ->
            Day(
                title = dayBuilder.title,
                topics = dayBuilder.topics.map { topicBuilder ->
                    Topic(
                        title = topicBuilder.title,
                        subtopics = topicBuilder.subtopics.toList()
                    )
                }
            )
        }
    }

    private fun parseCheckmark(text: String): Pair<String, Boolean> {
        val trimmed = text.trim()
        return when {
            trimmed.startsWith("[x] ", ignoreCase = true) -> {
                trimmed.substring(4).trim() to true
            }
            trimmed.startsWith("[ ] ") -> {
                trimmed.substring(4).trim() to false
            }
            else -> {
                trimmed to false
            }
        }
    }

    private fun extractText(node: Node): String {
        val sb = StringBuilder()
        fun walk(n: Node) {
            when (n) {
                is Text -> sb.append(n.literal)
                is Code -> sb.append(n.literal)
                is Link -> {
                    val dest = n.destination
                    if (!dest.isNullOrBlank()) {
                        sb.append("[")
                        var child = n.firstChild
                        while (child != null) {
                            walk(child)
                            child = child.next
                        }
                        sb.append("](").append(dest).append(")")
                    } else {
                        var child = n.firstChild
                        while (child != null) {
                            walk(child)
                            child = child.next
                        }
                    }
                }
                is SoftLineBreak, is HardLineBreak -> sb.append(" ")
                else -> {
                    var child = n.firstChild
                    while (child != null) {
                        walk(child)
                        child = child.next
                    }
                }
            }
        }
        walk(node)
        return sb.toString().trim()
    }
}
