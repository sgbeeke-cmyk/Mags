package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.audio.metadata.FlacMetadataExtractor
import com.example.data.local.AppDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import com.example.data.local.PlaylistWithCount
import com.example.data.local.TrackEntity
import com.example.data.model.EqualizerPreset
import com.example.data.model.Playlist
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    companion object {
        private const val TAG = "MusicRepository"
    }

    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()
    private val equalizerDao = database.equalizerDao()

    val allTracks: Flow<List<Track>> = trackDao.getAllTracks().map { list -> list.map { it.toTrack() } }
    val hiResTracks: Flow<List<Track>> = trackDao.getHiResTracks().map { list -> list.map { it.toTrack() } }
    val favoriteTracks: Flow<List<Track>> = trackDao.getFavoriteTracks().map { list -> list.map { it.toTrack() } }
    val recentlyPlayed: Flow<List<Track>> = trackDao.getRecentlyPlayedTracks().map { list -> list.map { it.toTrack() } }

    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylistsWithCount().map { list ->
        list.map {
            Playlist(
                id = it.id,
                name = it.name,
                description = it.description,
                trackCount = it.trackCount,
                createdAt = it.createdAt,
                accentColorHex = it.accentColorHex
            )
        }
    }

    val equalizerPresets: Flow<List<EqualizerPreset>> = equalizerDao.getAllPresets().map { list ->
        list.map { it.toPreset() }
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistDao.getTracksForPlaylist(playlistId).map { list -> list.map { it.toTrack() } }
    }

    suspend fun createPlaylist(name: String, description: String = "", accentColorHex: String = "#4DEEEA"): Long {
        return withContext(Dispatchers.IO) {
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    name = name,
                    description = description,
                    accentColorHex = accentColorHex
                )
            )
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.clearPlaylist(playlistId)
            playlistDao.deletePlaylist(playlistId)
        }
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        withContext(Dispatchers.IO) {
            playlistDao.renamePlaylist(playlistId, newName)
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        withContext(Dispatchers.IO) {
            playlistDao.addTrackToPlaylist(
                PlaylistTrackCrossRef(
                    playlistId = playlistId,
                    trackId = trackId
                )
            )
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        withContext(Dispatchers.IO) {
            playlistDao.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    suspend fun toggleFavorite(trackId: String, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            trackDao.setFavorite(trackId, isFavorite)
        }
    }

    suspend fun recordPlayed(trackId: String) {
        withContext(Dispatchers.IO) {
            trackDao.recordPlayed(trackId, System.currentTimeMillis())
        }
    }

    suspend fun importTrackFromUri(uri: Uri): Track? {
        return withContext(Dispatchers.IO) {
            try {
                val metadata = FlacMetadataExtractor.extract(context, uri)
                val id = uri.toString().hashCode().toString()
                val track = Track(
                    id = id,
                    title = metadata.title,
                    artist = metadata.artist,
                    album = metadata.album,
                    durationMs = metadata.durationMs,
                    uri = uri.toString(),
                    format = metadata.format,
                    sampleRate = metadata.sampleRate,
                    bitDepth = metadata.bitDepth,
                    bitrateKbps = metadata.bitrateKbps,
                    channels = metadata.channels,
                    isHiRes = metadata.isHiRes,
                    albumArtUri = metadata.albumArtUri,
                    lyricsRaw = metadata.lyricsRaw,
                    year = metadata.year,
                    genre = metadata.genre,
                    trackNumber = metadata.trackNumber,
                    fileSize = metadata.fileSize
                )
                trackDao.insertTrack(TrackEntity.fromTrack(track))
                track
            } catch (e: Exception) {
                Log.e(TAG, "Error importing track from $uri", e)
                null
            }
        }
    }

    suspend fun initializeBundledDemoTracks() {
        withContext(Dispatchers.IO) {
            val count = trackDao.getTrackCount()
            if (count > 0) return@withContext

            val bundledFiles = listOf(
                BundledSpec(
                    filename = "neon_odyssey.flac",
                    artRes = R.drawable.cover_neon_odyssey_1790200947877,
                    defaultTitle = "Neon Odyssey",
                    defaultArtist = "Aura Resonance",
                    defaultAlbum = "Prismatic Horizons",
                    sampleRate = 96000,
                    bitDepth = 24,
                    genre = "Synthwave Hi-Res"
                ),
                BundledSpec(
                    filename = "aurora_borealis.flac",
                    artRes = R.drawable.cover_aurora_acoustic_1790200960555,
                    defaultTitle = "Aurora Borealis",
                    defaultArtist = "Nordic Ensemble",
                    defaultAlbum = "Echoes of the Fjord",
                    sampleRate = 48000,
                    bitDepth = 24,
                    genre = "Acoustic Ambient"
                ),
                BundledSpec(
                    filename = "velvet_nocturne.flac",
                    artRes = R.drawable.cover_midnight_jazz_1790200971617,
                    defaultTitle = "Velvet Nocturne",
                    defaultArtist = "Miles Tribute Quartet",
                    defaultAlbum = "Blue Room Sessions",
                    sampleRate = 44100,
                    bitDepth = 16,
                    genre = "Midnight Jazz"
                )
            )

            val musicDir = File(context.filesDir, "bundled_music")
            if (!musicDir.exists()) musicDir.mkdirs()

            val entities = mutableListOf<TrackEntity>()

            for (spec in bundledFiles) {
                try {
                    val outFile = File(musicDir, spec.filename)
                    if (!outFile.exists() || outFile.length() == 0L) {
                        context.assets.open("music/${spec.filename}").use { input ->
                            FileOutputStream(outFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                    val uri = Uri.fromFile(outFile)
                    val metadata = FlacMetadataExtractor.extract(context, uri, spec.defaultTitle)

                    val track = Track(
                        id = "bundled_${spec.filename}",
                        title = metadata.title.ifEmpty { spec.defaultTitle },
                        artist = metadata.artist.ifEmpty { spec.defaultArtist },
                        album = metadata.album.ifEmpty { spec.defaultAlbum },
                        durationMs = if (metadata.durationMs > 0) metadata.durationMs else 45000L,
                        uri = uri.toString(),
                        format = "FLAC",
                        sampleRate = if (metadata.sampleRate > 0) metadata.sampleRate else spec.sampleRate,
                        bitDepth = if (metadata.bitDepth > 0) metadata.bitDepth else spec.bitDepth,
                        bitrateKbps = metadata.bitrateKbps.coerceAtLeast(1411),
                        channels = metadata.channels,
                        isHiRes = (metadata.sampleRate > 48000 || metadata.bitDepth > 16 || spec.sampleRate > 48000 || spec.bitDepth > 16),
                        albumArtRes = spec.artRes,
                        lyricsRaw = metadata.lyricsRaw,
                        year = metadata.year,
                        genre = metadata.genre ?: spec.genre,
                        trackNumber = metadata.trackNumber,
                        fileSize = outFile.length()
                    )
                    entities.add(TrackEntity.fromTrack(track))
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading bundled track ${spec.filename}", e)
                }
            }

            if (entities.isNotEmpty()) {
                trackDao.insertTracks(entities)

                // Add these tracks to the default playlists as well
                try {
                    // Hi-Res playlist gets track 1 & 2
                    playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = 1L, trackId = entities[0].id))
                    if (entities.size > 1) {
                        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = 1L, trackId = entities[1].id))
                        // Late night ambient gets track 2 & 3
                        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = 2L, trackId = entities[1].id))
                    }
                    if (entities.size > 2) {
                        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = 2L, trackId = entities[2].id))
                        // Acoustic gets track 2
                        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = 3L, trackId = entities[1].id))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed linking demo tracks to default playlists: ${e.message}")
                }
            }
        }
    }

    private data class BundledSpec(
        val filename: String,
        val artRes: Int,
        val defaultTitle: String,
        val defaultArtist: String,
        val defaultAlbum: String,
        val sampleRate: Int,
        val bitDepth: Int,
        val genre: String
    )
}
