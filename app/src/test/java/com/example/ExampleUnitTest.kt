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
}

