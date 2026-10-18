package com.music.bitchord.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import com.music.bitchord.ui.haptics.Haptic
import com.music.bitchord.ui.haptics.rememberHaptics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlin.math.abs
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.model.Song
import com.music.bitchord.sharedui.resources.*
import kotlin.math.roundToInt

enum class PlayerDrawerTab {
    UP_NEXT,
    LYRICS,
    RELATED
}

/**
 * YouTube Music-style animated equalizer bars.
 */
@Composable
fun MiniEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White,
) {
    val transition = rememberInfiniteTransition(label = "eq_bars")
    val bar1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eq1",
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eq2",
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eq3",
    )

    Row(
        modifier = modifier.height(13.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            Modifier
                .width(2.5.dp)
                .fillMaxHeight(if (isPlaying) bar1 else 0.35f)
                .background(barColor, RoundedCornerShape(1.dp))
        )
        Box(
            Modifier
                .width(2.5.dp)
                .fillMaxHeight(if (isPlaying) bar2 else 0.55f)
                .background(barColor, RoundedCornerShape(1.dp))
        )
        Box(
            Modifier
                .width(2.5.dp)
                .fillMaxHeight(if (isPlaying) bar3 else 0.4f)
                .background(barColor, RoundedCornerShape(1.dp))
        )
    }
}

/**
 * Small explicit badge "E".
 */
@Composable
fun ExplicitBadge(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Box(
        modifier = modifier
            .size(13.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "E",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = Color.Black,
            lineHeight = 10.sp,
        )
    }
}

/**
 * Clean parallel drag handle matching "=" in YouTube Music.
 */
@Composable
fun ReorderHandleIcon(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .width(18.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color.White.copy(alpha = 0.65f))
        )
        Box(
            Modifier
                .width(18.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color.White.copy(alpha = 0.65f))
        )
    }
}

private fun isSongExplicit(song: Song): Boolean {
    val text = (song.title + " " + song.artist).lowercase()
    return text.contains("explicit") || text.contains("[e]") || text.contains("(e)")
}

private fun buildSubtitle(song: Song): String {
    val duration = song.durationText?.takeIf { it.isNotBlank() }
    return if (duration != null) "${song.artist} • $duration" else song.artist
}

internal data class DrawerQueueItem(
    val timelineIndex: Int,
    val song: Song,
    val key: String,
)

private class DrawerQueueDrag(private val listState: LazyListState) {
    var tracks: List<DrawerQueueItem> = emptyList()
    var onMove: (Int, Int) -> Unit = { _, _ -> }
    var onActiveChange: (Boolean) -> Unit = {}

    var edgeZone = 0f
    var edgeSpeed = 0f

    var held by mutableStateOf<DrawerQueueItem?>(null)
        private set

    var heldTop by mutableFloatStateOf(0f)
        private set

    var autoScrollDir by mutableIntStateOf(0)
        private set

    var autoScrollSpeed = 0f
        private set

    private var heldCenter = 0f
    private var heldSize = 0
    private var awaiting: Int? = null

    fun start(key: String) {
        val track = tracks.firstOrNull { it.key == key } ?: return
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        heldSize = item.size
        heldCenter = item.offset + item.size / 2f
        heldTop = item.offset.toFloat()
        awaiting = null
        held = track
        onActiveChange(true)
    }

    fun drag(deltaY: Float) = settle(deltaY)

    fun scrolled() = settle(0f)

    fun end(key: String) {
        if (held?.key != key) return
        held = null
        awaiting = null
        setAutoScroll(0f)
        onActiveChange(false)
    }

    private fun settle(deltaY: Float) {
        val key = held?.key ?: return
        val info = listState.layoutInfo
        val items = info.visibleItemsInfo
        val half = heldSize / 2f

        var center = heldCenter + deltaY
        tracks.firstOrNull()?.let { first ->
            items.firstOrNull { it.key == first.key }?.let {
                center = center.coerceAtLeast(it.offset + half)
            }
        }
        tracks.lastOrNull()?.let { last ->
            items.firstOrNull { it.key == last.key }?.let {
                center = center.coerceAtMost(it.offset + it.size - half)
            }
        }
        heldCenter = center
        val top = center - half
        val minTop = info.viewportStartOffset.toFloat()
        heldTop = top.coerceIn(minTop, (info.viewportEndOffset - heldSize).toFloat().coerceAtLeast(minTop))

        val dragged = items.firstOrNull { it.key == key } ?: return setAutoScroll(0f)
        aimAutoScroll(top, key)

        awaiting?.let {
            if (dragged.index != it) return
            awaiting = null
        }

        val target = items
            .filter { item -> item.key != key && tracks.any { it.key == item.key } }
            .minByOrNull { abs(it.offset + it.size / 2f - center) }
            ?: return

        if (abs(center - (target.offset + target.size / 2f)) > target.size / 2f) return

        if (target.index == listState.firstVisibleItemIndex && listState.canScrollBackward) return

        val from = tracks.firstOrNull { it.key == key }?.timelineIndex ?: return
        val to = tracks.firstOrNull { it.key == target.key }?.timelineIndex ?: return
        if (from != to) {
            onMove(from, to)
            awaiting = target.index
        }
    }

