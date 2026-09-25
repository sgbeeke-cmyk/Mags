package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EqualizerPreset
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.data.repository.MusicRepository
import com.example.player.AudioFxManager
import com.example.player.PlaybackManager
import com.example.player.RepeatMode
import com.example.visualizer.VisualizerFrame
import com.example.visualizer.VisualizerStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = MusicRepository(application, database)

    val audioFxManager = AudioFxManager()
    val playbackManager = PlaybackManager(application, audioFxManager, viewModelScope)

    val allTracks: StateFlow<List<Track>> = repository.allTracks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val hiResTracks: StateFlow<List<Track>> = repository.hiResTracks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val recentlyPlayed: StateFlow<List<Track>> = repository.recentlyPlayed.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val equalizerPresets: StateFlow<List<EqualizerPreset>> = repository.equalizerPresets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        EqualizerPreset.DEFAULT_PRESETS
    )

    // Playback state delegations
    val currentTrack = playbackManager.currentTrack
    val isPlaying = playbackManager.isPlaying
    val playbackPositionMs = playbackManager.playbackPositionMs
    val durationMs = playbackManager.durationMs
    val currentLyrics = playbackManager.currentLyrics
    val queue = playbackManager.queue
    val queueIndex = playbackManager.queueIndex
    val repeatMode = playbackManager.repeatMode
    val shuffleMode = playbackManager.shuffleMode
    val isShuffleEnabled = playbackManager.isShuffleEnabled
    val smartShuffleReasons = playbackManager.smartShuffleReasons
    val isGaplessEnabled = playbackManager.isGaplessEnabled
    val sleepTimerState = playbackManager.sleepTimerState

    // Real-time Visualizer state delegations
    val visualizerFrame: StateFlow<VisualizerFrame> = playbackManager.visualizerEngine.visualizerFrame
    val visualizerHasPermission: StateFlow<Boolean> = playbackManager.visualizerEngine.hasPermission
    val audioSessionId: StateFlow<Int> = playbackManager.audioSessionId

    private val _visualizerStyle = MutableStateFlow(VisualizerStyle.BARS)
    val visualizerStyle: StateFlow<VisualizerStyle> = _visualizerStyle.asStateFlow()

    fun setVisualizerStyle(style: VisualizerStyle) {
        _visualizerStyle.value = style
    }

    fun refreshAudioPermissionState() {
        playbackManager.visualizerEngine.refreshPermissionState()
    }

    // Audio FX state delegations
    val equalizerBands = audioFxManager.bands
    val bassBoostLevel = audioFxManager.bassBoostLevel
    val virtualizerLevel = audioFxManager.virtualizerLevel
    val isFxEnabled = audioFxManager.isEnabled
    val currentPresetName = audioFxManager.currentPresetName

    // UI state for search & filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected playlist for detail screen
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedPlaylistTracks = MutableStateFlow<List<Track>>(emptyList())
    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylistTracks.asStateFlow()

    // Track for Audio Spec / Details Sheet
    private val _inspectingTrack = MutableStateFlow<Track?>(null)
    val inspectingTrack: StateFlow<Track?> = _inspectingTrack.asStateFlow()

    init {
        playbackManager.onTrackPlayed = { track ->
            viewModelScope.launch {
                repository.recordPlayed(track.id)
            }
        }

        viewModelScope.launch {
            repository.initializeBundledDemoTracks()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: Track, queueList: List<Track>? = null, startIndex: Int = -1) {
        val targetQueue = queueList ?: allTracks.value
        val index = if (startIndex >= 0) startIndex else targetQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackManager.playTrackList(targetQueue, index)
    }

    fun togglePlayPause() = playbackManager.togglePlayPause()

    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)

    fun skipToNext() = playbackManager.skipToNext()

    fun skipToPrevious() = playbackManager.skipToPrevious()

    fun toggleRepeatMode() = playbackManager.toggleRepeatMode()

    fun toggleShuffle() = playbackManager.toggleShuffle()

    fun cycleShuffleMode() = playbackManager.cycleShuffleMode()

    fun setShuffleMode(mode: com.example.player.ShuffleMode) = playbackManager.setShuffleMode(mode)

    fun playSmartShuffle(tracks: List<Track>, startingTrack: Track? = null) =
        playbackManager.playSmartShuffle(tracks, startingTrack)

    fun setGaplessEnabled(enabled: Boolean) = playbackManager.setGaplessEnabled(enabled)

    fun startSleepTimer(minutes: Int, finishTrack: Boolean = false) = playbackManager.startSleepTimer(minutes, finishTrack)

    fun startSleepTimerEndOfTrack() = playbackManager.startSleepTimerEndOfTrack()

    fun addSleepTimerMinutes(minutes: Int) = playbackManager.addSleepTimerMinutes(minutes)

    fun cancelSleepTimer() = playbackManager.cancelSleepTimer()

    fun setSleepTimerFinishTrack(finish: Boolean) = playbackManager.setSleepTimerFinishTrack(finish)

    fun addToQueueNext(track: Track) = playbackManager.addToQueueNext(track)

    fun addToQueueEnd(track: Track) = playbackManager.addToQueueEnd(track)

    fun removeFromQueue(index: Int) = playbackManager.removeFromQueue(index)

    fun clearQueue() = playbackManager.clearQueue()

    fun setBandGain(bandIndex: Int, gainDb: Float) = audioFxManager.setBandGain(bandIndex, gainDb)

    fun setBassBoost(level: Int) = audioFxManager.setBassBoost(level)

    fun setVirtualizer(level: Int) = audioFxManager.setVirtualizer(level)

    fun applyPreset(preset: EqualizerPreset) = audioFxManager.applyPreset(preset)

    fun toggleFxEnabled(enabled: Boolean) = audioFxManager.setEnabled(enabled)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, !track.isFavorite)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
                _selectedPlaylistTracks.value = emptyList()
            }
        }
    }

    fun selectPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        viewModelScope.launch {
            repository.getTracksForPlaylist(playlist.id).collect { tracks ->
                _selectedPlaylistTracks.value = tracks
            }
        }
    }

    fun clearSelectedPlaylist() {
        _selectedPlaylist.value = null
        _selectedPlaylistTracks.value = emptyList()
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun inspectTrack(track: Track?) {
        _inspectingTrack.value = track
    }

    fun importFiles(uris: List<Uri>) {
        viewModelScope.launch {
            for (uri in uris) {
                repository.importTrackFromUri(uri)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.release()
    }
}
