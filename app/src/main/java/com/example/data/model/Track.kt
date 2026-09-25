package com.example.data.model

data class Track(
    val id: String,
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
    val audioQualityBadge: String
        get() = when {
            sampleRate >= 96000 && bitDepth >= 24 -> "FLAC 24-bit / ${sampleRate / 1000}kHz Studio Master"
            sampleRate >= 48000 && bitDepth >= 24 -> "FLAC 24-bit / ${sampleRate / 1000}kHz Hi-Res"
            isHiRes -> "FLAC Hi-Res Lossless"
            format.equals("FLAC", ignoreCase = true) -> "FLAC ${bitDepth}-bit / ${(sampleRate / 1000.0).toString().trimEnd('0').trimEnd('.')}kHz Lossless"
            else -> "$format Lossless"
        }
}
