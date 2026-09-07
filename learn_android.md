# Day 1: Kotlin Core & Modern Syntax
## Kotlin Essentials & Null Safety
* [ ] Variables & Mutability: `val` vs `var`, Type Inference - [Kotlin Docs](https://kotlinlang.org/docs/basic-syntax.html#variables)
* [ ] Null Safety System: Nullable types (`Type?`), Safe Call (`?.`), Elvis Operator (`?:`), Not-Null Assertion (`!!`) - [Kotlin Docs](https://kotlinlang.org/docs/null-safety.html)
* [ ] Smart Casts & Type Checks: `is`, `!is`, `as?` safe cast - [Kotlin Docs](https://kotlinlang.org/docs/typecasts.html)

## Control Flow & Functions
* [ ] Expressions: `when` expressions as exhaustive pattern matching - [Kotlin Docs](https://kotlinlang.org/docs/control-flow.html#when-expression)
* [ ] Functions & Default Parameters: Named arguments, single-expression functions
* [ ] Scope Functions: Master `let`, `run`, `with`, `apply`, and `also` - [Baeldung Guide](https://www.baeldung.com/kotlin/scope-functions)

# Day 2: Object-Oriented Kotlin & Functional Concepts
## Classes & Data Models
* [ ] Data Classes: Automatic `equals()`, `hashCode()`, `toString()`, and `copy()` - [Kotlin Docs](https://kotlinlang.org/docs/data-classes.html)
* [ ] Computed Properties: Custom getters (`val isDone: Boolean get() = ...`) - [Kotlin Docs](https://kotlinlang.org/docs/properties.html#getters-and-setters)
* [ ] Sealed Classes & Interfaces: Modeling restricted class hierarchies - [Kotlin Docs](https://kotlinlang.org/docs/sealed-classes.html)

## Collections & Functional Operators
* [ ] Collections Overview: `List`, `Set`, `Map` (Mutable vs Immutable)
* [ ] Functional Operators: `map`, `filter`, `any`, `all`, `indexOfFirst`, `sortedWith` - [Kotlin Docs](https://kotlinlang.org/docs/collection-operations.html)
* [ ] Lambdas & Higher-Order Functions: Passing functions as parameters (`(T) -> Unit`)

# Day 3: Jetpack Compose Fundamentals
## Declarative UI Mindset
* [ ] Declarative Paradigm: How Compose differs from legacy XML Views - [Android Docs](https://developer.android.com/develop/ui/compose/mental-model)
* [ ] Composable Functions: `@Composable` annotation, Recomposition lifecycle - [Android Docs](https://developer.android.com/develop/ui/compose/lifecycle)
* [ ] Component Structure: Building modular UI components (`DayCard`, `TopicSection`, `SubtopicRow`)

## Core Layouts & Modifiers
* [ ] Basic Layouts: `Column`, `Row`, `Box`, `Surface`, and `Scaffold` - [Android Docs](https://developer.android.com/develop/ui/compose/layouts/basics)
* [ ] Modifier Chains: Order matters in `fillMaxWidth()`, `padding()`, `clip()`, `border()`, `clickable()` - [Android Docs](https://developer.android.com/develop/ui/compose/modifiers)
* [ ] Material 3 Components: `TopAppBar`, `FloatingActionButton`, `Card`, `LinearProgressIndicator`, `AlertDialog` - [Material 3 Docs](https://m3.material.io/components)

# Day 4: Compose State Management & Reactivity
## State in Compose
* [ ] State & Recomposition: `remember` and `mutableStateOf` - [Android Docs](https://developer.android.com/develop/ui/compose/state)
* [ ] State Hoisting: Lifting state up to make composables stateless and reusable - [Android Docs](https://developer.android.com/develop/ui/compose/state#state-hoisting)
* [ ] MutableState inside Data Classes: Using `MutableState<Boolean>` for reactive checkbox toggles - [Android Docs](https://developer.android.com/reference/kotlin/androidx/compose/runtime/MutableState)

## Lazy Lists & Performance
* [ ] Scrollable Lists: `LazyColumn` and `LazyRow` - [Android Docs](https://developer.android.com/develop/ui/compose/lists)
* [ ] List Item Keys: Using `key = { item.id }` for efficient list recomposition and item animations
* [ ] List State Controls: `rememberLazyListState()` and `animateScrollToItem()` for auto-scrolling

# Day 5: Architecture, ViewModel & StateFlow
## Architecture Patterns
* [ ] MVVM in Android: Model-View-ViewModel architecture principles - [Android Docs](https://developer.android.com/topic/architecture)
* [ ] ViewModel Lifecycle: `androidx.lifecycle.ViewModel` retaining state across configuration changes - [Android Docs](https://developer.android.com/topic/libraries/architecture/viewmodel)

## Kotlin Coroutines & StateFlow
* [ ] Coroutines Basics: `suspend` functions, `viewModelScope`, and Dispatchers - [Kotlin Docs](https://kotlinlang.org/docs/coroutines-overview.html)
* [ ] StateFlow & SharedFlow: `MutableStateFlow` vs `StateFlow.asStateFlow()` - [Android Docs](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
* [ ] Compose Integration: Observing flows safely in UI using `collectAsState()` - [Android Docs](https://developer.android.com/develop/ui/compose/state-lines)

# Day 6: Storage, JSON & Data Persistence
## Android File Storage & SharedPreferences
* [ ] SharedPreferences API: Storing key-value pairs with `getSharedPreferences()` - [Android Docs](https://developer.android.com/training/data-storage/shared-preferences)
* [ ] Local File Access: Reading raw assets or user files via `ContentResolver.openInputStream()` - [Android Docs](https://developer.android.com/training/secure-file-sharing/retrieve-info)

## Data Serialization
* [ ] JSON Parsing: Using `JSONObject` and `JSONArray` for multi-entity serialization - [Android Docs](https://developer.android.com/reference/org/json/JSONObject)
* [ ] Task State Persistence: Unique key hashing (`"task_done|id|day|topic|subtopic"`) to persist checkbox states across app restarts

# Day 7: Background Work, Alarms & Receivers
## AlarmManager & Exact Alarms
* [ ] Exact Timing: `AlarmManager.setAlarmClock()` and `setExactAndAllowWhileIdle()` - [Android Docs](https://developer.android.com/training/scheduling/alarms)
* [ ] PendingIntent: Constructing `PendingIntent.getBroadcast()` with `FLAG_IMMUTABLE` - [Android Docs](https://developer.android.com/guide/components/intents-filters)
* [ ] Device Reboot Handling: Catching `BOOT_COMPLETED` with `BroadcastReceiver` - [Android Docs](https://developer.android.com/guide/components/broadcasts)

## BroadcastReceivers
* [ ] Receiver Registration: Declaring receivers in `AndroidManifest.xml` - [Android Docs](https://developer.android.com/guide/components/broadcasts)
* [ ] Notification Actions: Handling custom `BroadcastReceiver` triggers (Notification Snooze flow)

# Day 8: Android Notification System
## Channels & Builders
* [ ] Notification Channels: Creating `NotificationChannel` with `IMPORTANCE_HIGH` (API 26+) - [Android Docs](https://developer.android.com/develop/ui/views/notifications/channels)
* [ ] NotificationCompat.Builder: Setting icons (`setSmallIcon`, `setLargeIcon`), title, text, priority - [Android Docs](https://developer.android.com/develop/ui/views/notifications/build-notification)

## Custom Sounds & Permissions
* [ ] Custom Raw Sounds: Playing `@raw/yoooooooo.mp3` via `AudioAttributes` and `setSound()` - [Android Docs](https://developer.android.com/reference/android/media/AudioAttributes)
* [ ] Notification Actions: Adding `NotificationCompat.Action` buttons for Snooze - [Android Docs](https://developer.android.com/develop/ui/views/notifications/expanded)
* [ ] Runtime Permissions: Requesting `POST_NOTIFICATIONS` permission on Android 13+ (API 33) - [Android Docs](https://developer.android.com/develop/ui/views/notifications/notification-permission)

# Day 9: Text Processing & Markdown AST Parsing
## Abstract Syntax Trees (AST)
* [ ] CommonMark Library: `Parser.builder().build().parse(text)` - [CommonMark Java GitHub](https://github.com/commonmark/commonmark-java)
* [ ] AST Node Traversal: Navigating `Document`, `Heading`, `BulletList`, `ListItem`, and `Link` nodes
* [ ] Text Extraction & Sanitization: Preserving link destinations while stripping citation tags

## Clickable Links in Compose
* [ ] AnnotatedString: Formatting text ranges with `buildAnnotatedString` and `SpanStyle` - [Android Docs](https://developer.android.com/develop/ui/compose/text/style-text#annotated-string)
* [ ] String Annotations: Attaching custom tags (`pushStringAnnotation("URL", url)`) - [Android Docs](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/AnnotatedString)
* [ ] Pointer Input & Gesture Detection: Using `detectTapGestures` and `TextLayoutResult.getOffsetForPosition()` - [Android Docs](https://developer.android.com/develop/ui/compose/touch-input/gestures)

# Day 10: Animations & Advanced Polish
## Compose Item Animations
* [ ] List Item Animations: Animating item reordering and favorites moves using `Modifier.animateItem()` - [Android Docs](https://developer.android.com/develop/ui/compose/animation/composables-modifiers#animate-item-placement)
* [ ] Color Shift Animations: Transitioning card highlight background colors with `animateColorAsState()` - [Android Docs](https://developer.android.com/develop/ui/compose/animation/value-based#animatecolorasstate)

## Navigation & Back Handling
* [ ] BackHandler: Intercepting system back gestures with `BackHandler` - [Android Docs](https://developer.android.com/develop/ui/compose/navigation#back-button)
* [ ] Exit Dialogs: Prompting confirmation before finishing the activity (`ComponentActivity.finish()`)
* [ ] Gradle Build System: Compiling, running unit tests (`./gradlew test`), and building APKs - [Android Docs](https://developer.android.com/build)