    private fun aimAutoScroll(top: Float, key: String) {
        val info = listState.layoutInfo
        val speed = edgeScrollSpeed(
            top = top,
            bottom = top + heldSize,
            viewportStart = info.viewportStartOffset,
            viewportEnd = info.viewportEndOffset,
            zone = edgeZone,
            speed = edgeSpeed,
        )
        val blocked = when {
            speed < 0f -> tracks.firstOrNull()?.key == key || !listState.canScrollBackward
            speed > 0f -> tracks.lastOrNull()?.key == key || !listState.canScrollForward
            else -> true
        }
        setAutoScroll(if (blocked) 0f else speed)
    }

    private fun setAutoScroll(speed: Float) {
        autoScrollSpeed = speed
        val direction = when {
            speed > 0f -> 1
            speed < 0f -> -1
            else -> 0
        }
        if (autoScrollDir != direction) autoScrollDir = direction
    }
}

@Composable
private fun rememberDrawerQueueDrag(
    listState: LazyListState,
    tracks: List<DrawerQueueItem>,
    onMove: (Int, Int) -> Unit,
    onActiveChange: (Boolean) -> Unit,
): DrawerQueueDrag {
    val drag = remember(listState) { DrawerQueueDrag(listState) }
    drag.tracks = tracks
    drag.onMove = onMove
    drag.onActiveChange = onActiveChange
    with(LocalDensity.current) {
        drag.edgeZone = 48.dp.toPx()
        drag.edgeSpeed = 340.dp.toPx()
    }
    val direction = drag.autoScrollDir
    LaunchedEffect(drag, direction) {
        if (direction == 0) return@LaunchedEffect
        listState.scroll {
            var previous = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                val seconds = ((now - previous) / 1_000_000_000f).coerceAtMost(1f / 30f)
                previous = now
                if (scrollBy(drag.autoScrollSpeed * seconds) == 0f) break
                drag.scrolled()
            }
        }
    }
    return drag
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UpcomingQueueTrackRow(
    trackSong: Song,
    itemKey: String,
    isBeingDragged: Boolean,
    isAnyItemDragging: Boolean,
    onJumpTo: () -> Unit,
    onSongMenu: () -> Unit,
    onPlayNext: () -> Unit,
    onRemoveFromQueue: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 72.dp.toPx() }
    val maxSwipePx = thresholdPx * 1.35f
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(if (isBeingDragged) Modifier.alpha(0f) else Modifier),
    ) {
        // Background swipe actions layer
        val currentOffset = offsetX.value
        if (currentOffset > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1976D2))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                        contentDescription = "Play next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Play next",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = Color.White,
                    )
                }
            }
        } else if (currentOffset < 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD32F2F))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = Color.White,
                    )
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Remove from queue",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        // Foreground content card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141211))
                .pointerInput(isAnyItemDragging) {
                    if (!isAnyItemDragging) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value >= thresholdPx) {
                                        haptics.play(Haptic.Select)
                                        onPlayNext()
                                    } else if (offsetX.value <= -thresholdPx) {
                                        haptics.play(Haptic.Select)
                                        onRemoveFromQueue()
                                    }
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetX.animateTo(0f) }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-maxSwipePx, maxSwipePx))
                                }
                            },
                        )
                    }
                }
                .combinedClickable(
                    enabled = !isAnyItemDragging && offsetX.value == 0f,
                    onClick = onJumpTo,
                    onLongClick = {
                        haptics.play(Haptic.Select)
                        onSongMenu()
                    },
                )
                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = rememberRemoteArtworkUrl(trackSong),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trackSong.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ExplicitBadge(visible = isSongExplicit(trackSong))
                    Text(
                        text = buildSubtitle(trackSong),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(
                onClick = {
                    haptics.play(Haptic.Tap)
                    onSongMenu()
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Song options",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .pointerInput(itemKey) {
                        detectDragGestures(
                            onDragStart = {
                                haptics.play(Haptic.Select)
                                onDragStart()
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onDrag = { change, amount ->
                                change.consume()
                                onDrag(amount.y)
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                ReorderHandleIcon()
            }
        }
    }
}

/**
 * YouTube Music-style full expanding drawer from bottom with:
 * - Top mini player bar (thumbnail, song title, artist, cast icon, play/pause)
 * - Tabs row: UP NEXT | LYRICS | RELATED with underline indicator
 * - UP NEXT: source info + save button, current song card with live animated equalizer, auto-play toggle, mood chips, queue list
 * - LYRICS: synchronized lyrics
 * - RELATED: "Start radio from this song" and similar recommendations
 */
@Composable
internal fun PlayerExpandedDrawer(
    song: Song,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onOpenCast: () -> Unit,
    activeTab: PlayerDrawerTab,
    onTabSelected: (PlayerDrawerTab) -> Unit,
    onDismiss: () -> Unit,
    // UP NEXT queue props
    queue: List<Song>,
    currentIndex: Int,
    autoplayEnabled: Boolean,
    onToggleAutoplay: () -> Unit,
    controlsLocked: Boolean,
    onJumpTo: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveInQueue: (Int, Int) -> Unit,
    onClearQueue: () -> Unit,
    onQueueDragActiveChange: (Boolean) -> Unit = {},
    contextTitle: String? = null,
    onSaveQueue: (() -> Unit)? = null,
    // LYRICS props
    lyrics: List<LyricLine>?,
    lyricsTranslation: LyricsTranslationUi,
    lyricsPlayhead: LyricPlayhead,
    lyricsUnavailable: Boolean,
    seekToLyric: (Long) -> Unit,
    lyricsControlsOpen: Boolean,
    onRevealLyricsControls: () -> Unit,
    onHideLyricsControls: () -> Unit,
    particleProgress: State<Float>?,
    lyricsShareEnabled: Boolean,
    lyricPicker: LyricsPicker,
    // RELATED props
    relatedTracks: List<Song>,
    relatedLoading: Boolean,
    onStartRadio: () -> Unit,
    onPlayRelatedSong: (Song) -> Unit,
    onSongMenu: (Song) -> Unit = {},
    dominantColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
) {
    PlayerBackHandler(enabled = true, onBack = onDismiss)

    val haptics = rememberHaptics()
    var dragY by remember { mutableFloatStateOf(0f) }
    val animatedDragY by animateFloatAsState(
        targetValue = dragY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "drawerDrag",
    )

    var queueItemDragging by remember { mutableStateOf(false) }

    val drawerNestedScroll = remember(queueItemDragging) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (queueItemDragging) {
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                return available
            }
        }
    }

    val solidHeaderBg = remember(dominantColor) {
        if (dominantColor != Color.Transparent) {
            Color(
                red = (dominantColor.red * 0.22f + 0.13f * 0.78f).coerceIn(0f, 1f),
                green = (dominantColor.green * 0.22f + 0.12f * 0.78f).coerceIn(0f, 1f),
                blue = (dominantColor.blue * 0.22f + 0.11f * 0.78f).coerceIn(0f, 1f),
                alpha = 1f,
            )
        } else {
            Color(0xFF221E1B)
        }
    }

    val bgBrush = remember(solidHeaderBg) {
        Brush.verticalGradient(
            listOf(
                solidHeaderBg,
                Color(0xFF181513),
                Color(0xFF100F0E),
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.93f)
            .nestedScroll(drawerNestedScroll)
            .offset { IntOffset(0, animatedDragY.roundToInt()) }
            .shadow(20.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(bgBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* swallow clicks inside drawer */ },
            )
            .navigationBarsPadding(),
    ) {
        // Drag handle, top mini player bar, and tab bar enclosed in solid opaque header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(solidHeaderBg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag gesture area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(queueItemDragging) {
                        if (queueItemDragging) return@pointerInput
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragY > 160f) {
                                    dragY = 0f
                                    onDismiss()
                                } else {
                                    dragY = 0f
                                }
                            },
                            onDragCancel = { dragY = 0f },
                        ) { _, delta ->
                            if (!queueItemDragging) {
                                dragY = (dragY + delta).coerceAtLeast(0f)
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                    // Drag pill
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.35f)),
                    )

                    // Top mini song bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = rememberRemoteArtworkUrl(song),
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                ),
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(
                            onClick = onOpenCast,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Cast,
                                contentDescription = "Cast",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                }

                // Tab bar: UP NEXT | LYRICS | RELATED
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(
                        PlayerDrawerTab.UP_NEXT to "UP NEXT",
                        PlayerDrawerTab.LYRICS to "LYRICS",
                        PlayerDrawerTab.RELATED to "RELATED",
                    ).forEach { (tab, label) ->
                        val selected = activeTab == tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTabSelected(tab) }
                                .padding(top = 10.dp, bottom = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                            )
                            Spacer(Modifier.height(8.dp))
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(Color.White)
                                )
                            } else {
                                Spacer(Modifier.height(2.5.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.White.copy(alpha = 0.12f),
            )

            // Tab Body Content with smooth crossfade
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "drawerTabContent",
            ) { tab ->
                when (tab) {
                    PlayerDrawerTab.UP_NEXT -> {
                        val upcomingTracks = remember(queue, currentIndex) {
                            if (queue.isEmpty()) {
                                emptyList()
                            } else {
                                val effectiveCurrent = currentIndex.coerceAtLeast(-1)
                                val seen = HashMap<String, Int>()
                                val occurrence = IntArray(queue.size) { i ->
                                    val id = queue[i].videoId
                                    seen.getOrDefault(id, 0).also { seen[id] = it + 1 }
                                }
                                val usedKeys = HashSet<String>()
                                if (effectiveCurrent >= 0 && effectiveCurrent < queue.size) {
                                    for (i in 0..effectiveCurrent) {
                                        val s = queue[i]
                                        val base = "cur_" + (s.queueEntryId ?: "${s.videoId}#${occurrence[i]}")
                                        var k = base
                                        var r = 1
                                        while (!usedKeys.add(k)) k = base + "~" + r++
                                    }
                                }
                                val startIdx = (effectiveCurrent + 1).coerceIn(0, queue.size)
                                val list = ArrayList<DrawerQueueItem>(queue.size - startIdx)
                                for (i in startIdx until queue.size) {
                                    val s = queue[i]
                                    val base = "upcoming_" + (s.queueEntryId ?: "${s.videoId}#${occurrence[i]}")
                                    var k = base
                                    var r = 1
                                    while (!usedKeys.add(k)) k = base + "~" + r++
                                    list += DrawerQueueItem(timelineIndex = i, song = s, key = k)
                                }
                                list
                            }
                        }
                        val listState = rememberLazyListState()
                        val drag = rememberDrawerQueueDrag(
                            listState = listState,
                            tracks = upcomingTracks,
                            onMove = onMoveInQueue,
                            onActiveChange = { active ->
                                queueItemDragging = active
                                onQueueDragActiveChange(active)
                            },
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 32.dp),
                            ) {
                                // 1. Header: Playing from + Save
                                item(key = "header_playing_from") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Playing from",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                                color = Color.White.copy(alpha = 0.6f),
                                            )
                                            Text(
                                                text = contextTitle?.takeIf { it.isNotBlank() }
                                                    ?: (song.playbackSource ?: "${song.title} Radio"),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                ),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.12f))
                                                .clickable { onSaveQueue?.invoke() }
                                                .padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp),
                                            )
                                            Text(
                                                text = "Save",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                ),
                                                color = Color.White,
                                            )
                                        }
                                    }
                                }

                                // 2. Currently playing song card (highlighted)
                                item(key = "current_song_card") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .combinedClickable(
                                                onClick = {
                                                    // Clicking current song
                                                },
                                                onLongClick = {
                                                    haptics.play(Haptic.Select)
                                                    onSongMenu(song)
                                                },
                                            )
                                            .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(modifier = Modifier.size(46.dp)) {
                                            AsyncImage(
                                                model = rememberRemoteArtworkUrl(song),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.White.copy(alpha = 0.08f)),
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(3.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                            ) {
                                                MiniEqualizerBars(isPlaying = isPlaying)
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                ),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                ExplicitBadge(visible = isSongExplicit(song))
                                                Text(
                                                    text = buildSubtitle(song),
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                    color = Color.White.copy(alpha = 0.65f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                haptics.play(Haptic.Tap)
                                                onSongMenu(song)
                                            },
                                            modifier = Modifier.size(36.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MoreVert,
                                                contentDescription = "Song options",
                                                tint = Color.White.copy(alpha = 0.75f),
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }

                                // 3. Auto-play toggle
                                item(key = "autoplay_row") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Auto-play",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                ),
                                                color = Color.White,
                                            )
                                            Text(
                                                text = "Add similar content for endless listening",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = Color.White.copy(alpha = 0.6f),
                                            )
                                        }
                                        Switch(
                                            checked = autoplayEnabled,
                                            onCheckedChange = { onToggleAutoplay() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color.White.copy(alpha = 0.5f),
                                                uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                                                uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                                            ),
                                        )
                                    }
                                }

                                // 4. Remaining queue tracks with drag-to-reorder support and swipe gestures
                                items(
                                    upcomingTracks,
                                    key = { it.key },
                                ) { item ->
                                    val isBeingDragged = drag.held?.key == item.key
                                    if (isBeingDragged) {
                                        DisposableEffect(item.key) {
                                            onDispose { drag.end(item.key) }
                                        }
                                    }

                                    UpcomingQueueTrackRow(
                                        trackSong = item.song,
                                        itemKey = item.key,
                                        isBeingDragged = isBeingDragged,
                                        isAnyItemDragging = drag.held != null,
                                        onJumpTo = { onJumpTo(item.timelineIndex) },
                                        onSongMenu = { onSongMenu(item.song) },
                                        onPlayNext = { onMoveInQueue(item.timelineIndex, currentIndex + 1) },
                                        onRemoveFromQueue = { onRemoveFromQueue(item.timelineIndex) },
                                        onDragStart = { drag.start(item.key) },
                                        onDragEnd = { drag.end(item.key) },
                                        onDrag = drag::drag,
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = null,
                                            placementSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                        ),
                                    )
                                }
                            }

                            // Floating elevated card for the held/dragged queue item
                            drag.held?.let { heldItem ->
                                val held = heldItem.song
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset { IntOffset(0, drag.heldTop.roundToInt()) }
                                        .padding(horizontal = 8.dp)
                                        .shadow(12.dp, RoundedCornerShape(10.dp))
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2C2723))
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AsyncImage(
                                            model = rememberRemoteArtworkUrl(held),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.White.copy(alpha = 0.08f)),
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = held.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 15.sp,
                                                ),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                ExplicitBadge(visible = isSongExplicit(held))
                                                Text(
                                                    text = buildSubtitle(held),
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier.size(40.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            ReorderHandleIcon()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PlayerDrawerTab.LYRICS -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            if (lyrics.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (lyricsUnavailable) {
                                        Text(
                                            text = "Lyrics not available",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White.copy(alpha = 0.6f),
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            color = Color.White,
                                            strokeWidth = 3.dp,
                                        )
                                    }
                                }
                            } else {
                                LyricsPanel(
                                    lines = lyrics,
                                    subLines = lyricsTranslation.subLines,
                                    trackKey = song.videoId,
                                    playhead = lyricsPlayhead,
                                    looking = !lyricsUnavailable,
                                    isPlaying = isPlaying,
                                    active = activeTab == PlayerDrawerTab.LYRICS,
                                    onSeekToLine = seekToLyric,
                                    controlsOpen = lyricsControlsOpen,
                                    onRevealControls = onRevealLyricsControls,
                                    onHideControls = onHideLyricsControls,
                                    translationProgress = particleProgress,
                                    onScrollingChange = { },
                                    canPick = lyricsShareEnabled,
                                    picking = lyricPicker.picking,
                                    picked = lyricPicker.picks,
                                    onPickLine = lyricPicker.pick,
                                    onTogglePick = lyricPicker.toggle,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }

                    PlayerDrawerTab.RELATED -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            // "Start radio from this song" card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable(onClick = onStartRadio)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Radio,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                                Text(
                                    text = "Start radio from this song",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                    ),
                                    color = Color.White,
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            if (relatedLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(36.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp,
                                    )
                                }
                            } else if (relatedTracks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "No related songs found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.6f),
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    items(relatedTracks) { relatedSong ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .combinedClickable(
                                                    onClick = { onPlayRelatedSong(relatedSong) },
                                                    onLongClick = {
                                                        haptics.play(Haptic.Select)
                                                        onSongMenu(relatedSong)
                                                    },
                                                )
                                                .padding(start = 6.dp, end = 2.dp, top = 6.dp, bottom = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            AsyncImage(
                                                model = rememberRemoteArtworkUrl(relatedSong),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White.copy(alpha = 0.08f)),
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = relatedSong.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 15.sp,
                                                    ),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    ExplicitBadge(visible = isSongExplicit(relatedSong))
                                                    Text(
                                                        text = buildSubtitle(relatedSong),
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                        color = Color.White.copy(alpha = 0.6f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    haptics.play(Haptic.Tap)
                                                    onSongMenu(relatedSong)
                                                },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.MoreVert,
                                                    contentDescription = "Song options",
                                                    tint = Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
