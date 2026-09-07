# Markdown Timetable 📅

A modern, native Android application built with **Kotlin** and **Jetpack Compose** that parses raw Markdown files or text into a hierarchical, interactive study and task timetable with cascading completion tracking, exact alarms with custom sounds, snooze notifications, and local device persistence.

---

## 🌟 Key Features

1. **Hierarchical Markdown Parsing**:
   - Parses Markdown Heading Level 1 (`# Day X`) into **Days**.
   - Parses Markdown Heading Level 2 (`## Topic Y`) into **Topics**.
   - Parses Bullet List items (`* [ ] Task`) into **Subtopics** with interactive checkboxes.

2. **Cascading Completion Reactivity**:
   - Toggling a **Subtopic** checkbox automatically updates its parent **Topic** completion state.
   - When all Subtopics in a Topic are completed, the Topic is automatically marked **DONE** with a green checkmark and strikethrough.
   - When all Topics in a Day are completed, the Day is automatically marked **COMPLETED** with a green badge and progress bar update.

3. **Multi-Timetable Management & Favorites**:
   - Manage multiple study plans simultaneously on the Home Screen.
   - **Favorites**: Star any timetable (`⭐`) to pin it to the top in a dedicated **FAVORITES** section with smooth animated transitions (`Modifier.animateItem()`).
   - **Reordering**: Move timetables up or down smoothly using `↑` and `↓` buttons.
   - **Renaming**: Click the pencil icon (`✏️`) next to any timetable title to rename it.

4. **Clickable Links & Smart Touch Handling**:
   - Markdown links (`[LeetCode Two Sum](https://leetcode.com/problems/two-sum/)`) and raw URLs are rendered in bold blue underlined text showing both label and URL destination.
   - **Smart Touch Handling**: Tapping directly on a link opens the URL in the system browser (`LocalUriHandler`). Tapping anywhere else on the task row toggles the checkbox state without triggering accidental link opens.

5. **Auto-Scroll to Next Incomplete Day**:
   - On opening a timetable, all completed days start collapsed, and the screen automatically scrolls directly to the **first incomplete day**, expanding its tasks for immediate focus.

6. **Exact Daily Notifications & Snooze Action**:
   - Per-timetable configurable daily reminder time (e.g. 8:00 PM).
   - Powered by `AlarmManager.setAlarmClock()`, ensuring notifications trigger at the exact minute even when the app is completely closed or in deep Doze mode.
   - **Custom Notification Sound**: Plays `@raw/yoooooooo.mp3` custom sound on notification trigger.
   - **Snooze Action Button**: Includes a **Snooze (Xm)** action button directly in the notification dropdown with custom user-input snooze duration (e.g. 5, 10, 15, 30 minutes).
   - **Boot Restoration**: Re-schedules alarms automatically after device reboot (`BOOT_COMPLETED`).

7. **Local Persistence**:
   - All timetables, completion states, custom titles, favorites, and reminder times are saved in local device `SharedPreferences`.

---

## 🏗️ Architecture & Component Overview

```
com.example.markdowntimetable
│
├── model/
│   ├── SavedTimetable.kt       # Entity model for multi-timetable storage
│   └── TimetableModels.kt      # Subtopic (MutableState), Topic (computed isDone), Day (computed isDone)
│
├── parser/
│   └── MarkdownParser.kt       # CommonMark AST parser mapping markdown to Day/Topic/Subtopic
│
├── persistence/
│   └── TaskPersistence.kt      # SharedPreferences storage for multi-timetables & task states
│
├── viewmodel/
│   └── MarkdownViewModel.kt    # StateFlow state manager for timetables, active view & reordering
│
├── notification/
│   ├── AlarmManager & WorkManager background notification system
│   ├── ReminderScheduler.kt    # Exact AlarmManager schedule & cancel helper
│   ├── NotificationReceiver.kt # BroadcastReceiver catching daily exact alarms
│   ├── SnoozeReceiver.kt       # BroadcastReceiver handling notification Snooze action
│   ├── BootReceiver.kt         # BroadcastReceiver restoring alarms on device boot
│   ├── ReminderWorker.kt       # CoroutineWorker building high-importance notifications
│   └── ReminderPreferences.kt # Preferences helper for reminder settings
│
└── ui/
    └── TimetableScreen.kt      # Jetpack Compose UI (HomeScreen, DetailScreen, Dialogs, RoundCheckbox)
```

---

## 📂 Detailed File-by-File Breakdown

### 1. `MainActivity.kt`
- Entry point of the application (`ComponentActivity`).
- Sets up `enableEdgeToEdge()` and renders `TimetableScreen(viewModel)`.

### 2. `model/TimetableModels.kt`
- **`Subtopic`**: Holds `title: String` and `isDone: MutableState<Boolean>`.
- **`Topic`**: Holds `title: String` and `subtopics: List<Subtopic>`. Computed `isDone` evaluates `subtopics.isNotEmpty() && subtopics.all { it.isDone.value }`.
- **`Day`**: Holds `title: String` and `topics: List<Topic>`. Computed `isDone` evaluates `topics.isNotEmpty() && topics.all { it.isDone }`.

### 3. `model/SavedTimetable.kt`
- Data class storing `id`, `title`, `content`, `isFavorite`, `orderIndex`, `isReminderEnabled`, `reminderHour`, `reminderMinute`, and `snoozeMinutes`.

