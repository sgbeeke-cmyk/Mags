package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.player.ShuffleMode
import com.example.ui.components.ApkExportDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SleepTimerDialog
import com.example.ui.screens.DownloadScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.PlayerBottomSheet
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.QueueBottomSheet
import com.example.ui.screens.TrackDetailsDialog
import com.example.ui.screens.TracksScreen
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.viewmodel.MusicViewModel

enum class AuraTab {
    LIBRARY,
    PLAYLISTS,
    EQUALIZER,
    DOWNLOAD
}

@Composable
fun AuraMusicApp(
    viewModel: MusicViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(AuraTab.LIBRARY) }
    var isPlayerExpanded by remember { mutableStateOf(false) }
    var isQueueExpanded by remember { mutableStateOf(false) }
    var isSleepTimerDialogOpen by remember { mutableStateOf(false) }
    var isApkDialogOpen by remember { mutableStateOf(false) }

    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val hiResTracks by viewModel.hiResTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()

    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackPositionMs by viewModel.playbackPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val currentLyrics by viewModel.currentLyrics.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val queueIndex by viewModel.queueIndex.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val shuffleMode by viewModel.shuffleMode.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val smartShuffleReasons by viewModel.smartShuffleReasons.collectAsStateWithLifecycle()
    val isGaplessEnabled by viewModel.isGaplessEnabled.collectAsStateWithLifecycle()
    val isAutoPlayNext by viewModel.isAutoPlayNext.collectAsStateWithLifecycle()
    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()

    val eqBands by viewModel.equalizerBands.collectAsStateWithLifecycle()
    val bassBoost by viewModel.bassBoostLevel.collectAsStateWithLifecycle()
    val virtualizer by viewModel.virtualizerLevel.collectAsStateWithLifecycle()
    val isFxEnabled by viewModel.isFxEnabled.collectAsStateWithLifecycle()
    val currentPresetName by viewModel.currentPresetName.collectAsStateWithLifecycle()
    val presets by viewModel.equalizerPresets.collectAsStateWithLifecycle()
    val audioSessionId by viewModel.audioSessionId.collectAsStateWithLifecycle()

    val inspectingTrack by viewModel.inspectingTrack.collectAsStateWithLifecycle()

    // Real-time Visualizer states
    val visualizerFrame by viewModel.visualizerFrame.collectAsStateWithLifecycle()
    val visualizerStyle by viewModel.visualizerStyle.collectAsStateWithLifecycle()
    val visualizerHasPermission by viewModel.visualizerHasPermission.collectAsStateWithLifecycle()

    // RECORD_AUDIO permission launcher for hardware Media3 AudioSessionId visualizer
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.refreshAudioPermissionState()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("aura_app_scaffold"),
        containerColor = AuraDarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (currentTab != AuraTab.DOWNLOAD && !isPlayerExpanded) {
                ExtendedFloatingActionButton(
                    onClick = { isApkDialogOpen = true },
                    modifier = Modifier
                        .testTag("floating_download_apk_btn")
                        .padding(bottom = if (currentTrack != null) 70.dp else 4.dp),
                    containerColor = AuraCyanPrimary,
                    contentColor = Color.Black,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Download APK",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                    },
                    text = {
                        Text(
                            text = "Download APK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuraDarkBackground)
            ) {
                // Mini Player docked directly above bottom bar
                if (currentTrack != null) {
                    MiniPlayer(
                        track = currentTrack!!,
                        isPlaying = isPlaying,
                        playbackPositionMs = playbackPositionMs,
                        durationMs = durationMs,
                        sleepTimerRemaining = if (sleepTimerState.isActive) sleepTimerState.formattedRemaining else null,
                        onSleepTimerClick = { isSleepTimerDialogOpen = true },
                        visualizerFrame = visualizerFrame,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSkipNext = { viewModel.skipToNext() },
                        onExpand = { isPlayerExpanded = true }
                    )
                }

                // Bottom Navigation
                NavigationBar(
                    containerColor = AuraDarkCard,
                    modifier = Modifier
                        .testTag("bottom_nav_bar")
                        .fillMaxWidth()
                        .border(1.dp, AuraDarkBorder)
                ) {
                    NavigationBarItem(
                        selected = currentTab == AuraTab.LIBRARY,
                        onClick = { currentTab = AuraTab.LIBRARY },
                        modifier = Modifier.testTag("nav_library"),
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AuraTab.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                contentDescription = "FLAC Library"
                            )
                        },
                        label = { Text("Library", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraCyanPrimary,
                            selectedTextColor = AuraCyanPrimary,
                            indicatorColor = Color(0xFF1B2333),
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AuraTab.PLAYLISTS,
                        onClick = { currentTab = AuraTab.PLAYLISTS },
                        modifier = Modifier.testTag("nav_playlists"),
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AuraTab.PLAYLISTS) Icons.Filled.PlaylistPlay else Icons.Outlined.PlaylistPlay,
                                contentDescription = "Playlists"
                            )
                        },
                        label = { Text("Playlists", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraCyanPrimary,
                            selectedTextColor = AuraCyanPrimary,
                            indicatorColor = Color(0xFF1B2333),
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AuraTab.EQUALIZER,
                        onClick = { currentTab = AuraTab.EQUALIZER },
                        modifier = Modifier.testTag("nav_equalizer"),
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AuraTab.EQUALIZER) Icons.Filled.Equalizer else Icons.Outlined.Equalizer,
                                contentDescription = "Equalizer"
                            )
                        },
                        label = { Text("Equalizer", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraCyanPrimary,
                            selectedTextColor = AuraCyanPrimary,
                            indicatorColor = Color(0xFF1B2333),
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == AuraTab.DOWNLOAD,
                        onClick = { currentTab = AuraTab.DOWNLOAD },
                        modifier = Modifier.testTag("nav_download"),
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AuraTab.DOWNLOAD) Icons.Filled.Download else Icons.Outlined.Download,
                                contentDescription = "Download APK"
                            )
                        },
                        label = { Text("Download APK", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraCyanPrimary,
                            selectedTextColor = AuraCyanPrimary,
                            indicatorColor = Color(0xFF1B2333),
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AuraTab.LIBRARY -> {
                    TracksScreen(
                        allTracks = allTracks,
                        hiResTracks = hiResTracks,
                        favoriteTracks = favoriteTracks,
                        recentlyPlayedTracks = recentlyPlayed,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        playlists = playlists,
                        onTrackClick = { track, list ->
                            viewModel.playTrack(track, list)
                            isPlayerExpanded = true
                        },
                        onPlayAll = { list ->
                            viewModel.setShuffleMode(ShuffleMode.OFF)
                            viewModel.playTrack(list.first(), list, 0)
                        },
                        onShuffleAll = { list ->
                            viewModel.setShuffleMode(ShuffleMode.STANDARD)
                            val shuffled = list.shuffled()
                            viewModel.playTrack(shuffled.first(), shuffled, 0)
                        },
                        onSmartShuffleAll = { list ->
                            viewModel.playSmartShuffle(list)
                            isPlayerExpanded = true
                        },
                        onToggleFavorite = { track -> viewModel.toggleFavorite(track) },
                        onAddToQueueNext = { track -> viewModel.addToQueueNext(track) },
                        onAddToPlaylist = { playlistId, trackId -> viewModel.addTrackToPlaylist(playlistId, trackId) },
                        onInspectTrack = { track -> viewModel.inspectTrack(track) },
                        onImportTracks = { uris -> viewModel.importFiles(uris) },
                        sleepTimerState = sleepTimerState,
                        onOpenSleepTimer = { isSleepTimerDialogOpen = true },
                        visualizerFrame = visualizerFrame
                    )
                }
                AuraTab.PLAYLISTS -> {
                    PlaylistsScreen(
                        playlists = playlists,
                        selectedPlaylist = selectedPlaylist,
                        selectedPlaylistTracks = selectedPlaylistTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onSelectPlaylist = { pl -> viewModel.selectPlaylist(pl) },
                        onClearSelectedPlaylist = { viewModel.clearSelectedPlaylist() },
                        onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                        onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                        onPlayTrack = { track, list ->
                            viewModel.playTrack(track, list)
                            isPlayerExpanded = true
                        },
                        onPlayAll = { list ->
                            viewModel.setShuffleMode(ShuffleMode.OFF)
                            viewModel.playTrack(list.first(), list, 0)
                        },
                        onShuffleAll = { list ->
                            viewModel.setShuffleMode(ShuffleMode.STANDARD)
                            val shuffled = list.shuffled()
                            viewModel.playTrack(shuffled.first(), shuffled, 0)
                        },
                        onSmartShuffleAll = { list ->
                            viewModel.playSmartShuffle(list)
                            isPlayerExpanded = true
                        },
                        onRemoveTrackFromPlaylist = { plId, trId -> viewModel.removeTrackFromPlaylist(plId, trId) },
                        allLocalTracks = allTracks,
                        onAddTrackToPlaylist = { plId, trId -> viewModel.addTrackToPlaylist(plId, trId) },
                        onAddTracksToPlaylist = { plId, trIds -> viewModel.addTracksToPlaylist(plId, trIds) },
                        onMoveTrack = { plId, from, to -> viewModel.moveTrackInPlaylist(plId, from, to) },
                        onReorderTracks = { plId, trIds -> viewModel.reorderPlaylistTracks(plId, trIds) }
                    )
                }
                AuraTab.EQUALIZER -> {
                    EqualizerScreen(
                        bands = eqBands,
                        bassBoost = bassBoost,
                        virtualizer = virtualizer,
                        isEnabled = isFxEnabled,
                        currentPresetName = currentPresetName,
                        presets = presets,
                        isGaplessEnabled = isGaplessEnabled,
                        onToggleFxEnabled = { viewModel.toggleFxEnabled(it) },
                        onBandGainChange = { index, gain -> viewModel.setBandGain(index, gain) },
                        onBassBoostChange = { viewModel.setBassBoost(it) },
                        onVirtualizerChange = { viewModel.setVirtualizer(it) },
                        onApplyPreset = { viewModel.applyPreset(it) },
                        onToggleGapless = { viewModel.setGaplessEnabled(it) },
                        audioSessionId = audioSessionId,
                        onResetToFlat = { viewModel.resetEqualizerFlat() },
                        visualizerFrame = visualizerFrame,
                        isPlaying = isPlaying,
                        hasRecordPermission = visualizerHasPermission,
                        onRequestRecordPermission = {
                            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        visualizerStyle = visualizerStyle,
                        onSelectVisualizerStyle = { viewModel.setVisualizerStyle(it) }
                    )
                }
                AuraTab.DOWNLOAD -> {
                    DownloadScreen()
                }
            }
        }
    }

    // Full Player BottomSheet
    if (isPlayerExpanded && currentTrack != null) {
        PlayerBottomSheet(
            track = currentTrack,
            isPlaying = isPlaying,
            playbackPositionMs = playbackPositionMs,
            durationMs = durationMs,
            lyrics = currentLyrics,
            repeatMode = repeatMode,
            isShuffleEnabled = isShuffleEnabled,
            isGaplessEnabled = isGaplessEnabled,
            sleepTimerState = sleepTimerState,
            isAutoPlayNext = isAutoPlayNext,
            shuffleMode = shuffleMode,
            smartShuffleReason = currentTrack?.id?.let { smartShuffleReasons[it] },
            visualizerFrame = visualizerFrame,
            hasRecordPermission = visualizerHasPermission,
            onRequestRecordPermission = {
                recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            visualizerStyle = visualizerStyle,
            onSelectVisualizerStyle = { viewModel.setVisualizerStyle(it) },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onSeekTo = { viewModel.seekTo(it) },
            onSeekForward = { viewModel.seekForward() },
            onSeekBack = { viewModel.seekBack() },
            onSkipNext = { viewModel.skipToNext() },
            onSkipPrevious = { viewModel.skipToPrevious() },
            onToggleRepeat = { viewModel.toggleRepeatMode() },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onOpenEqualizer = {
                isPlayerExpanded = false
                currentTab = AuraTab.EQUALIZER
            },
            onOpenQueue = { isQueueExpanded = true },
            onOpenSleepTimer = { isSleepTimerDialogOpen = true },
            onToggleAutoPlayNext = { viewModel.setAutoPlayNext(it) },
            onToggleGapless = { viewModel.setGaplessEnabled(it) },
            onStartSleepTimerPreset = { viewModel.startSleepTimer(it, false) },
            onStartSleepTimerEndOfTrack = { viewModel.startSleepTimerEndOfTrack() },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() },
            onInspectTrack = { viewModel.inspectTrack(it) },
            onDismiss = { isPlayerExpanded = false }
        )
    }

    // Queue Sheet
    if (isQueueExpanded) {
        QueueBottomSheet(
            queue = queue,
            currentIndex = queueIndex,
            isPlaying = isPlaying,
            shuffleMode = shuffleMode,
            smartShuffleReasons = smartShuffleReasons,
            onSetShuffleMode = { viewModel.setShuffleMode(it) },
            onTrackClick = { index ->
                viewModel.playbackManager.playTrackList(queue, index, preserveOrderAsIs = true)
            },
            onRemoveFromQueue = { viewModel.removeFromQueue(it) },
            onClearQueue = { viewModel.clearQueue() },
            onDismiss = { isQueueExpanded = false }
        )
    }

    // Track Specs Dialog
    inspectingTrack?.let { track ->
        TrackDetailsDialog(
            track = track,
            onDismiss = { viewModel.inspectTrack(null) }
        )
    }

    // Sleep Timer Dialog
    if (isSleepTimerDialogOpen) {
        SleepTimerDialog(
            sleepTimerState = sleepTimerState,
            onStartTimer = { minutes, finishTrack ->
                viewModel.startSleepTimer(minutes, finishTrack)
            },
            onStartEndOfTrack = {
                viewModel.startSleepTimerEndOfTrack()
            },
            onAddMinutes = { extraMinutes ->
                viewModel.addSleepTimerMinutes(extraMinutes)
            },
            onCancelTimer = {
                viewModel.cancelSleepTimer()
            },
            onToggleFinishTrack = { finish ->
                viewModel.setSleepTimerFinishTrack(finish)
            },
            onDismiss = { isSleepTimerDialogOpen = false }
        )
    }

    // APK Download / Export Dialog
    if (isApkDialogOpen) {
        ApkExportDialog(
            onDismissRequest = { isApkDialogOpen = false }
        )
    }
}
