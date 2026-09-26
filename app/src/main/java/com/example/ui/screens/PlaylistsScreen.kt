package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.ui.components.HiResBadge
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraGoldTertiary
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletSecondary

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    selectedPlaylist: Playlist?,
    selectedPlaylistTracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onSelectPlaylist: (Playlist) -> Unit,
    onClearSelectedPlaylist: () -> Unit,
    onCreatePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onShuffleAll: (List<Track>) -> Unit,
    onSmartShuffleAll: ((List<Track>) -> Unit)? = null,
    onRemoveTrackFromPlaylist: (Long, String) -> Unit,
    allLocalTracks: List<Track> = emptyList(),
    onAddTrackToPlaylist: (Long, String) -> Unit = { _, _ -> },
    onAddTracksToPlaylist: (Long, List<String>) -> Unit = { _, _ -> },
    onMoveTrack: (Long, Int, Int) -> Unit = { _, _, _ -> },
    onReorderTracks: (Long, List<String>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDesc by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = AuraDarkSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "New FLAC Playlist",
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("playlist_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AuraDarkCard,
                            unfocusedContainerColor = AuraDarkCard,
                            focusedBorderColor = AuraCyanPrimary,
                            unfocusedBorderColor = AuraDarkBorder,
                            focusedTextColor = AuraTextPrimary,
                            unfocusedTextColor = AuraTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = newPlaylistDesc,
                        onValueChange = { newPlaylistDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("playlist_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AuraDarkCard,
                            unfocusedContainerColor = AuraDarkCard,
                            focusedBorderColor = AuraCyanPrimary,
                            unfocusedBorderColor = AuraDarkBorder,
                            focusedTextColor = AuraTextPrimary,
                            unfocusedTextColor = AuraTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim(), newPlaylistDesc.trim())
                            newPlaylistName = ""
                            newPlaylistDesc = ""
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_playlist_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyanPrimary, contentColor = AuraDarkBackground)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = AuraTextMuted)
                }
            }
        )
    }

    if (selectedPlaylist != null) {
        PlaylistDetailContent(
            playlist = selectedPlaylist,
            tracks = selectedPlaylistTracks,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            onBack = onClearSelectedPlaylist,
            onPlayTrack = { track -> onPlayTrack(track, selectedPlaylistTracks) },
            onPlayAll = { onPlayAll(selectedPlaylistTracks) },
            onShuffleAll = { onShuffleAll(selectedPlaylistTracks) },
            onSmartShuffleAll = onSmartShuffleAll?.let { cb -> { cb(selectedPlaylistTracks) } },
            onRemoveTrack = { trackId -> onRemoveTrackFromPlaylist(selectedPlaylist.id, trackId) },
            onDeletePlaylist = {
                onDeletePlaylist(selectedPlaylist.id)
                onClearSelectedPlaylist()
            },
            allLocalTracks = allLocalTracks,
            onAddTrack = { trackId -> onAddTrackToPlaylist(selectedPlaylist.id, trackId) },
            onAddTracks = { trackIds -> onAddTracksToPlaylist(selectedPlaylist.id, trackIds) },
            onMoveTrack = { from, to -> onMoveTrack(selectedPlaylist.id, from, to) },
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier
                .testTag("playlists_screen")
                .fillMaxSize()
                .background(AuraDarkBackground)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playlists",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AuraTextPrimary
                    )
                    Text(
                        text = "${playlists.size} curated FLAC collections",
                        fontSize = 12.sp,
                        color = AuraTextSecondary
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_playlist_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraCyanPrimary,
                        contentColor = AuraDarkBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Playlist",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Playlist", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = AuraTextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No Playlists Yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary
                        )
                        Text(
                            text = "Create custom FLAC playlists to organize your studio recordings and hi-res albums.",
                            fontSize = 13.sp,
                            color = AuraTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraCyanPrimary,
                                contentColor = AuraDarkBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create First Playlist", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("playlists_grid"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistGridCard(
                            playlist = playlist,
                            onClick = { onSelectPlaylist(playlist) },
                            onDelete = { onDeletePlaylist(playlist.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistGridCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val accentColor = remember(playlist.accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(playlist.accentColorHex))
        } catch (_: Exception) {
            AuraCyanPrimary
        }
    }

    Card(
        modifier = modifier
            .testTag("playlist_card_${playlist.id}")
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with icon and options menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(accentColor.copy(alpha = 0.35f), AuraDarkSurface)
                            )
                        )
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp).testTag("playlist_menu_${playlist.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Playlist Options",
                            tint = AuraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(AuraDarkSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Playlist", color = Color(0xFFFF5252)) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = playlist.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${playlist.trackCount} tracks",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AuraCyanPrimary
            )

            if (playlist.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = playlist.description,
                    fontSize = 11.sp,
                    color = AuraTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailContent(
    playlist: Playlist,
    tracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayTrack: (Track) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onSmartShuffleAll: (() -> Unit)? = null,
    onRemoveTrack: (String) -> Unit,
    onDeletePlaylist: () -> Unit,
    allLocalTracks: List<Track> = emptyList(),
    onAddTrack: (String) -> Unit = {},
    onAddTracks: (List<String>) -> Unit = {},
    onMoveTrack: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddTracksDialog by remember { mutableStateOf(false) }

    if (showAddTracksDialog) {
        AddTracksToPlaylistDialog(
            playlistName = playlist.name,
            allLocalTracks = allLocalTracks,
            existingTrackIds = remember(tracks) { tracks.map { it.id }.toSet() },
            onDismiss = { showAddTracksDialog = false },
            onAddSelectedTracks = { selectedIds ->
                onAddTracks(selectedIds)
                showAddTracksDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .testTag("playlist_detail_screen")
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("playlist_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to playlists",
                    tint = AuraTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tracks.size} tracks • Seamless Gapless",
                    fontSize = 12.sp,
                    color = AuraTextSecondary
                )
            }

            // Quick Add Local Tracks Button in Header
            IconButton(
                onClick = { showAddTracksDialog = true },
                modifier = Modifier.testTag("playlist_header_add_tracks_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Add local tracks",
                    tint = AuraCyanPrimary
                )
            }

            // Delete playlist button
            IconButton(
                onClick = onDeletePlaylist,
                modifier = Modifier.testTag("playlist_delete_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete playlist",
                    tint = AuraTextMuted
                )
            }
        }

        // Action Buttons Row: Play, Shuffle, Smart Shuffle
        if (tracks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("playlist_play_all"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyanPrimary, contentColor = AuraDarkBackground)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onShuffleAll,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("playlist_shuffle_all"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraDarkCard, contentColor = AuraTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                Button(
                    onClick = { onSmartShuffleAll?.invoke() ?: onShuffleAll() },
                    modifier = Modifier.weight(1.35f).height(40.dp).testTag("playlist_smart_shuffle"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
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
        }

        // Secondary Info & Add Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRACK ORDER (${tracks.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextMuted,
                letterSpacing = 1.sp
            )

            TextButton(
                onClick = { showAddTracksDialog = true },
                modifier = Modifier.testTag("playlist_add_local_tracks_action")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = AuraCyanPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Local Tracks",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraCyanPrimary
                )
            }
        }

        // Track items in playlist with Move Up, Move Down, and Remove actions
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AuraTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No tracks in this playlist yet.",
                        color = AuraTextSecondary,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = { showAddTracksDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraCyanPrimary,
                            contentColor = AuraDarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_playlist_add_tracks_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Tracks from Local Storage", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("playlist_track_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                    val isCurrent = currentTrack?.id == track.id
                    val isFirst = index == 0
                    val isLast = index == tracks.lastIndex

                    Row(
                        modifier = Modifier
                            .testTag("playlist_item_${track.id}")
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) AuraDarkSurfaceVariant else AuraDarkCard)
                            .clickable { onPlayTrack(track) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Position order index badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) AuraCyanPrimary.copy(alpha = 0.2f) else Color(0xFF1B2333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) AuraCyanPrimary else AuraTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title & Artist
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = track.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) AuraCyanPrimary else AuraTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                HiResBadge(track = track, compact = true)
                            }
                            Text(
                                text = "${track.artist} • ${track.album}",
                                fontSize = 12.sp,
                                color = AuraTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Reordering Controls: Move Up & Move Down
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = { onMoveTrack(index, index - 1) },
                                enabled = !isFirst,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("playlist_move_up_${track.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move track up",
                                    tint = if (!isFirst) AuraCyanPrimary else AuraTextMuted.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { onMoveTrack(index, index + 1) },
                                enabled = !isLast,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("playlist_move_down_${track.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move track down",
                                    tint = if (!isLast) AuraCyanPrimary else AuraTextMuted.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Remove track from playlist button
                            IconButton(
                                onClick = { onRemoveTrack(track.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("playlist_remove_${track.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove from playlist",
                                    tint = AuraTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

/**
 * Dialog to select and add tracks stored on the local device to the playlist.
 */
@Composable
private fun AddTracksToPlaylistDialog(
    playlistName: String,
    allLocalTracks: List<Track>,
    existingTrackIds: Set<String>,
    onDismiss: () -> Unit,
    onAddSelectedTracks: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    val filteredTracks = remember(allLocalTracks, searchQuery) {
        if (searchQuery.isBlank()) {
            allLocalTracks
        } else {
            allLocalTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AuraDarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = "Add Tracks to Playlist",
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                    fontSize = 18.sp
                )
                Text(
                    text = "Target: $playlistName",
                    fontSize = 12.sp,
                    color = AuraCyanPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search local device tracks...", color = AuraTextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AuraTextMuted)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_tracks_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AuraDarkCard,
                        unfocusedContainerColor = AuraDarkCard,
                        focusedBorderColor = AuraCyanPrimary,
                        unfocusedBorderColor = AuraDarkBorder,
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary
                    )
                )

                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching local tracks found.",
                            color = AuraTextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("local_tracks_picker_list"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredTracks, key = { it.id }) { track ->
                            val alreadyInPlaylist = track.id in existingTrackIds
                            val isSelected = track.id in selectedIds

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            alreadyInPlaylist -> AuraDarkSurfaceVariant.copy(alpha = 0.5f)
                                            isSelected -> AuraCyanPrimary.copy(alpha = 0.15f)
                                            else -> AuraDarkCard
                                        }
                                    )
                                    .clickable(enabled = !alreadyInPlaylist) {
                                        selectedIds = if (isSelected) {
                                            selectedIds - track.id
                                        } else {
                                            selectedIds + track.id
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (alreadyInPlaylist) AuraTextMuted else AuraTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${track.artist} • ${track.album}",
                                        fontSize = 11.sp,
                                        color = AuraTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (alreadyInPlaylist) {
                                    Text(
                                        text = "In Playlist",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTextMuted,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) AuraCyanPrimary else Color.Transparent
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) AuraCyanPrimary else AuraDarkBorder,
                                                RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = AuraDarkBackground,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedIds.isNotEmpty()) {
                        onAddSelectedTracks(selectedIds.toList())
                    }
                },
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier.testTag("confirm_add_tracks_to_playlist_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuraCyanPrimary,
                    contentColor = AuraDarkBackground,
                    disabledContainerColor = AuraDarkSurfaceVariant,
                    disabledContentColor = AuraTextMuted
                )
            ) {
                Text(
                    text = if (selectedIds.isEmpty()) "Select Tracks" else "Add (${selectedIds.size})",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AuraTextMuted)
            }
        }
    )
}
