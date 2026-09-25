package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.player.SleepTimerState
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.HiResBadge
import com.example.visualizer.VisualizerFrame
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

enum class TrackFilter {
    ALL,
    HI_RES,
    FAVORITES,
    RECENT
}

@Composable
fun TracksScreen(
    allTracks: List<Track>,
    hiResTracks: List<Track>,
    favoriteTracks: List<Track>,
    recentlyPlayedTracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    playlists: List<Playlist>,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onShuffleAll: (List<Track>) -> Unit,
    onSmartShuffleAll: ((List<Track>) -> Unit)? = null,
    onToggleFavorite: (Track) -> Unit,
    onAddToQueueNext: (Track) -> Unit,
    onAddToPlaylist: (Long, String) -> Unit,
    onInspectTrack: (Track) -> Unit,
    onImportTracks: (List<Uri>) -> Unit,
    sleepTimerState: SleepTimerState? = null,
    onOpenSleepTimer: (() -> Unit)? = null,
    visualizerFrame: VisualizerFrame? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(TrackFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportTracks(uris)
        }
    }

    val currentList = when (selectedFilter) {
        TrackFilter.ALL -> allTracks
        TrackFilter.HI_RES -> hiResTracks
        TrackFilter.FAVORITES -> favoriteTracks
        TrackFilter.RECENT -> recentlyPlayedTracks
    }

    val filteredTracks = if (searchQuery.isBlank()) {
        currentList
    } else {
        currentList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .testTag("tracks_screen")
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Lossless Library",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary
                )
                Text(
                    text = "${allTracks.size} local tracks • Gapless FLAC Ready",
                    fontSize = 12.sp,
                    color = AuraTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sleep Timer button / badge in library header
                if (onOpenSleepTimer != null) {
                    if (sleepTimerState?.isActive == true) {
                        Box(
                            modifier = Modifier
                                .testTag("library_sleep_timer_badge")
                                .clip(RoundedCornerShape(10.dp))
                                .background(AuraCyanPrimary.copy(alpha = 0.15f))
                                .border(1.dp, AuraCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { onOpenSleepTimer() }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = "Sleep timer active",
                                    tint = AuraCyanPrimary,
                                    modifier = Modifier.size(14.dp)
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
                    } else {
                        IconButton(
                            onClick = onOpenSleepTimer,
                            modifier = Modifier
                                .testTag("library_sleep_timer_button")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Sleep timer",
                                tint = AuraTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Import FLAC Files Button
                Button(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("audio/*", "application/octet-stream"))
                    },
                    modifier = Modifier.testTag("import_audio_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraDarkCard,
                        contentColor = AuraCyanPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .testTag("search_field")
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = { Text("Search FLAC tracks, artists, albums...", color = AuraTextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AuraTextMuted)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AuraDarkSurface,
                unfocusedContainerColor = AuraDarkSurface,
                focusedBorderColor = AuraCyanPrimary,
                unfocusedBorderColor = AuraDarkBorder,
                focusedTextColor = AuraTextPrimary,
                unfocusedTextColor = AuraTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    label = "All Tracks (${allTracks.size})",
                    isSelected = selectedFilter == TrackFilter.ALL,
                    onClick = { selectedFilter = TrackFilter.ALL }
                )
            }
            item {
                FilterChipItem(
                    label = "Hi-Res FLAC (${hiResTracks.size})",
                    isSelected = selectedFilter == TrackFilter.HI_RES,
                    onClick = { selectedFilter = TrackFilter.HI_RES }
                )
            }
            item {
                FilterChipItem(
                    label = "Favorites (${favoriteTracks.size})",
                    isSelected = selectedFilter == TrackFilter.FAVORITES,
                    onClick = { selectedFilter = TrackFilter.FAVORITES }
                )
            }
            item {
                FilterChipItem(
                    label = "Recently Played",
                    isSelected = selectedFilter == TrackFilter.RECENT,
                    onClick = { selectedFilter = TrackFilter.RECENT }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Play All, Shuffle, & Smart Shuffle
        if (filteredTracks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onPlayAll(filteredTracks) },
                    modifier = Modifier
                        .testTag("play_all_button")
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraCyanPrimary,
                        contentColor = AuraDarkBackground
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { onShuffleAll(filteredTracks) },
                    modifier = Modifier
                        .testTag("shuffle_all_button")
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraDarkCard,
                        contentColor = AuraTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                Button(
                    onClick = { onSmartShuffleAll?.invoke(filteredTracks) ?: onShuffleAll(filteredTracks) },
                    modifier = Modifier
                        .testTag("smart_shuffle_button")
                        .weight(1.35f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraCyanPrimary.copy(alpha = 0.15f),
                        contentColor = AuraCyanPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraCyanPrimary.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = AuraCyanPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Smart Shuffle", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AuraCyanPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Track List
        if (filteredTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AuraTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tracks in this view",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuraTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .testTag("track_list")
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTracks, key = { it.id }) { track ->
                    val isCurrent = currentTrack?.id == track.id
                    TrackItemRow(
                        track = track,
                        isCurrent = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        visualizerFrame = if (isCurrent) visualizerFrame else null,
                        playlists = playlists,
                        onClick = { onTrackClick(track, filteredTracks) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onAddToQueueNext = { onAddToQueueNext(track) },
                        onAddToPlaylist = { playlistId -> onAddToPlaylist(playlistId, track.id) },
                        onInspectTrack = { onInspectTrack(track) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isSelected) AuraCyanPrimary else AuraDarkBorder,
                RoundedCornerShape(8.dp)
            )
            .background(if (isSelected) AuraCyanPrimary.copy(alpha = 0.15f) else AuraDarkCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AuraCyanPrimary else AuraTextSecondary
        )
    }
}

@Composable
private fun TrackItemRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    visualizerFrame: VisualizerFrame? = null,
    playlists: List<Playlist>,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueueNext: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onInspectTrack: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistSubmenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .testTag("track_item_${track.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) AuraDarkSurfaceVariant else AuraDarkCard)
            .border(
                1.dp,
                if (isCurrent) AuraCyanPrimary.copy(alpha = 0.4f) else AuraDarkBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1B2232)),
            contentAlignment = Alignment.Center
        ) {
            when {
                track.albumArtRes != null -> {
                    Image(
                        painter = painterResource(id = track.albumArtRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                track.albumArtUri != null -> {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AuraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        AudioVisualizer(
                            isPlaying = true,
                            visualizerFrame = visualizerFrame,
                            barCount = 4,
                            maxHeight = 16.dp,
                            barWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.title,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCurrent) AuraCyanPrimary else AuraTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                HiResBadge(track = track, compact = true)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                fontSize = 12.sp,
                color = AuraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Duration
        Text(
            text = formatDuration(track.durationMs),
            fontSize = 12.sp,
            color = AuraTextMuted
        )

        // Overflow Menu Button
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.testTag("track_menu_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Track options",
                    tint = AuraTextSecondary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {
                    showMenu = false
                    showPlaylistSubmenu = false
                },
                modifier = Modifier.background(AuraDarkSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("Play Next") },
                    onClick = {
                        onAddToQueueNext()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (track.isFavorite) "Remove Favorite" else "Add to Favorites") },
                    onClick = {
                        onToggleFavorite()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist...") },
                    onClick = { showPlaylistSubmenu = !showPlaylistSubmenu }
                )
                if (showPlaylistSubmenu) {
                    playlists.forEach { pl ->
                        DropdownMenuItem(
                            text = { Text("  ↳ ${pl.name}", color = AuraCyanPrimary) },
                            onClick = {
                                onAddToPlaylist(pl.id)
                                showMenu = false
                                showPlaylistSubmenu = false
                            }
                        )
                    }
                }
                DropdownMenuItem(
                    text = { Text("Audio Specifications") },
                    onClick = {
                        onInspectTrack()
                        showMenu = false
                    }
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
