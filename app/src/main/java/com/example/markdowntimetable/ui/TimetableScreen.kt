package com.example.markdowntimetable.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.markdowntimetable.model.Day
import com.example.markdowntimetable.model.SavedTimetable
import com.example.markdowntimetable.model.Subtopic
import com.example.markdowntimetable.model.Topic
import com.example.markdowntimetable.notification.ReminderWorker
import com.example.markdowntimetable.parser.MarkdownParser
import com.example.markdowntimetable.persistence.TaskPersistence
import com.example.markdowntimetable.viewmodel.MarkdownViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TimetableScreen(viewModel: MarkdownViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    val timetables by viewModel.timetables.collectAsState()
    val activeTimetable by viewModel.activeTimetable.collectAsState()
    val days by viewModel.days.collectAsState()
    val newlyAddedId by viewModel.newlyAddedId.collectAsState()

    var showAddChoiceDialog by remember { mutableStateOf(false) }
    var showPasteTextDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val name = getFileNameFromUri(context, selectedUri) ?: "Markdown File.md"
                context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    viewModel.addTimetable(context, name, content)
                    Toast.makeText(context, "Added $name", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (activeTimetable == null) {
        BackHandler {
            showExitDialog = true
        }

        HomeScreen(
            viewModel = viewModel,
            timetables = timetables,
            newlyAddedId = newlyAddedId,
            onSelectTimetable = { item -> viewModel.selectTimetable(context, item) },
            onDeleteTimetable = { item -> viewModel.deleteTimetable(context, item.id) },
            onToggleFavorite = { item -> viewModel.toggleFavorite(context, item.id) },
            onRenameTimetable = { item, newTitle -> viewModel.renameTimetable(context, item.id, newTitle) },
            onMoveUp = { item -> viewModel.moveTimetableUp(context, item) },
            onMoveDown = { item -> viewModel.moveTimetableDown(context, item) },
            onAddClick = { showAddChoiceDialog = true }
        )
    } else {
        BackHandler {
            viewModel.navigateToHomeScreen()
        }

        DetailScreen(
            viewModel = viewModel,
            activeTimetable = activeTimetable!!,
            days = days,
            onBack = { viewModel.navigateToHomeScreen() },
            onOpenReminderSettings = { showReminderDialog = true }
        )
    }

    if (showExitDialog) {
        val activity = context as? ComponentActivity
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Markdown Timetable?") },
            text = { Text("Are you sure you want to exit the application?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        activity?.finish()
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddChoiceDialog) {
        AddSourceChoiceDialog(
            onPickFile = { filePickerLauncher.launch("*/*") },
            onPasteText = { showPasteTextDialog = true },
            onDismiss = { showAddChoiceDialog = false }
        )
    }

    if (showPasteTextDialog) {
        PasteMarkdownDialog(
            onConfirm = { text, name ->
                viewModel.addTimetable(context, name, text)
                Toast.makeText(context, "Timetable added from text", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPasteTextDialog = false }
        )
    }

    if (showReminderDialog && activeTimetable != null) {
        ReminderSettingsDialog(
            activeTimetable = activeTimetable!!,
            onSave = { isEnabled, hour, minute, snoozeMins ->
                viewModel.updateTimetableReminderSettings(
                    context,
                    activeTimetable!!.id,
                    isEnabled,
                    hour,
                    minute,
                    snoozeMins
                )
            },
            onDismiss = { showReminderDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MarkdownViewModel,
    timetables: List<SavedTimetable>,
    newlyAddedId: String?,
    onSelectTimetable: (SavedTimetable) -> Unit,
    onDeleteTimetable: (SavedTimetable) -> Unit,
    onToggleFavorite: (SavedTimetable) -> Unit,
    onRenameTimetable: (SavedTimetable, String) -> Unit,
    onMoveUp: (SavedTimetable) -> Unit,
    onMoveDown: (SavedTimetable) -> Unit,
    onAddClick: () -> Unit
) {
    var itemToDelete by remember { mutableStateOf<SavedTimetable?>(null) }
    var itemToRename by remember { mutableStateOf<SavedTimetable?>(null) }

    val favorites = remember(timetables) { timetables.filter { it.isFavorite } }
    val nonFavorites = remember(timetables) { timetables.filter { !it.isFavorite } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Markdown Timetables",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Timetable"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (timetables.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Timetables Available",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "To add a timetable:\n1. Tap the '+' button at bottom-right.\n2. Pick a .md file or paste raw Markdown text.\n3. Track your daily topics and tasks!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (favorites.isNotEmpty()) {
                        item(key = "header_favorites") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .animateItem()
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FAVORITES",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB300)
                                )
                            }
                        }

                        itemsIndexed(favorites, key = { _, item -> item.id }) { index, timetable ->
                            TimetableCard(
                                timetable = timetable,
                                canMoveUp = index > 0,
                                canMoveDown = index < favorites.size - 1,
                                isNewlyAdded = (timetable.id == newlyAddedId),
                                modifier = Modifier.animateItem(),
                                onClearHighlight = { viewModel.clearNewlyAddedId() },
                                onMoveUp = { onMoveUp(timetable) },
                                onMoveDown = { onMoveDown(timetable) },
                                onClick = { onSelectTimetable(timetable) },
                                onFavoriteToggle = { onToggleFavorite(timetable) },
                                onRename = { itemToRename = timetable },
                                onDelete = { itemToDelete = timetable }
                            )
                        }

                        if (nonFavorites.isNotEmpty()) {
                            item(key = "header_all") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ALL TIMETABLES",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    itemsIndexed(nonFavorites, key = { _, item -> item.id }) { index, timetable ->
                        TimetableCard(
                            timetable = timetable,
                            canMoveUp = index > 0,
                            canMoveDown = index < nonFavorites.size - 1,
                            isNewlyAdded = (timetable.id == newlyAddedId),
                            modifier = Modifier.animateItem(),
                            onClearHighlight = { viewModel.clearNewlyAddedId() },
                            onMoveUp = { onMoveUp(timetable) },
                            onMoveDown = { onMoveDown(timetable) },
                            onClick = { onSelectTimetable(timetable) },
                            onFavoriteToggle = { onToggleFavorite(timetable) },
                            onRename = { itemToRename = timetable },
                            onDelete = { itemToDelete = timetable }
                        )
                    }
                }
            }
        }
    }

    if (itemToRename != null) {
        RenameTimetableDialog(
            currentTitle = itemToRename!!.title,
            onConfirm = { newTitle ->
                val target = itemToRename
                itemToRename = null
                target?.let { onRenameTimetable(it, newTitle) }
            },
            onDismiss = { itemToRename = null }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Timetable?") },
            text = { Text("Are you sure you want to delete '${itemToDelete?.title}'? Saved task states for this timetable will also be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        val target = itemToDelete
                        itemToDelete = null
                        target?.let { onDeleteTimetable(it) }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TimetableCard(
    timetable: SavedTimetable,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isNewlyAdded: Boolean,
    modifier: Modifier = Modifier,
    onClearHighlight: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val days = remember(timetable.content) { MarkdownParser.parse(timetable.content) }
    val persistence = remember { TaskPersistence(context) }

    var animateHighlight by remember(timetable.id) { mutableStateOf(isNewlyAdded) }

    LaunchedEffect(animateHighlight) {
        if (animateHighlight) {
            delay(2500)
            animateHighlight = false
            onClearHighlight()
        }
    }

    val cardBgColor by animateColorAsState(
        targetValue = when {
            animateHighlight -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
        label = "CardHighlightShift"
    )

    val completedDays = remember(days, timetable) {
        days.count { day ->
            day.topics.all { topic ->
                topic.subtopics.all { subtopic ->
                    persistence.isTaskDone(
                        timetable.id,
                        day.title,
                        topic.title,
                        subtopic.title,
                        subtopic.isDone.value
                    )
                }
            }
        }
    }

    val progress = if (days.isNotEmpty()) completedDays.toFloat() / days.size.toFloat() else 0f

    val timeFormatted = remember(timetable.reminderHour, timetable.reminderMinute) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timetable.reminderHour)
            set(Calendar.MINUTE, timetable.reminderMinute)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (timetable.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite Timetable",
                            tint = if (timetable.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onRename() }
                    ) {
                        Text(
                            text = timetable.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onRename, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename Timetable",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canMoveUp) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Move Up",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (canMoveDown) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Move Down",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Timetable",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$completedDays of ${days.size} Days Completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF2E7D32),
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (timetable.isReminderEnabled) "Reminder: $timeFormatted (Snooze: ${timetable.snoozeMinutes}m)" else "Reminders Disabled",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RenameTimetableDialog(
    currentTitle: String,
    onConfirm: (newTitle: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Timetable") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Timetable Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isNotEmpty()) {
                        onConfirm(title.trim())
                    }
                },
                enabled = title.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: MarkdownViewModel,
    activeTimetable: SavedTimetable,
    days: List<Day>,
    onBack: () -> Unit,
    onOpenReminderSettings: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Find the first incomplete day index
    val firstIncompleteIndex = remember(days) {
        val idx = days.indexOfFirst { !it.isDone }
        if (idx != -1) idx else 0
    }

    // Auto-scroll to the first incomplete day
    LaunchedEffect(days) {
        if (days.isNotEmpty() && firstIncompleteIndex > 0) {
            listState.animateScrollToItem(firstIncompleteIndex)
        }
    }

    // Notification permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeTimetable.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Timetables"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        onOpenReminderSettings()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Timetable Notification Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val completedDays = days.count { it.isDone }
                    Text(
                        text = "$completedDays of ${days.size} Days Completed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (days.isNotEmpty()) completedDays.toFloat() / days.size.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2E7D32),
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(days) { index, day ->
                    DayCard(
                        day = day,
                        initiallyExpanded = (index == firstIncompleteIndex),
                        onToggleTask = { topicTitle, subtopic, isDone ->
                            viewModel.onSubtopicToggled(context, day.title, topicTitle, subtopic, isDone)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddSourceChoiceDialog(
    onPickFile: () -> Unit,
    onPasteText: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Timetable") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPickFile(); onDismiss() },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text("Select .md File", fontWeight = FontWeight.Bold)
                            Text("Pick a markdown file from device storage", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPasteText(); onDismiss() },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text("Paste / Type Markdown Text", fontWeight = FontWeight.Bold)
                            Text("Input raw markdown text directly", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PasteMarkdownDialog(
    onConfirm: (text: String, fileName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("Custom_Timetable.md") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paste Markdown Text") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Timetable Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("Markdown Content") },
                    placeholder = {
                        Text(
                            "# Day 1: Title\n## Topic 1: Subtitle\n* [ ] Task 1\n* [ ] Task 2"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawText.trim().isNotEmpty()) {
                        val name = if (fileName.isBlank()) "Custom_Timetable.md" else fileName.trim()
                        onConfirm(rawText, name)
                        onDismiss()
                    }
                },
                enabled = rawText.trim().isNotEmpty()
            ) {
                Text("Load Timetable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RoundCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    checkedColor: Color = Color(0xFF2E7D32),
    uncheckedColor: Color = MaterialTheme.colorScheme.outline
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (checked) checkedColor else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (checked) checkedColor else uncheckedColor,
                shape = CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.65f)
            )
        }
    }
}

@Composable
fun DayCard(
    day: Day,
    initiallyExpanded: Boolean,
    onToggleTask: (topicTitle: String, subtopic: Subtopic, isDone: Boolean) -> Unit
) {
    var isExpanded by remember(day.title, initiallyExpanded) { mutableStateOf(initiallyExpanded) }
    val isDone = day.isDone

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Day Completed",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Column {
                        ClickableMarkdownText(
                            text = day.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                            onNonLinkClick = { isExpanded = !isExpanded }
                        )
                        val completedTopics = day.topics.count { it.isDone }
                        Text(
                            text = "$completedTopics of ${day.topics.size} Topics completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDone) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDone) {
                        Surface(
                            color = Color(0xFFC8E6C9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "DONE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse Day" else "Expand Day"
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                    day.topics.forEachIndexed { index, topic ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        TopicSection(
                            topic = topic,
                            onToggleTask = { subtopic, isDone ->
                                onToggleTask(topic.title, subtopic, isDone)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicSection(
    topic: Topic,
    onToggleTask: (subtopic: Subtopic, isDone: Boolean) -> Unit
) {
    val isDone = topic.isDone

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isDone) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Topic Completed",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                ClickableMarkdownText(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isDone) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onNonLinkClick = {}
                )
                val doneSubtopics = topic.subtopics.count { it.isDone.value }
                Text(
                    text = "($doneSubtopics/${topic.subtopics.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDone) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                topic.subtopics.forEach { subtopic ->
                    SubtopicRow(
                        subtopic = subtopic,
                        onToggleTask = onToggleTask
                    )
                }
            }
        }
    }
}

@Composable
fun SubtopicRow(
    subtopic: Subtopic,
    onToggleTask: (subtopic: Subtopic, isDone: Boolean) -> Unit
) {
    val isChecked = subtopic.isDone.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundCheckbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                onToggleTask(subtopic, checked)
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        ClickableMarkdownText(
            text = subtopic.title,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (isChecked) Color.Gray else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            onNonLinkClick = {
                val newDone = !subtopic.isDone.value
                onToggleTask(subtopic, newDone)
            }
        )
    }
}

@Composable
fun ClickableMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textDecoration: TextDecoration = TextDecoration.None,
    onNonLinkClick: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val annotatedString = remember(text, textDecoration, color, fontWeight) {
        buildMarkdownAnnotatedString(
            rawText = text,
            baseColor = color,
            baseDecoration = textDecoration,
            baseFontWeight = fontWeight
        )
    }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        style = style,
        modifier = modifier.pointerInput(annotatedString) {
            detectTapGestures { pos ->
                var handledLink = false
                layoutResult?.let { layout ->
                    val offset = layout.getOffsetForPosition(pos)
                    val urlAnnotations = annotatedString.getStringAnnotations(
                        tag = "URL",
                        start = offset,
                        end = offset
                    )
                    if (urlAnnotations.isNotEmpty()) {
                        val url = urlAnnotations.first().item
                        try {
                            uriHandler.openUri(url)
                            handledLink = true
                        } catch (_: Exception) {
                            Toast.makeText(context, "Could not open link: $url", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (!handledLink) {
                    onNonLinkClick()
                }
            }
        },
        onTextLayout = { layoutResult = it }
    )
}

private fun buildMarkdownAnnotatedString(
    rawText: String,
    baseColor: Color,
    baseDecoration: TextDecoration,
    baseFontWeight: FontWeight
): AnnotatedString {
    return buildAnnotatedString {
        val cleaned = rawText
            .replace(Regex("\\[span_\\d+\\]\\(start_span\\)"), "")
            .replace(Regex("\\[span_\\d+\\]\\(end_span\\)"), "")
            .replace(Regex("\\[cite:\\s*\\d+]"), "")
            .replace(Regex("\\(null\\)"), "")

        val mdLinkRegex = Regex("\\[([^\\]]+)]\\((https?://[^\\s)]+)\\)")
        val rawUrlRegex = Regex("(?<!\\()(https?://[^\\s)]+)")
        var lastIndex = 0

        val matches = (mdLinkRegex.findAll(cleaned) + rawUrlRegex.findAll(cleaned))
            .sortedBy { it.range.first }

        matches.forEach { match ->
            val range = match.range
            if (range.first >= lastIndex) {
                if (range.first > lastIndex) {
                    val plainPart = cleaned.substring(lastIndex, range.first)
                    withStyle(SpanStyle(color = baseColor, textDecoration = baseDecoration, fontWeight = baseFontWeight)) {
                        append(plainPart)
                    }
                }

                val isMdLink = match.groupValues.size >= 3
                val labelText = if (isMdLink) match.groupValues[1] else ""
                val url = if (isMdLink) match.groupValues[2] else match.groupValues[0]

                val linkFullDisplay = if (isMdLink && labelText.isNotBlank() && labelText != url) {
                    "$labelText ($url)"
                } else {
                    url
                }

                pushStringAnnotation(tag = "URL", annotation = url)
                withStyle(
                    SpanStyle(
                        color = Color(0xFF1976D2),
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(linkFullDisplay)
                }
                pop()

                lastIndex = range.last + match.value.length
            }
        }

        if (lastIndex < cleaned.length) {
            val remaining = cleaned.substring(lastIndex)
            withStyle(SpanStyle(color = baseColor, textDecoration = baseDecoration, fontWeight = baseFontWeight)) {
                append(remaining)
            }
        }
    }
}

@Composable
fun ReminderSettingsDialog(
    activeTimetable: SavedTimetable,
    onSave: (isEnabled: Boolean, hour: Int, minute: Int, snoozeMins: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var isEnabled by remember { mutableStateOf(activeTimetable.isReminderEnabled) }
    var hour by remember { mutableIntStateOf(activeTimetable.reminderHour) }
    var minute by remember { mutableIntStateOf(activeTimetable.reminderMinute) }
    var snoozeInputText by remember { mutableStateOf(activeTimetable.snoozeMinutes.toString()) }

    val snoozeMinutes = remember(snoozeInputText) {
        val parsed = snoozeInputText.toIntOrNull()
        if (parsed != null && parsed > 0) parsed else 15
    }

    val formattedTime = remember(hour, minute) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
    }

    val timePicker = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            hour = selectedHour
            minute = selectedMinute
        },
        hour,
        minute,
        false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder Settings: ${activeTimetable.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Reminder", fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }

                if (isEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reminder Time:", fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(onClick = { timePicker.show() }) {
                            Text(formattedTime)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Snooze (mins):", fontWeight = FontWeight.Medium)
                        }
                        OutlinedTextField(
                            value = snoozeInputText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                                    snoozeInputText = newValue
                                }
                            },
                            singleLine = true,
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        ReminderWorker.sendNotification(
                            context = context,
                            title = activeTimetable.title,
                            content = "Test reminder notification for ${activeTimetable.title}",
                            timetableId = activeTimetable.id,
                            snoozeMinutes = snoozeMinutes
                        )
                        Toast.makeText(context, "Test notification sent! (Snooze: ${snoozeMinutes}m)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Test Notification Now")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(isEnabled, hour, minute, snoozeMinutes)
                    Toast.makeText(context, "Reminder settings saved", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
    }
    return name ?: uri.path?.substringAfterLast('/')
}
