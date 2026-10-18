# Custom UI Modifications Guide (YouTube Music Theme)

This document contains a complete record of all custom UI modifications made to **BitChord** to transform it into the dark, clean YouTube Music aesthetic requested by the user. If the upstream repository is updated or if you clone a fresh version, follow this guide to reapply all changes quickly and cleanly.

---

## Table of Contents
1. [Overview & Requirements](#overview--requirements)
2. [Modified Files Summary](#modified-files-summary)
3. [Detailed File Changes & Code Snippets](#detailed-file-changes--code-snippets)
   - [1. `MainActivity.kt`](#1-mainactivitykt)
   - [2. `GlassNavBar.kt`](#2-glassnavbarkt)
   - [3. `PlayerControls.kt`](#3-playercontrolskt)
   - [4. `NowPlayingScreen.kt`](#4-nowplayingscreenkt)
   - [5. `HomeScreen.kt` & `MainViewModel.kt` (Indian Genre Filter Chips)](#5-homescreenkt--mainviewmodelkt-indian-genre-filter-chips)
   - [6. `PlayerExpandedDrawer.kt` (Queue Drag Reordering & Crash Prevention)](#6-playerexpandeddrawerkt-queue-drag-reordering--crash-prevention)
   - [7. `SongActionsSheet.kt` & `MainActivity.kt` (Song Options Bottom Sheet Height > 80%)](#7-songactionssheetkt--mainactivitykt-song-options-bottom-sheet-height--80)
   - [8. `AppSettings.kt` & `SettingsSheet.kt` (Cache Size Floor at 50MB, Default 100MB)](#8-appsettingskt--settingssheetkt-cache-size-floor-at-50mb-default-100mb)
   - [9. `Autoplay.kt` & `PlaybackService.kt` (Up Next Queue Expansion to 30 Tracks with Multi-Hop Radio)](#9-autoplaykt--playbackservicekt-up-next-queue-expansion-to-30-tracks-with-multi-hop-radio)
   - [10. Lightweight Build & Split ABI Configuration](#10-lightweight-build--split-abi-configuration)
4. [How to Reapply After Upstream Updates](#how-to-reapply-after-upstream-updates)

---

## Overview & Requirements

* **Dark & Clean Aesthetic**: Dark surfaces (`#050505` to `#1F1F1F`), high-contrast white text, rounded cards (`12.dp`), and tactile controls.
* **Bottom Navigation Order**: Strictly **1. Home**, **2. Search** (2nd position), **3. Explore**, **4. Library**.
* **Navigation Bar**: Search tab integrated cleanly inside the glass bottom bar rather than as an isolated floating button.
* **Top Headers**: **No Cast (Chromecast) icon** in any top header (neither Home nor Player view).
* **Full-Screen Player**:
  - Background: Seamless ambient vertical gradient from dominant artwork color fading into `#0C0C0C`.
  - Artwork: Fixed square container with `18.dp` rounded corners (`heroMode = false`), preventing distorted full-bleed stretch.
  - Top header: Left collapse chevron (`v`), clean centered title/artist header or clean bar, right 3-dots more menu (Song/Video pill and Cast icon removed).
  - Volume control: Removed volume slider row and excess spacers to match the clean YouTube Music player look.
  - Transport controls: 5-button symmetrical row: Shuffle, Previous, **solid white circular Play/Pause button**, Next, Repeat.
  - Loop/Repeat button: Repeat-1 state centered digit "1" at `8.sp`, `FontWeight.Black`, completely free from bottom clipping.
  - Bottom sheet peek: Drag handle indicator (`36.dp` x `4.dp`) with **`UP NEXT`**, **`LYRICS`**, and **`RELATED`** tabs with `softWrap = false` and color gradient backdrop.
  - Related songs: Clicking `RELATED` opens a modal sheet with real related tracks fetched via `loadAutoplayTracks` with a "Start radio from this song" action.
* **Home Screen**:
  - Header: Small app logo (`26.dp`) paired with dynamic time-of-day greeting ("Good morning", "Good afternoon", "Good evening") replacing the generic "Listen Now".
  - Horizontal Indian genre filter chips: `"Hindi"`, `"Ghazal/Sufi"`, `"Hindustani Classical"`, `"Indian Indie"`, `"Devotional"`, `"Bhojpuri"`, `"Party"` with native explore category matching and curated search shelves fallback.
  - Quick picks / Listen again: Two-row horizontal card grid (`TwoRowGridShelf`) positioned directly beneath the filter chips.
  - Feed cleanup: "Albums & singles" shelf filtered out completely from the home feed.
  - Card typography: Upgraded to `FontWeight.SemiBold` with `maxLines = 2` to fit full song titles cleanly without truncation.
* **Options & Context Menus**:
  - Long-press song card options sheet expands immediately to **> 80% screen height** (`82% - 88%`), ensuring all options (Play next, Add to queue, Start radio, Download, Add to playlist, Open album/artist, Share) are visible without scrolling.
* **Cache Management**:
  - Default song cache size reduced from 1024 MB to **100 MB**.
  - Cache size setting slider floor lowered from 200 MB to **50 MB** with discrete stops (50 MB, 100 MB, 250 MB, 500 MB, 1 GB, 2 GB, 5 GB, 10 GB, Unlimited).
* **Up Next Queue**:
  - AutoPlay suggestions and radio queues expanded from 10 to **30 tracks** (at least 25-30 songs).
  - Multi-hop chained radio fetching iteratively queries new seeds from the tail to guarantee full queue length even when YouTube Music radio calls filter out duplicates.
* **Lightweight Split Build**:
  - Excluded ONNX runtime and vocal isolation AI models via build flags, shrinking arm64 release APK from 67.5 MB to **31.4 MB**.

---

## Modified Files Summary

| File Path | Component | Changes Made |
| :--- | :--- | :--- |
| `app/src/main/java/com/music/bitchord/MainActivity.kt` | Navigation & App Shell | Reordered tab constants (`TAB_HOME=0`, `TAB_SEARCH=1`, `TAB_EXPLORE=2`, `TAB_LIBRARY=3`), reordered `tabs` list, passed `dismissPlayer` to `NowPlayingScreen`, set `skipPartiallyExpanded = true` for options sheet, increased `INITIAL_RADIO_TRACKS` to 30. |
| `app/src/main/java/com/music/bitchord/ui/components/GlassNavBar.kt` | Bottom Navigation Bar | Changed `standaloneIndex` to `-1` to embed all 4 tabs uniformly inside the bar. |
| `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerControls.kt` | Player Controls | Redesigned `TransportRow` with 5 buttons and white circular play button. Added `PlayerBottomPeekSheet` composable with `UP NEXT`, `LYRICS`, `RELATED` tabs. |
| `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/NowPlayingScreen.kt` | Full-Screen Player View | Added `onDismiss` callback. Redesigned top header with collapse chevron, Song/Video toggle pill, 3-dots menu (no cast icon). Integrated `PlayerBottomPeekSheet`. |
| `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/screens/HomeScreen.kt` | Home Screen & Shelves | Replaced filter chips with 7 curated Indian music genres (`Hindi`, `Ghazal/Sufi`, `Hindustani Classical`, `Indian Indie`, `Devotional`, `Bhojpuri`, `Party`). Polished headers and set card titles to semi-bold with `maxLines = 2`. |
| `app/src/main/java/com/music/bitchord/ui/MainViewModel.kt` | ViewModel & Mood Shelves | Added Indian genre explore alias mapping in `openMoodByName()` and curated fallback in `openCuratedMoodGenre()` / `loadCuratedGenreShelves()`. |
| `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerExpandedDrawer.kt` | Queue & Expanded Drawer | Added unique collision-resistant keys for queue items. Added smooth clamped drag & drop queue reordering with IPC synchronization. |
| `app/src/main/java/com/music/bitchord/ui/components/SongActionsSheet.kt` | Options Drawer | Configured options bottom sheet height between `82%` and `88%` of screen height. |
| `app/src/main/java/com/music/bitchord/data/settings/AppSettings.kt` | App Settings | Decreased `DEFAULT_SONG_CACHE_MB` to `100` and lowered `MIN_SONG_CACHE_MB` to `50`. |
| `app/src/main/java/com/music/bitchord/ui/screens/SettingsSheet.kt` | Settings UI | Added discrete cache slider stops starting from 50 MB (50, 100, 250, 500, 1024, 2048, 5120, 10240, 0). |
| `app/src/main/java/com/music/bitchord/playback/Autoplay.kt` | AutoPlay Queue Logic | Increased `MAX_QUEUED_AUTOPLAY` to `30`. Added multi-hop chained radio fetching loop to ensure at least 25-30 songs fill the queue. |
| `app/src/main/java/com/music/bitchord/playback/PlaybackService.kt` | Playback Background Service | Increased `INITIAL_STATION_TRACKS` to `30` for stations and AutoPlay queues. |
| `gradle.properties` & `app/build.gradle.kts` | Build System | Added `bitchord.light=true`, `bitchord.bundleWear=false`, enabled ABI split for `arm64-v8a` to produce a ~31 MB lightweight APK. |

---

## Detailed File Changes & Code Snippets

### 1. `MainActivity.kt`
**File Path**: `app/src/main/java/com/music/bitchord/MainActivity.kt`

#### A. Reorder Tab Indices
Around line 407:
```kotlin
private const val TAB_HOME = 0
private const val TAB_SEARCH = 1
private const val TAB_EXPLORE = 2
private const val TAB_LIBRARY = 3
```

#### B. Reorder `tabs` List
Around line 1222:
```kotlin
val tabs = remember {
    listOf(
        NavTab.Home,
        NavTab.Search,
        NavTab.Explore,
        NavTab.Library,
    )
}
```

#### C. Connect Player Dismiss Callback
Around line 3010, where `NowPlayingScreen` is invoked:
```kotlin
NowPlayingScreen(
    song = song,
    // ... other parameters
    onDismiss = dismissPlayer,
)
```

---

### 2. `GlassNavBar.kt`
**File Path**: `app/src/main/java/com/music/bitchord/ui/components/GlassNavBar.kt`

Around line 30:
```kotlin
// Change standaloneIndex from 1 to -1 so all 4 tabs sit inside the bottom bar
standaloneIndex: Int = -1,
```

---

### 3. `PlayerControls.kt`
**File Path**: `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerControls.kt`

#### A. Redesign `TransportRow`
Around lines 350-480:
```kotlin
@Composable
internal fun TransportRow(
    isPlaying: Boolean,
    isLoading: Boolean,
    previousEnabled: Boolean,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    shuffleEnabled: Boolean = false,
    repeatMode: Int = 0,
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. Shuffle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggleShuffle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BitChordIcons.Shuffle,
                contentDescription = null,
                tint = if (shuffleEnabled) Color.White else Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(24.dp),
            )
        }

        // 2. Previous
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .clickable(enabled = previousEnabled, onClick = onPrevious),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = null,
                tint = if (previousEnabled) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(34.dp),
            )
        }

        // 3. Prominent Solid White Circular Play/Pause Button
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.Black,
                    strokeWidth = 3.dp,
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        // 4. Next
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .clickable(enabled = nextEnabled, onClick = onNext),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = null,
                tint = if (nextEnabled) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(34.dp),
            )
        }

        // 5. Repeat
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onCycleRepeat),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (repeatMode == RepeatModes.ONE) BitChordIcons.RepeatOne else BitChordIcons.Repeat,
                contentDescription = null,
                tint = if (repeatMode != RepeatModes.OFF) Color.White else Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
```

#### B. Add `PlayerBottomPeekSheet` Composable
Directly after `PlayerActionRow` (around line 788):
```kotlin
/**
 * YouTube Music-style bottom peek bar with UP NEXT, LYRICS, and RELATED tabs.
 */
@Composable
internal fun PlayerBottomPeekSheet(
    queueOpen: Boolean,
    lyricsOpen: Boolean,
    onToggleQueue: () -> Unit,
    onToggleLyrics: () -> Unit,
    onOpenRelated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFF212121).copy(alpha = 0.95f))
            .padding(top = 10.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Drag handle indicator
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f)),
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PeekTab(
                title = "UP NEXT",
                selected = queueOpen,
                onClick = onToggleQueue,
            )
            PeekTab(
                title = "LYRICS",
                selected = lyricsOpen,
                onClick = onToggleLyrics,
            )
            PeekTab(
                title = "RELATED",
                selected = false,
                onClick = onOpenRelated,
            )
        }
    }
}

@Composable
private fun PeekTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
        ),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}
```

---

### 4. `NowPlayingScreen.kt`
**File Path**: `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/NowPlayingScreen.kt`

#### A. Add `onDismiss` Parameter
```kotlin
fun NowPlayingScreen(
    song: Song,
    // ...
    onDismiss: () -> Unit = {},
)
```

#### B. Redesign Top Header Strip (Lines ~2340–2445)
```kotlin
// Left: Collapse Chevron Button
Box(
    modifier = Modifier
        .align(Alignment.CenterStart)
        .size(42.dp)
        .clip(CircleShape)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onDismiss,
        ),
    contentAlignment = Alignment.Center,
) {
    Icon(
        imageVector = Icons.Rounded.KeyboardArrowDown,
        contentDescription = "Close",
        tint = Color.White,
        modifier = Modifier.size(28.dp),
    )
}

// Center: Song / Video Segmented Toggle Pill
var isVideoMode by remember { mutableStateOf(song.isVideoOrigin) }
Row(
    modifier = Modifier
        .align(Alignment.Center)
        .clip(RoundedCornerShape(percent = 50))
        .background(Color.White.copy(alpha = 0.16f))
        .padding(2.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (!isVideoMode) Color.White.copy(alpha = 0.28f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { isVideoMode = false }
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Song",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (!isVideoMode) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
            ),
            color = if (!isVideoMode) Color.White else Color.White.copy(alpha = 0.65f),
        )
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (isVideoMode) Color.White.copy(alpha = 0.28f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { isVideoMode = true }
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Video",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isVideoMode) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
            ),
            color = if (isVideoMode) Color.White else Color.White.copy(alpha = 0.65f),
        )
    }
}

// Right: 3-dots Menu (Cast button intentionally omitted)
Box(
    modifier = Modifier
        .align(Alignment.CenterEnd)
        .size(42.dp)
        .clip(CircleShape)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onOpenMenu,
        ),
    contentAlignment = Alignment.Center,
) {
    Icon(
        imageVector = Icons.Rounded.MoreVert,
        contentDescription = stringResource(Res.string.more),
        tint = Color.White,
        modifier = Modifier.size(22.dp),
    )
}
```

#### C. Embed `PlayerBottomPeekSheet`
In `playerActions` (lines ~1495–1520):
```kotlin
val playerActions: @Composable () -> Unit = {
    // Subtle output caption
    Box(
        modifier = Modifier.fillMaxWidth().height(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        OutputCaption(
            accountName = accountName,
            onOpenOutput = openAudioOutput,
            onOpenMembers = openListenTogetherMembers,
        )
    }
    Spacer(Modifier.height(8.dp))
    PlayerBottomPeekSheet(
        queueOpen = queueOpen,
        lyricsOpen = lyricsOpen,
        onToggleQueue = toggleQueue,
        onToggleLyrics = toggleLyrics,
        onOpenRelated = openPlaybackOrigin,
    )
}
```

---

### 5. `HomeScreen.kt` & `MainViewModel.kt` (Indian Genre Filter Chips)
**Files**: `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/screens/HomeScreen.kt` & `app/src/main/java/com/music/bitchord/ui/MainViewModel.kt`

#### A. Curated Indian Music Filter Chips Composable
In `HomeScreen.kt`, replaced generic moods with 7 curated Indian music genres placed above quick picks:
```kotlin
@Composable
private fun HomeMoodFilterChips(
    selectedMood: String?,
    onSelectMood: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val genres = remember {
        listOf(
            "Hindi",
            "Ghazal/Sufi",
            "Hindustani Classical",
            "Indian Indie",
            "Devotional",
            "Bhojpuri",
            "Party",
        )
    }
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentPadding = PaddingValues(horizontal = PAGE_GUTTER),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(genres) { genre ->
            val isSelected = genre == selectedMood
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White else Color.White.copy(alpha = 0.08f)
                    )
                    .clickable {
                        onSelectMood(if (isSelected) null else genre)
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = genre,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.Black else Color.White,
                )
            }
        }
    }
}
```

#### B. Dynamic Explore & Curated Search Shelves Wiring
In `MainViewModel.kt`:
1. **Explore Category Alias Matching (`openMoodByName`)**:
   Matches Indian genre titles to YouTube Music's Explore mood categories using normalized aliases (e.g., `"hindi" -> "bollywood"`, `"ghazal/sufi" -> "sufi"`, `"party" -> "party"`, `"devotional" -> "devotional"`).
2. **Curated Multi-Shelf Fallback (`openCuratedMoodGenre`)**:
   For genres that are not part of YouTube Music's static mood catalog (e.g., *Hindustani Classical*, *Indian Indie*, *Bhojpuri*), queries YouTube Music concurrently for Playlists, Top Songs, and Albums to build a full browse experience:
   ```kotlin
   fun openMoodByName(moodName: String) {
       val moodId = findMoodIdFor(moodName)
       if (moodId != null) {
           openMood(moodId)
       } else {
           openCuratedMoodGenre(moodName)
       }
   }
   ```
            state.data
        }
        itemsIndexedShelves(shelves = displayShelves, ...)
    }
}
```

#### C. Polish "Mixed for you" / "Listen again" Headers & Cards
In `SectionHeader`:
```kotlin
Text(
    text = displayTitle,
    style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
    color = MaterialTheme.colorScheme.onBackground,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
if (onShowAll != null) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onShowAll)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.show_all),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = Color.White,
        )
    }
}
```

In `ShelfCard`:
```kotlin
Text(
    text = item.title,
    style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    color = if (isCurrent) PlayingAccent else MaterialTheme.colorScheme.onBackground,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.weight(1f, fill = false),
)
```

---

### 6. `PlayerExpandedDrawer.kt` (Queue Drag Reordering & Crash Prevention)
**File Path**: `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerExpandedDrawer.kt`

#### A. Unique Key Collision Prevention (Fixes Silent Crash / Crash on "Up Next")
Prevents `IllegalArgumentException: Key was already used` when tracks in the queue have empty, null, or duplicate `queueEntryId`s:
```kotlin
data class DrawerQueueItem(
    val timelineIndex: Int,
    val song: Song,
    val key: String,
)

// Computed with unique collision-resistant key tracking:
val upcomingTracks = remember(playbackQueue, currentSongIndex) {
    val usedKeys = mutableSetOf<String>()
    playbackQueue.drop(currentSongIndex + 1).mapIndexed { offset, song ->
        val timelineIndex = currentSongIndex + 1 + offset
        val base = song.queueEntryId ?: "${song.id}_$timelineIndex"
        var candidate = "upcoming_$base"
        var r = 1
        while (!usedKeys.add(candidate)) {
            candidate = "upcoming_${base}_${r++}"
        }
        DrawerQueueItem(timelineIndex = timelineIndex, song = song, key = candidate)
    }
}
```

#### B. Smooth Drag & Drop Queue Reordering (Fixes Flickering, IPC Loop & Freezes)
- **`awaiting` synchronization**: Prevents multiple async IPC `onMoveInQueue` (`MediaController.moveMediaItem`) calls before Compose can reflect the new positions in layout.
- **Clamped bounds & edge auto-scrolling**: Held center is clamped between the first and last visible upcoming items, and automatically scrolls up/down at 340dp/s when dragging near viewport edges.
- **Clean disposal**: `DisposableEffect(item.key) { onDispose { drag.end(item.key) } }` resets drag state if an item is recycled or removed mid-drag.
- **Smooth animations**: Uses `Modifier.animateItem(placementSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))` so other items slide gracefully out of the way.

---

### 7. `SongActionsSheet.kt` & `MainActivity.kt` (Song Options Bottom Sheet Height > 80%)
**Files**: `app/src/main/java/com/music/bitchord/ui/components/SongActionsSheet.kt` & `app/src/main/java/com/music/bitchord/MainActivity.kt`

- **Immediate Full Expansion**:
  In `MainActivity.kt`, passed `sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)` to `ModalBottomSheet` so it never halts at the 50% half-screen anchor.
- **Expanded Height (> 80%)**:
  In `SongActionsSheet.kt` inside `TintedSheet`, configured the sheet container to span `min = screenHeight * 0.82f` and `max = screenHeight * 0.88f`:
  ```kotlin
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  val minHeight = screenHeight * 0.82f
  val maxHeight = screenHeight * 0.88f
  Column(
      Modifier
          .fillMaxWidth()
          .heightIn(min = minHeight, max = maxHeight),
  )
  ```
  All options (Play next, Add to queue, Start radio, Download, Add to playlist, Open album/artist, Share, etc.) are immediately visible and clickable upon holding any card or tapping the three-dot button without scrolling.
---

### 8. `AppSettings.kt` & `SettingsSheet.kt` (Cache Size Floor at 50MB, Default 100MB)
**Files**: `app/src/main/java/com/music/bitchord/data/settings/AppSettings.kt` & `app/src/main/java/com/music/bitchord/ui/screens/SettingsSheet.kt`

- **Defaults & Floor**:
  In `AppSettings.kt`:
  ```kotlin
  const val DEFAULT_SONG_CACHE_MB = 100
  const val MIN_SONG_CACHE_MB = 50
  ```
- **Discrete Slider Stops**:
  In `SettingsSheet.kt`, allows users to choose discrete cache limits starting from 50 MB:
  ```kotlin
  val cacheStops = listOf(50, 100, 250, 500, 1024, 2048, 5120, 10240, 0)
  ```
  Stops display as `50 MB`, `100 MB`, `250 MB`, `500 MB`, `1 GB`, `2 GB`, `5 GB`, `10 GB`, `Unlimited`.

---

### 9. `Autoplay.kt` & `PlaybackService.kt` (Up Next Queue Expansion to 30 Tracks with Multi-Hop Radio)
**Files**: `app/src/main/java/com/music/bitchord/playback/Autoplay.kt`, `app/src/main/java/com/music/bitchord/playback/PlaybackService.kt`, `app/src/main/java/com/music/bitchord/MainActivity.kt`

- **Queue Limit Increase**:
  - `Autoplay.kt`: `const val MAX_QUEUED_AUTOPLAY = 30` (was `10`).
  - `PlaybackService.kt`: `private const val INITIAL_STATION_TRACKS = 30` (was `24`).
  - `MainActivity.kt`: `private const val INITIAL_RADIO_TRACKS = 30` (was `24`).
- **Chained Multi-Hop Radio Fetching**:
  A single `radio(seed)` API call to YouTube Music often returns fewer than 30 usable songs after excluding music video duplicates and applying artist quotas (`PER_ARTIST_LIMIT = 2`).
  In `Autoplay.kt`, `loadAutoplayTracks()` loops up to 5 hops, iteratively seeding from the tail of the newly added suggestions until reaching `limit = 30` (guaranteeing 25–30 songs):
  ```kotlin
  val extra = QueueBuilder.extend(existing, related, limit).toMutableList()
  val triedSeeds = mutableSetOf(seed)

  while (extra.size < limit && triedSeeds.size < 5) {
      val nextSeed = extra.asReversed().firstNotNullOfOrNull { song ->
          val s = youtubeSeedFor(song)
          if (s != null && s !in triedSeeds) s else null
      } ?: break
      triedSeeds.add(nextSeed)
      val moreRelated = YtMusicRepository.radio(nextSeed).getOrNull()
      if (!moreRelated.isNullOrEmpty()) {
          val moreExtra = QueueBuilder.extend(existing + extra, moreRelated, limit - extra.size)
          if (moreExtra.isNotEmpty()) {
              extra.addAll(moreExtra)
          }
      }
  }
  ```

---

### 10. Lightweight Build & Split ABI Configuration
**Files**: `gradle.properties`, `app/build.gradle.kts`

- **Exclude Offline Vocal Isolation AI Models & ONNX Runtime**:
  Offline vocal separation bundles ~35+ MB of native ONNX binaries and AI weight models (`.onnx` models).
  To build a super lightweight APK for everyday streaming:
  In `gradle.properties`:
  ```properties
  bitchord.light=true
  bitchord.bundleWear=false
  ```
  In `app/build.gradle.kts`:
  ```kotlin
  val isLight = providers.gradleProperty("bitchord.light").orNull == "true"
  if (isLight) {
      // Excludes onnxruntime and AI models from packaging
      packaging {
          jniLibs {
              excludes += "**/libonnxruntime*.so"
          }
      }
  }
  ```
- **ABI Split for Arm64-v8a**:
  Generates standalone APKs per CPU architecture. Running `.\gradlew :app:assembleDevRelease` generates `app-dev-arm64-v8a-release.apk` at only **~31.4 MB** (down from > 95 MB original universal build).

---

## How to Reapply After Upstream Updates

If you pull upstream changes using Git:
```bash
# Check diff or stash before pulling
git stash

# Pull upstream commits
git pull origin main

# Reapply your stash, or view git diff of this guide:
git apply --reject
```
Or simply refer to the snippets in this document to re-verify the modified files:
1. `app/src/main/java/com/music/bitchord/MainActivity.kt` (Navigation tabs, options sheet skip partial expand, initial radio size)
2. `app/src/main/java/com/music/bitchord/ui/components/GlassNavBar.kt` (Embedded navigation tabs)
3. `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerControls.kt` (5-button transport row, white play circle, peek sheet)
4. `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/NowPlayingScreen.kt` (Clean header, no cast icon, ambient background)
5. `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/screens/HomeScreen.kt` & `MainViewModel.kt` (7 Indian genre filter chips, explore aliases & curated fallback shelves)
6. `sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerExpandedDrawer.kt` (Unique queue item keys, smooth clamped drag & drop reordering)
7. `app/src/main/java/com/music/bitchord/ui/components/SongActionsSheet.kt` (Options sheet height > 80%)
8. `app/src/main/java/com/music/bitchord/data/settings/AppSettings.kt` (Cache default 100MB, min 50MB)
9. `app/src/main/java/com/music/bitchord/ui/screens/SettingsSheet.kt` (Cache slider starting from 50MB with discrete stops)
10. `app/src/main/java/com/music/bitchord/playback/Autoplay.kt` & `PlaybackService.kt` (Up Next queue increased from 10 to 30 songs with multi-hop chained radio fetching)
11. `gradle.properties` & `app/build.gradle.kts` (Lightweight build flag, ABI split for arm64)
