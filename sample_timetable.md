# Day 1: Kotlin & Compose Fundamentals
## Topic 1: Environment Setup
* [x] Download & Install Android Studio
* [x] Configure JDK 17 and Android SDK 35
* [ ] Clone repository and run initial Gradle sync
* [ ] Configure Android Emulator / ADB physical device

## Topic 2: Kotlin Language Essentials
* Learn Data Classes and Computed Properties
* Master Sealed Interfaces and Pattern Matching
* Understand Coroutines, StateFlow, and SharedFlow
* Practice Kotlin Scope Functions (`let`, `also`, `apply`, `run`)

# Day 2: Jetpack Compose & State Management
## Topic 1: Declarative UI Components
* Explore `@Composable` functions and Recomposition lifecycle
* Master basic layouts: `Column`, `Row`, `Box`, and `LazyColumn`
* Use `Card`, `Checkbox`, and `LinearProgressIndicator`
* Implement smooth expand/collapse animations with `AnimatedVisibility`

## Topic 2: State & ViewModel Integration
* Understand `remember` and `mutableStateOf`
* Connect ViewModel `StateFlow` to Compose UI with `collectAsState()`
* Implement `rememberLauncherForActivityResult` for storage access
* Implement ContentResolver reading for `.md` input streams

# Day 3: Markdown AST & Cascading Verification
## Topic 1: CommonMark Parser Integration
* Add `org.commonmark:commonmark:0.21.0` dependency
* Traverse AST Nodes: `Heading`, `BulletList`, and `ListItem`
* Extract clean text from inline text and code spans
* Map AST hierarchy to `Day`, `Topic`, and `Subtopic` data models

## Topic 2: Reactive Completion Testing
* [ ] Verify Subtopic checkbox state toggles reactively
* [ ] Verify Topic automatically completes when all Subtopics are done
* [ ] Verify Day automatically completes when all Topics are done
* [ ] Verify visual indicators (green checkmarks, strikethrough, DONE badge)
