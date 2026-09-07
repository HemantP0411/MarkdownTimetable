package com.example.markdowntimetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.markdowntimetable.ui.TimetableScreen
import com.example.markdowntimetable.ui.theme.MarkdownTimetableTheme
import com.example.markdowntimetable.viewmodel.MarkdownViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MarkdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkdownTimetableTheme {
                TimetableScreen(viewModel = viewModel)
            }
        }
    }
}
