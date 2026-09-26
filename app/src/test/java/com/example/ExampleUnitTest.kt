package com.example

import com.example.audio.metadata.LyricsParser
import com.example.data.model.EqualizerPreset
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testLyricsParser_syncedLyrics() {
    val lrc = """
      [00:00.00] (Synth Pulse Intro)
      [00:06.40] Electric dreams beneath neon skies
      [00:12.80] Twenty-four bit master tape
    """.trimIndent()

    val parsed = LyricsParser.parse(lrc)
    assertTrue(parsed.isSynced)
    assertEquals(3, parsed.lines.size)
    assertEquals(0L, parsed.lines[0].timestampMs)
    assertEquals(6400L, parsed.lines[1].timestampMs)
    assertEquals("Electric dreams beneath neon skies", parsed.lines[1].text)

    // Test active lyric search
    val activeIdx1 = LyricsParser.findActiveLyricIndex(parsed, 7000L)
    assertEquals(1, activeIdx1)

    val activeIdx2 = LyricsParser.findActiveLyricIndex(parsed, 15000L)
    assertEquals(2, activeIdx2)
  }

  @Test
  fun testDefaultPresets_exist() {
    assertTrue(EqualizerPreset.DEFAULT_PRESETS.isNotEmpty())
    val master = EqualizerPreset.DEFAULT_PRESETS.first()
    assertEquals("Hi-Res Master", master.name)
    assertEquals(5, master.bandLevels.size)
  }

  @Test
  fun testSleepTimerState_formattingAndProgress() {
    val state = com.example.player.SleepTimerState(
      isActive = true,
      remainingSeconds = 125L, // 2m 5s
      totalSeconds = 300L,     // 5m
      finishCurrentTrackFirst = false,
      isWaitingForTrackEnd = false
    )

    assertEquals("02:05", state.formattedRemaining)
    assertEquals(125f / 300f, state.progress, 0.01f)

    // Over an hour
    val hourState = com.example.player.SleepTimerState(
      isActive = true,
      remainingSeconds = 3665L, // 1h 1m 5s
      totalSeconds = 7200L
    )
    assertEquals("1:01:05", hourState.formattedRemaining)

    // Waiting for track end
    val waitingState = com.example.player.SleepTimerState(
      isActive = true,
      remainingSeconds = 0L,
      totalSeconds = 300L,
      isWaitingForTrackEnd = true
    )
    assertEquals("Finishing song", waitingState.formattedRemaining)
  }

  @Test
  fun testSmartShuffle_preservesAllTracksAndNoDuplicates() {
    val dummyTracks = (1..10).map { i ->
      com.example.data.model.Track(
        id = "track_$i",
        title = "Title $i",
        artist = "Artist ${(i % 3) + 1}",
        album = "Album ${(i % 2) + 1}",
        durationMs = 180000L,
        uri = "content://dummy/$i",
        playCount = if (i % 2 == 0) 5 else 0,
        lastPlayedMs = if (i % 2 == 0) System.currentTimeMillis() - 10000 else 0L
      )
    }

    val shuffledWithReasons = com.example.player.SmartShuffleEngine.smartShuffleWithReasons(
      tracks = dummyTracks,
      currentTrack = dummyTracks[0]
    )

    assertEquals(10, shuffledWithReasons.size)
    assertEquals("track_1", shuffledWithReasons[0].track.id)
    val trackIds = shuffledWithReasons.map { it.track.id }.toSet()
    assertEquals(10, trackIds.size)
  }

  @Test
  fun testSmartShuffle_prioritizesUnplayedTracks() {
    val frequentlyPlayed = (1..5).map { i ->
      com.example.data.model.Track(
        id = "frequent_$i",
        title = "Frequent $i",
        artist = "Artist $i",
        album = "Album $i",
        durationMs = 180000L,
        uri = "content://dummy/$i",
        playCount = 50,
        lastPlayedMs = System.currentTimeMillis() - 1000 // Just played
      )
    }

    val unheardTracks = (1..5).map { i ->
      com.example.data.model.Track(
        id = "unheard_$i",
        title = "Unheard $i",
        artist = "Artist ${i + 5}",
        album = "Album ${i + 5}",
        durationMs = 180000L,
        uri = "content://dummy/${i + 5}",
        playCount = 0,
        lastPlayedMs = 0L
      )
    }

    val all = frequentlyPlayed + unheardTracks
    val shuffled = com.example.player.SmartShuffleEngine.smartShuffleWithReasons(
      tracks = all,
      currentTrack = null
    )

    // In the first 4 tracks of the shuffled list, unheard tracks should have prominent representation
    val firstFourUnheardCount = shuffled.take(4).count { it.track.playCount == 0 }
    assertTrue("Smart shuffle should surface unheard tracks early in the queue", firstFourUnheardCount >= 2)
  }

  @Test
  fun testMediaControllerSeekCalculations() {
    val durationMs = 240_000L // 4 mins
    val currentPos = 50_000L // 50s

    // Seek forward 10s
    val seekForwardTarget = (currentPos + 10_000L).coerceAtMost(durationMs)
    assertEquals(60_000L, seekForwardTarget)

    // Seek back 10s
    val seekBackTarget = (currentPos - 10_000L).coerceAtLeast(0L)
    assertEquals(40_000L, seekBackTarget)

    // Clamping to boundaries
    val overflowForward = (235_000L + 10_000L).coerceAtMost(durationMs)
    assertEquals(240_000L, overflowForward)

    val underflowBack = (5_000L - 10_000L).coerceAtLeast(0L)
    assertEquals(0L, underflowBack)
  }

  @Test
  fun testFiveBandEqualizerFrequencies() {
    val fxManager = com.example.player.AudioFxManager()
    val bands = fxManager.bands.value
    assertEquals(5, bands.size)
    assertEquals(60, bands[0].centerFreqHz)
    assertEquals(250, bands[1].centerFreqHz)
    assertEquals(1000, bands[2].centerFreqHz)
    assertEquals(4000, bands[3].centerFreqHz)
    assertEquals(16000, bands[4].centerFreqHz)

    // Test setting band gains
    fxManager.setBandGain(0, 4.5f)
    assertEquals(4.5f, fxManager.bands.value[0].gainDb, 0.01f)

    fxManager.setBandGain(4, -3.0f)
    assertEquals(-3.0f, fxManager.bands.value[4].gainDb, 0.01f)

    // Test resetting to flat
    fxManager.resetToFlat()
    assertTrue(fxManager.bands.value.all { it.gainDb == 0f })
  }

  @Test
  fun testPlaylistEntitySchemaAndModelConversion() {
    val entity = com.example.data.local.PlaylistEntity(
      id = 42L,
      name = "Studio Masters",
      description = "Lossless FLAC recordings",
      createdAt = 1700000000000L,
      updatedAt = 1700000050000L,
      accentColorHex = "#4DEEEA"
    )

    val model = entity.toPlaylist(trackCount = 5)
    assertEquals(42L, model.id)
    assertEquals("Studio Masters", model.name)
    assertEquals("Lossless FLAC recordings", model.description)
    assertEquals(5, model.trackCount)
    assertEquals("#4DEEEA", model.accentColorHex)
  }

  @Test
  fun testPlaylistTrackReorderLogic() {
    val initialTracks = listOf("track_a", "track_b", "track_c", "track_d")
    val crossRefs = initialTracks.mapIndexed { index, id ->
      com.example.data.local.PlaylistTrackCrossRef(
        playlistId = 1L,
        trackId = id,
        orderIndex = index
      )
    }

    // Verify initial ordering
    assertEquals(0, crossRefs[0].orderIndex)
    assertEquals(1, crossRefs[1].orderIndex)
    assertEquals(2, crossRefs[2].orderIndex)
    assertEquals(3, crossRefs[3].orderIndex)

    // Reorder: Move track_c to the top: ["track_c", "track_a", "track_b", "track_d"]
    val newOrder = listOf("track_c", "track_a", "track_b", "track_d")
    val reordered = newOrder.mapIndexed { index, id ->
      com.example.data.local.PlaylistTrackCrossRef(
        playlistId = 1L,
        trackId = id,
        orderIndex = index
      )
    }

    assertEquals("track_c", reordered[0].trackId)
    assertEquals(0, reordered[0].orderIndex)
    assertEquals("track_a", reordered[1].trackId)
    assertEquals(1, reordered[1].orderIndex)
    assertEquals("track_b", reordered[2].trackId)
    assertEquals(2, reordered[2].orderIndex)
    assertEquals("track_d", reordered[3].trackId)
    assertEquals(3, reordered[3].orderIndex)
  }

  @Test
  fun testPlaylistTrackMoveUpAndDownSimulation() {
    val tracks = mutableListOf("track_1", "track_2", "track_3")

    // Move track at index 2 (track_3) up to index 1
    val fromIndex = 2
    val toIndex = 1
    val moved = tracks.removeAt(fromIndex)
    tracks.add(toIndex, moved)

    assertEquals(listOf("track_1", "track_3", "track_2"), tracks)

    // Move track at index 0 (track_1) down to index 1
    val fromIndex2 = 0
    val toIndex2 = 1
    val moved2 = tracks.removeAt(fromIndex2)
    tracks.add(toIndex2, moved2)

    assertEquals(listOf("track_3", "track_1", "track_2"), tracks)
  }

  @Test
  fun testPlaylistRemovalCompactionLogic() {
    val items = mutableListOf("A", "B", "C", "D")
    // Remove "B"
    items.remove("B")

    // Re-index consecutive 0..N-1
    val compacted = items.mapIndexed { idx, id -> idx to id }
    assertEquals(3, compacted.size)
    assertEquals(0 to "A", compacted[0])
    assertEquals(1 to "C", compacted[1])
    assertEquals(2 to "D", compacted[2])
  }

  @Test
  fun testSleepTimerStateCalculations() {
    val state = com.example.player.SleepTimerState(
      isActive = true,
      remainingSeconds = 1800L,
      totalSeconds = 1800L,
      finishCurrentTrackFirst = false,
      isWaitingForTrackEnd = false
    )

    assertEquals(1.0f, state.progress, 0.001f)
    assertEquals("30:00", state.formattedRemaining)

    // Mid-countdown
    val midState = state.copy(remainingSeconds = 900L)
    assertEquals(0.5f, midState.progress, 0.001f)
    assertEquals("15:00", midState.formattedRemaining)

    // Over an hour
    val hourState = state.copy(remainingSeconds = 3665L, totalSeconds = 3665L)
    assertEquals("1:01:05", hourState.formattedRemaining)

    // Waiting for track end
    val waitingState = state.copy(isWaitingForTrackEnd = true)
    assertEquals("Finishing song", waitingState.formattedRemaining)
  }

  @Test
  fun testAutoPlayNextLogic() {
    var isAutoPlayNext = true
    val queue = listOf("Song 1", "Song 2", "Song 3")
    var currentIndex = 0

    // When auto play is true, track completion advances to next index
    fun onSongComplete(reasonAuto: Boolean): Boolean {
      if (reasonAuto && !isAutoPlayNext) {
        return false // Pauses
      }
      if (currentIndex < queue.lastIndex) {
        currentIndex++
        return true // Continues
      }
      return false
    }

    assertTrue(onSongComplete(reasonAuto = true))
    assertEquals(1, currentIndex)
    assertEquals("Song 2", queue[currentIndex])

    // Turn auto play next OFF
    isAutoPlayNext = false
    val willContinue = onSongComplete(reasonAuto = true)
    assertFalse(willContinue)
    assertEquals(1, currentIndex) // Stays at current index and pauses
  }
}

