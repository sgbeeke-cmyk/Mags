package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LyricsData
import com.example.data.model.Track
import com.example.player.RepeatMode
import com.example.player.ShuffleMode
import com.example.player.SleepTimerState
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.CustomMediaController
import com.example.ui.components.HiResBadge
import com.example.ui.components.LyricsView
import com.example.ui.components.MediaPlayerSettingsDialog
import com.example.ui.components.RealtimeVisualizerCanvas
import com.example.ui.components.RealtimeVisualizerControlPanel
import com.example.visualizer.VisualizerFrame
import com.example.visualizer.VisualizerStyle
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomSheet(
    track: Track?,
    isPlaying: Boolean,
    playbackPositionMs: Long,
    durationMs: Long,
    lyrics: LyricsData,
    repeatMode: RepeatMode,
    isShuffleEnabled: Boolean,
    isGaplessEnabled: Boolean,
    sleepTimerState: SleepTimerState,
    isAutoPlayNext: Boolean = true,
    shuffleMode: ShuffleMode = if (isShuffleEnabled) ShuffleMode.STANDARD else ShuffleMode.OFF,
    smartShuffleReason: String? = null,
    visualizerFrame: VisualizerFrame = VisualizerFrame(),
    hasRecordPermission: Boolean = false,
    onRequestRecordPermission: () -> Unit = {},
    visualizerStyle: VisualizerStyle = VisualizerStyle.BARS,
    onSelectVisualizerStyle: (VisualizerStyle) -> Unit = {},
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekForward: (() -> Unit)? = null,
    onSeekBack: (() -> Unit)? = null,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onToggleAutoPlayNext: (Boolean) -> Unit = {},
    onToggleGapless: (Boolean) -> Unit = {},
    onStartSleepTimerPreset: (Int) -> Unit = {},
    onStartSleepTimerEndOfTrack: () -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onInspectTrack: (Track) -> Unit,
    onDismiss: () -> Unit
) {
    if (track == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showLyricsMode by remember { mutableStateOf(false) }
    var showVisualizerMode by remember { mutableStateOf(false) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }

    if (isSettingsDialogOpen) {
        MediaPlayerSettingsDialog(
            sleepTimerState = sleepTimerState,
            isAutoPlayNext = isAutoPlayNext,
            isGaplessEnabled = isGaplessEnabled,
            onToggleAutoPlayNext = onToggleAutoPlayNext,
            onToggleGapless = onToggleGapless,
            onOpenFullSleepTimerDialog = {
                isSettingsDialogOpen = false
                onOpenSleepTimer()
            },
            onStartSleepTimerPreset = onStartSleepTimerPreset,
            onStartSleepTimerEndOfTrack = onStartSleepTimerEndOfTrack,
            onCancelSleepTimer = onCancelSleepTimer,
            onOpenEqualizer = {
                isSettingsDialogOpen = false
                onOpenEqualizer()
            },
            onDismiss = { isSettingsDialogOpen = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AuraDarkBackground,
        dragHandle = null,
        modifier = Modifier
            .testTag("player_sheet")
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = AuraTextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM LIBRARY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = AuraTextMuted
                    )
                    Text(
                        text = track.album,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sleepTimerState.isActive) {
                        Box(
                            modifier = Modifier
                                .testTag("sleep_timer_active_badge")
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraCyanPrimary.copy(alpha = 0.15f))
                                .border(1.dp, AuraCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenSleepTimer)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = AuraCyanPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sleepTimerState.formattedRemaining,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraCyanPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = { onInspectTrack(track) },
                        modifier = Modifier.testTag("track_details_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "View audio specifications",
                            tint = AuraTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { isSettingsDialogOpen = true },
                        modifier = Modifier.testTag("player_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Media player settings",
                            tint = AuraTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Area: Album Artwork, Visualizer Display, or Lyrics Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AuraDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (showVisualizerMode) {
                    // Dedicated Full-Card Real-time Audio Visualizer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F141E))
                            .border(1.dp, AuraDarkBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top status inside visualizer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isPlaying) AuraCyanPrimary else AuraTextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (visualizerFrame.isHardwareSynced) "MEDIA3 AUDIO SESSION SYNC" else "DSP SPECTRUM MONITOR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = if (visualizerFrame.isHardwareSynced) AuraCyanPrimary else AuraTextSecondary
                                    )
                                }

                                if (!hasRecordPermission) {
                                    Text(
                                        text = "Tap for HW Sync",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFA726),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x33FFA726))
                                            .clickable { onRequestRecordPermission() }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Visualizer Canvas occupying available height
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                RealtimeVisualizerCanvas(
                                    frame = visualizerFrame,
                                    isPlaying = isPlaying,
                                    style = visualizerStyle,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Style selection chips inside card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                VisualizerStyle.values().forEach { style ->
                                    val isSelected = style == visualizerStyle
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AuraCyanPrimary.copy(alpha = 0.2f) else AuraDarkCard)
                                            .border(
                                                1.dp,
                                                if (isSelected) AuraCyanPrimary.copy(alpha = 0.6f) else AuraDarkBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSelectVisualizerStyle(style) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = style.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) AuraCyanPrimary else AuraTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (showLyricsMode) {
                    LyricsView(
                        lyrics = lyrics,
                        currentPositionMs = if (isDraggingSlider) sliderDragPosition.toLong() else playbackPositionMs,
                        onSeekTo = onSeekTo,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Album Artwork Card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showLyricsMode = true }
                            .shadow(24.dp, shape = RoundedCornerShape(20.dp), spotColor = AuraCyanPrimary)
                            .background(AuraDarkSurfaceVariant)
                            .border(1.dp, AuraDarkBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            track.albumArtRes != null -> {
                                Image(
                                    painter = painterResource(id = track.albumArtRes),
                                    contentDescription = "Cover art for ${track.title}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            track.albumArtUri != null -> {
                                AsyncImage(
                                    model = track.albumArtUri,
                                    contentDescription = "Cover art for ${track.title}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = AuraTextMuted,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }

                        // Subtle bottom overlay with visualizer bars + tap hint
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC000000))
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showLyricsMode = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lyrics,
                                        contentDescription = null,
                                        tint = AuraCyanPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Lyrics",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }

                                // Interactive mini spectrum directly overlaid on artwork
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showVisualizerMode = true }
                                        .background(Color(0x55000000))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    AudioVisualizer(
                                        isPlaying = isPlaying,
                                        visualizerFrame = visualizerFrame,
                                        barCount = 6,
                                        maxHeight = 14.dp,
                                        barWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Visualizer",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AuraCyanPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Track Info Row: Title, Artist, Hi-Res Badge & Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = AuraTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        color = AuraTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HiResBadge(track = track)
                        if (isGaplessEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .border(0.8.dp, AuraCyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .background(AuraCyanPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GAPLESS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraCyanPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    if (shuffleMode == ShuffleMode.SMART) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AuraCyanPrimary.copy(alpha = 0.12f))
                                .border(1.dp, AuraCyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AuraCyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (!smartShuffleReason.isNullOrBlank()) "Smart: $smartShuffleReason" else "Smart Shuffle: Discovery Flow",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuraCyanPrimary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite(track) },
                    modifier = Modifier.testTag("player_favorite_button")
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (track.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (track.isFavorite) Color(0xFFFF5252) else AuraTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom MediaController UI Component (Media3 ExoPlayer Session)
            CustomMediaController(
                track = track,
                isPlaying = isPlaying,
                playbackPositionMs = playbackPositionMs,
                durationMs = durationMs,
                onTogglePlayPause = onTogglePlayPause,
                onSeekTo = onSeekTo,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeekForward = onSeekForward ?: { onSeekTo((playbackPositionMs + 10_000L).coerceAtMost(durationMs)) },
                onSeekBack = onSeekBack ?: { onSeekTo((playbackPositionMs - 10_000L).coerceAtLeast(0L)) },
                showTrackHeader = false,
                isMediaSessionActive = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Audio Modes Row: Shuffle, Repeat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Mode Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (shuffleMode != ShuffleMode.OFF) AuraCyanPrimary.copy(alpha = 0.15f) else AuraDarkCard)
                        .border(
                            1.dp,
                            if (shuffleMode != ShuffleMode.OFF) AuraCyanPrimary.copy(alpha = 0.5f) else AuraDarkBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(onClick = onToggleShuffle)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("player_shuffle_button")
                ) {
                    Icon(
                        imageVector = if (shuffleMode == ShuffleMode.SMART) Icons.Default.AutoAwesome else Icons.Default.Shuffle,
                        contentDescription = "Shuffle mode: ${shuffleMode.label}",
                        tint = if (shuffleMode != ShuffleMode.OFF) AuraCyanPrimary else AuraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shuffle: ${shuffleMode.label}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (shuffleMode != ShuffleMode.OFF) AuraCyanPrimary else AuraTextMuted
                    )
                }

                // Repeat Mode Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (repeatMode != RepeatMode.OFF) AuraCyanPrimary.copy(alpha = 0.15f) else AuraDarkCard)
                        .border(
                            1.dp,
                            if (repeatMode != RepeatMode.OFF) AuraCyanPrimary.copy(alpha = 0.5f) else AuraDarkBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(onClick = onToggleRepeat)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("player_repeat_button")
                ) {
                    val icon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    Icon(
                        imageVector = icon,
                        contentDescription = "Toggle repeat: $repeatMode",
                        tint = if (repeatMode != RepeatMode.OFF) AuraCyanPrimary else AuraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (repeatMode) {
                            RepeatMode.OFF -> "Repeat Off"
                            RepeatMode.ALL -> "Repeat All"
                            RepeatMode.ONE -> "Repeat One"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (repeatMode != RepeatMode.OFF) AuraCyanPrimary else AuraTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Quick Utility Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Real-time Audio Visualizer Mode
                IconButton(
                    onClick = {
                        showVisualizerMode = !showVisualizerMode
                        if (showVisualizerMode) showLyricsMode = false
                    },
                    modifier = Modifier.testTag("toggle_visualizer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Toggle Real-time Audio Visualizer",
                        tint = if (showVisualizerMode) AuraCyanPrimary else AuraTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Toggle Lyrics Mode
                IconButton(
                    onClick = {
                        showLyricsMode = !showLyricsMode
                        if (showLyricsMode) showVisualizerMode = false
                    },
                    modifier = Modifier.testTag("toggle_lyrics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Toggle embedded lyrics view",
                        tint = if (showLyricsMode) AuraCyanPrimary else AuraTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Open Equalizer Sheet
                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier.testTag("open_equalizer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Open Equalizer",
                        tint = AuraTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Open Queue Drawer
                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier.testTag("open_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Open playback queue",
                        tint = AuraTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Sleep Timer
                IconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier.testTag("sleep_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep timer",
                        tint = if (sleepTimerState.isActive) AuraCyanPrimary else AuraTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