### 4. `parser/MarkdownParser.kt`
- Uses CommonMark library (`org.commonmark:commonmark:0.21.0`).
- Sanitizes citation markers like `[cite: 1]` before parsing AST.
- Maps `#` to `Day`, `##` to `Topic`, and bullets to `Subtopic`.
- Preserves `[Text](url)` link destinations from `Link` AST nodes.

### 5. `persistence/TaskPersistence.kt`
- Manages local `SharedPreferences`.
- Serializes `SavedTimetable` items into JSON arrays.
- Persists individual `isDone` states for every task using keys: `"task_done|{timetableId}|{day}|{topic}|{subtopic}"`.

### 6. `viewmodel/MarkdownViewModel.kt`
- Manages `StateFlow<List<SavedTimetable>>`, `StateFlow<SavedTimetable?>`, and `StateFlow<List<Day>>`.
- Handles adding, deleting, renaming, favoriting, reordering, and updating reminder settings for timetables.

### 7. `ui/TimetableScreen.kt`
- **`HomeScreen`**: Renders all saved timetables, separated into **FAVORITES** and **ALL TIMETABLES**, with `+` FAB to import files or paste text.
- **`DetailScreen`**: Displays selected timetable days/topics/subtopics, with auto-scroll to the first incomplete day (`rememberLazyListState`).
- **`ClickableMarkdownText`**: Parses Markdown links `[Label](url)` and standalone URLs into `AnnotatedString` with blue underline styling. Detects tap gestures to open URLs while allowing checkbox toggling on non-link areas.
- **`RoundCheckbox`**: Custom circular checkbox composable with green fill (`#2E7D32`) and white checkmark.
- **`ReminderSettingsDialog`**: Dialog allowing users to pick daily reminder time, input custom snooze minutes in a text box, and fire test notifications.

### 8. Notification System Files
- **`ReminderScheduler.kt`**: Uses `AlarmManager.setAlarmClock()` for exact daily alarms.
- **`NotificationReceiver.kt`**: Triggered by `AlarmManager`, checks task completion status, posts notification, and reschedules for next day.
- **`SnoozeReceiver.kt`**: Triggered when tapping the **Snooze (Xm)** action button on a notification. Dismisses active notification and schedules an exact snooze alarm.
- **`BootReceiver.kt`**: Restores all active timetable alarms when device restarts (`BOOT_COMPLETED`).
- **`ReminderWorker.kt`**: Builds high-importance notifications with custom app icon (`ic_app_icon`) and custom sound (`R.raw.yoooooooo`).

---

## 🔄 Flow-by-Flow Execution Guide

### Flow 1: App Launch & Local Data Loading
1. `MainActivity` launches -> `TimetableScreen` calls `LaunchedEffect(Unit) { viewModel.initialize(context) }`.
2. `TaskPersistence` loads all saved timetables from `SharedPreferences`.
3. If no timetables exist on first launch ever, creates initial sample timetable once.
4. `HomeScreen` renders all saved timetables sorted by Favorites first, then `orderIndex`.

### Flow 2: Adding / Importing a Timetable
1. User taps `+` FAB -> chooses **Select .md File** or **Paste / Type Markdown Text**.
2. Markdown text is loaded into `MarkdownViewModel.addTimetable(...)`.
3. New timetable is saved to `TaskPersistence` and scheduled for daily reminders.
4. User stays on `HomeScreen`, where the new timetable card appears with a smooth 2.5-second color shift transition (`animateColorAsState`).

### Flow 3: Interactive Task Toggling & Cascading Completion
1. User taps a timetable card -> opens `DetailScreen`.
2. User taps a `RoundCheckbox` or task text.
3. `subtopic.isDone.value` updates -> `topic.isDone` recalculates -> `day.isDone` recalculates.
4. `TaskPersistence.saveTaskDoneState(...)` writes state to `SharedPreferences` in real-time.
5. UI recomposes: checkmarks appear, text gets strikethrough, progress bar updates.

### Flow 4: Exact Daily Alarms & Snooze Action
1. At configured time (e.g. 8:00 PM), `AlarmManager` fires intent to `NotificationReceiver`.
2. `NotificationReceiver` checks if today's tasks are incomplete.
3. If incomplete, `ReminderWorker.sendNotification` posts a notification playing `@raw/yoooooooo.mp3`.
4. Notification dropdown displays a **Snooze (15m)** action button.
5. User taps **Snooze** -> `SnoozeReceiver` dismisses active notification and sets a new alarm for 15 minutes later.

---

## 🚀 How to Push This Project to GitHub

Follow these steps to initialize Git and push this repository to GitHub:

### Step 1: Open Terminal / PowerShell in Project Root
```powershell
cd D:\Users\heman\AndroidStudioProjects\MarkdownTimetable
```

### Step 2: Initialize Git Repository
```powershell
git init
```

### Step 3: Create `.gitignore` File
Ensure build artifacts and local IDE files are excluded:
```gitignore
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

### Step 4: Stage & Commit Files
```powershell
git add .
git commit -m "Initial commit: Complete Markdown Timetable app with Compose, Alarms, Multi-Timetables, and Snooze"
```

### Step 5: Create Repository on GitHub
1. Go to [GitHub.com](https://github.com) -> Click **New Repository**.
2. Name it `MarkdownTimetable`.
3. Do **not** check "Initialize with README" (since we already have one).
4. Click **Create repository**.

### Step 6: Link Remote & Push
Replace `YOUR_USERNAME` with your actual GitHub username:
```powershell
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/MarkdownTimetable.git
git push -u origin main
```
