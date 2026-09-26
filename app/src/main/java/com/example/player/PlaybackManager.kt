package com.example.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.audio.metadata.LyricsParser
import com.example.data.model.LyricsData
import com.example.data.model.Track
import com.example.visualizer.RealtimeAudioVisualizerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

class PlaybackManager(
    private val context: Context,
    val audioFxManager: AudioFxManager,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "PlaybackManager"
    }

    private var exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true
        )
        .build()

    val mediaSession: MediaSession = MediaSession.Builder(context, exoPlayer)
        .setId("MusicyMediaSession")
        .build()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    val visualizerEngine = RealtimeAudioVisualizerEngine(context, scope)

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _smartShuffleReasons = MutableStateFlow<Map<String, String>>(emptyMap())
    val smartShuffleReasons: StateFlow<Map<String, String>> = _smartShuffleReasons.asStateFlow()

    private var originalTracksOrder: List<Track> = emptyList()

    private val _isGaplessEnabled = MutableStateFlow(true)
    val isGaplessEnabled: StateFlow<Boolean> = _isGaplessEnabled.asStateFlow()

    private val _isAutoPlayNext = MutableStateFlow(true)
    val isAutoPlayNext: StateFlow<Boolean> = _isAutoPlayNext.asStateFlow()

    private val _currentLyrics = MutableStateFlow(LyricsData(false, emptyList()))
    val currentLyrics: StateFlow<LyricsData> = _currentLyrics.asStateFlow()

    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    private var positionTickerJob: Job? = null
    private var sleepTimerJob: Job? = null
    var onTrackPlayed: ((Track) -> Unit)? = null

    init {
        setupPlayerListener()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
    }

    fun setAutoPlayNext(enabled: Boolean) {
        _isAutoPlayNext.value = enabled
        exoPlayer.pauseAtEndOfMediaItems = !enabled
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                val sessionId = exoPlayer.audioSessionId
                _audioSessionId.value = sessionId
                visualizerEngine.onAudioSessionIdChanged(sessionId)
                visualizerEngine.onPlaybackStateChanged(isPlaying)
                if (isPlaying) {
                    startPositionTicker()
                    audioFxManager.attachAudioSession(sessionId)
                } else {
                    stopPositionTicker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                        _playbackPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                        val sessionId = exoPlayer.audioSessionId
                        _audioSessionId.value = sessionId
                        audioFxManager.attachAudioSession(sessionId)
                        visualizerEngine.onAudioSessionIdChanged(sessionId)
                    }
                    Player.STATE_ENDED -> {
                        if (_sleepTimerState.value.isActive && (_sleepTimerState.value.isWaitingForTrackEnd || _sleepTimerState.value.remainingSeconds <= 0)) {
                            exoPlayer.pause()
                            cancelSleepTimer()
                        }
                        if (!_isAutoPlayNext.value || (_repeatMode.value == RepeatMode.OFF && _queueIndex.value >= _queue.value.lastIndex)) {
                            _isPlaying.value = false
                            stopPositionTicker()
                        }
                    }
                    else -> Unit
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (_sleepTimerState.value.isActive && (_sleepTimerState.value.isWaitingForTrackEnd || (_sleepTimerState.value.finishCurrentTrackFirst && _sleepTimerState.value.remainingSeconds <= 0))) {
                    exoPlayer.pause()
                    cancelSleepTimer()
                    return
                }

                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && !_isAutoPlayNext.value) {
                    exoPlayer.pause()
                    _isPlaying.value = false
                    stopPositionTicker()
                    return
                }

                val currentMediaIndex = exoPlayer.currentMediaItemIndex
                if (currentMediaIndex in _queue.value.indices) {
                    _queueIndex.value = currentMediaIndex
                    val track = _queue.value[currentMediaIndex]
                    _currentTrack.value = track
                    _currentLyrics.value = LyricsParser.parse(track.lyricsRaw)
                    _durationMs.value = track.durationMs
                    _playbackPositionMs.value = 0L
                    onTrackPlayed?.invoke(track)
                }
            }
        })
    }

    private fun createMediaItem(track: Track): MediaItem {
        return MediaItem.Builder()
            .setUri(Uri.parse(track.uri))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .build()
            )
            .build()
    }

    fun playTrackList(tracks: List<Track>, startIndex: Int = 0, preserveOrderAsIs: Boolean = false) {
        if (tracks.isEmpty()) return
        if (!preserveOrderAsIs) {
            originalTracksOrder = tracks
        }
        val clampedIndex = startIndex.coerceIn(0, tracks.lastIndex)

        if (!preserveOrderAsIs && _shuffleMode.value == ShuffleMode.SMART) {
            val targetTrack = tracks[clampedIndex]
            val smartResults = SmartShuffleEngine.smartShuffleWithReasons(tracks, targetTrack)
            _smartShuffleReasons.value = smartResults.associate { it.track.id to it.reasonTag }
            val orderedTracks = smartResults.map { it.track }
            startPlaybackInternal(orderedTracks, 0)
            return
        }

        if (!preserveOrderAsIs && _shuffleMode.value == ShuffleMode.STANDARD) {
            val targetTrack = tracks[clampedIndex]
            val remaining = tracks.filterIndexed { index, _ -> index != clampedIndex }.shuffled()
            val orderedTracks = listOf(targetTrack) + remaining
            _smartShuffleReasons.value = emptyMap()
            startPlaybackInternal(orderedTracks, 0)
            return
        }

        _smartShuffleReasons.value = emptyMap()
        startPlaybackInternal(tracks, clampedIndex)
    }

    fun playSmartShuffle(tracks: List<Track>, startingTrack: Track? = null) {
        if (tracks.isEmpty()) return
        originalTracksOrder = tracks
        _shuffleMode.value = ShuffleMode.SMART
        _isShuffleEnabled.value = true

        val smartResults = SmartShuffleEngine.smartShuffleWithReasons(tracks, startingTrack)
        _smartShuffleReasons.value = smartResults.associate { it.track.id to it.reasonTag }
        val orderedTracks = smartResults.map { it.track }
        startPlaybackInternal(orderedTracks, 0)
    }

    private fun startPlaybackInternal(tracks: List<Track>, startIndex: Int) {
        val clampedIndex = startIndex.coerceIn(0, tracks.lastIndex)
        _queue.value = tracks
        _queueIndex.value = clampedIndex

        val targetTrack = tracks[clampedIndex]
        _currentTrack.value = targetTrack
        _currentLyrics.value = LyricsParser.parse(targetTrack.lyricsRaw)

        val mediaItems = tracks.map { createMediaItem(it) }

        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItems(mediaItems, clampedIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        _isPlaying.value = true
        startPositionTicker()
        onTrackPlayed?.invoke(targetTrack)
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.mediaItemCount == 0) {
                if (_queue.value.isNotEmpty()) {
                    playTrackList(_queue.value, _queueIndex.value.coerceAtLeast(0))
                }
            } else {
                exoPlayer.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs
        exoPlayer.seekTo(positionMs)
    }

    fun seekForward(deltaMs: Long = 10_000L) {
        val total = _durationMs.value.coerceAtLeast(0L)
        val target = if (total > 0) {
            (_playbackPositionMs.value + deltaMs).coerceAtMost(total)
        } else {
            _playbackPositionMs.value + deltaMs
        }
        seekTo(target)
    }

    fun seekBack(deltaMs: Long = 10_000L) {
        val target = (_playbackPositionMs.value - deltaMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_queue.value.isNotEmpty() && _repeatMode.value == RepeatMode.ALL) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    fun skipToPrevious() {
        if (exoPlayer.currentPosition > 3000L) {
            exoPlayer.seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else if (_queue.value.isNotEmpty()) {
            exoPlayer.seekTo(0L)
        }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun toggleShuffle() {
        cycleShuffleMode()
    }

    fun cycleShuffleMode() {
        val nextMode = _shuffleMode.value.next()
        setShuffleMode(nextMode)
    }

    fun setShuffleMode(mode: ShuffleMode) {
        if (_shuffleMode.value == mode && _isShuffleEnabled.value == mode.isEnabled) return
        _shuffleMode.value = mode
        _isShuffleEnabled.value = mode.isEnabled

        val curTrack = _currentTrack.value
        val curIndex = _queueIndex.value

        when (mode) {
            ShuffleMode.OFF -> {
                exoPlayer.shuffleModeEnabled = false
                _smartShuffleReasons.value = emptyMap()
                // Restore original sequential order for upcoming tracks
                if (originalTracksOrder.isNotEmpty() && curTrack != null && curIndex >= 0) {
                    val origIdx = originalTracksOrder.indexOfFirst { it.id == curTrack.id }
                    val remainingOriginal = if (origIdx >= 0) {
                        originalTracksOrder.drop(origIdx + 1)
                    } else {
                        originalTracksOrder.filter { it.id != curTrack.id }
                    }
                    reorderUpcomingQueue(remainingOriginal)
                }
            }
            ShuffleMode.STANDARD -> {
                _smartShuffleReasons.value = emptyMap()
                val pool = if (originalTracksOrder.isNotEmpty()) originalTracksOrder else _queue.value
                if (curTrack != null && curIndex >= 0) {
                    val remaining = pool.filter { it.id != curTrack.id }.shuffled()
                    reorderUpcomingQueue(remaining)
                }
                exoPlayer.shuffleModeEnabled = false
            }
            ShuffleMode.SMART -> {
                val pool = if (originalTracksOrder.isNotEmpty()) originalTracksOrder else _queue.value
                if (curTrack != null && curIndex >= 0) {
                    val smartResults = SmartShuffleEngine.smartShuffleWithReasons(
                        tracks = pool,
                        currentTrack = curTrack
                    )
                    _smartShuffleReasons.value = smartResults.associate { it.track.id to it.reasonTag }
                    val upcomingTracks = smartResults.drop(1).map { it.track }
                    reorderUpcomingQueue(upcomingTracks)
                }
                exoPlayer.shuffleModeEnabled = false
            }
        }
    }

    private fun reorderUpcomingQueue(upcomingTracks: List<Track>) {
        val curIndex = _queueIndex.value
        if (curIndex < 0) return

        val preservedHistory = _queue.value.take(curIndex + 1)
        val newQueue = preservedHistory + upcomingTracks
        _queue.value = newQueue

        val nextItemIndex = curIndex + 1
        val itemsToRemove = exoPlayer.mediaItemCount - nextItemIndex
        if (itemsToRemove > 0) {
            exoPlayer.removeMediaItems(nextItemIndex, exoPlayer.mediaItemCount)
        }
        val newMediaItems = upcomingTracks.map { createMediaItem(it) }
        if (newMediaItems.isNotEmpty()) {
            exoPlayer.addMediaItems(nextItemIndex, newMediaItems)
        }
    }

    fun setGaplessEnabled(enabled: Boolean) {
        _isGaplessEnabled.value = enabled
        // In ExoPlayer, seamless gapless playback is enabled by having all items loaded into the player queue
        // without pausing between items.
        exoPlayer.pauseAtEndOfMediaItems = !enabled
    }

    fun addToQueueNext(track: Track) {
        val currentQueue = _queue.value.toMutableList()
        val insertIndex = (_queueIndex.value + 1).coerceIn(0, currentQueue.size)
        currentQueue.add(insertIndex, track)
        _queue.value = currentQueue

        val mediaItem = MediaItem.Builder().setUri(Uri.parse(track.uri)).build()
        exoPlayer.addMediaItem(insertIndex, mediaItem)
    }

    fun addToQueueEnd(track: Track) {
        val currentQueue = _queue.value.toMutableList()
        currentQueue.add(track)
        _queue.value = currentQueue

        val mediaItem = MediaItem.Builder().setUri(Uri.parse(track.uri)).build()
        exoPlayer.addMediaItem(mediaItem)
    }

    fun removeFromQueue(index: Int) {
        if (index !in _queue.value.indices) return
        val currentQueue = _queue.value.toMutableList()
        val currentIndex = _queueIndex.value

        currentQueue.removeAt(index)
        _queue.value = currentQueue
        exoPlayer.removeMediaItem(index)

        if (index == currentIndex) {
            if (currentQueue.isEmpty()) {
                _currentTrack.value = null
                _queueIndex.value = -1
                exoPlayer.stop()
            } else {
                val newIndex = index.coerceAtMost(currentQueue.lastIndex)
                _queueIndex.value = newIndex
                _currentTrack.value = currentQueue[newIndex]
            }
        } else if (index < currentIndex) {
            _queueIndex.value = currentIndex - 1
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _queueIndex.value = -1
        _currentTrack.value = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        stopPositionTicker()
    }

    private fun startPositionTicker() {
        stopPositionTicker()
        positionTickerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _playbackPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = exoPlayer.duration
                    if (dur > 0) _durationMs.value = dur
                }
                delay(100L) // 100ms for ultra smooth lyric highlighting and scrubber
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    fun startSleepTimer(minutes: Int, finishTrack: Boolean = false) {
        val totalSeconds = (minutes * 60L).coerceAtLeast(1L)
        startSleepTimerSeconds(totalSeconds, finishTrack)
    }

    fun startSleepTimerSeconds(totalSeconds: Long, finishTrack: Boolean = false) {
        sleepTimerJob?.cancel()
        _sleepTimerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = totalSeconds,
            totalSeconds = totalSeconds,
            finishCurrentTrackFirst = finishTrack,
            isWaitingForTrackEnd = false
        )

        sleepTimerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000L)
                val current = _sleepTimerState.value
                if (!current.isActive) break
                val newRemaining = current.remainingSeconds - 1
                if (newRemaining <= 0) {
                    if (current.finishCurrentTrackFirst && exoPlayer.isPlaying) {
                        _sleepTimerState.value = current.copy(
                            remainingSeconds = 0,
                            isWaitingForTrackEnd = true
                        )
                        break
                    } else {
                        _sleepTimerState.value = SleepTimerState()
                        exoPlayer.pause()
                        break
                    }
                } else {
                    _sleepTimerState.value = current.copy(remainingSeconds = newRemaining)
                }
            }
        }
    }

    fun startSleepTimerEndOfTrack() {
        val currentDur = if (exoPlayer.duration > 0) exoPlayer.duration else _durationMs.value
        val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
        val remainingMs = (currentDur - currentPos).coerceAtLeast(0L)
        val remainingSeconds = (remainingMs / 1000L).coerceAtLeast(1L)
        startSleepTimerSeconds(remainingSeconds, finishTrack = true)
    }

    fun addSleepTimerMinutes(extraMinutes: Int) {
        val current = _sleepTimerState.value
        if (current.isActive) {
            val addedSec = extraMinutes * 60L
            _sleepTimerState.value = current.copy(
                remainingSeconds = current.remainingSeconds + addedSec,
                totalSeconds = current.totalSeconds + addedSec,
                isWaitingForTrackEnd = false
            )
        } else {
            startSleepTimer(extraMinutes, false)
        }
    }

    fun setSleepTimerFinishTrack(finish: Boolean) {
        val current = _sleepTimerState.value
        if (current.isActive) {
            _sleepTimerState.value = current.copy(finishCurrentTrackFirst = finish)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerState.value = SleepTimerState()
    }

    fun release() {
        cancelSleepTimer()
        stopPositionTicker()
        visualizerEngine.release()
        audioFxManager.releaseFx()
        mediaSession.release()
        exoPlayer.release()
    }
}
