package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: String,
    val format: String = "FLAC",
    val sampleRate: Int = 44100,
    val bitDepth: Int = 16,
    val bitrateKbps: Int = 1411,
    val channels: Int = 2,
    val isHiRes: Boolean = false,
    val albumArtUri: String? = null,
    val albumArtRes: Int? = null,
    val lyricsRaw: String? = null,
    val year: String? = null,
    val genre: String? = null,
    val trackNumber: Int? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedMs: Long = 0L,
    val fileSize: Long = 0L
) {
    fun toTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        uri = uri,
        format = format,
        sampleRate = sampleRate,
        bitDepth = bitDepth,
        bitrateKbps = bitrateKbps,
        channels = channels,
        isHiRes = isHiRes,
        albumArtUri = albumArtUri,
        albumArtRes = albumArtRes,
        lyricsRaw = lyricsRaw,
        year = year,
        genre = genre,
        trackNumber = trackNumber,
        isFavorite = isFavorite,
        playCount = playCount,
        lastPlayedMs = lastPlayedMs,
        fileSize = fileSize
    )

    companion object {
        fun fromTrack(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            uri = track.uri,
            format = track.format,
            sampleRate = track.sampleRate,
            bitDepth = track.bitDepth,
            bitrateKbps = track.bitrateKbps,
            channels = track.channels,
            isHiRes = track.isHiRes,
            albumArtUri = track.albumArtUri,
            albumArtRes = track.albumArtRes,
            lyricsRaw = track.lyricsRaw,
            year = track.year,
            genre = track.genre,
            trackNumber = track.trackNumber,
            isFavorite = track.isFavorite,
            playCount = track.playCount,
            lastPlayedMs = track.lastPlayedMs,
            fileSize = track.fileSize
        )
    }
}
