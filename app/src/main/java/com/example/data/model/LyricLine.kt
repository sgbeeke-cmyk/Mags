package com.example.data.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class LyricsData(
    val isSynced: Boolean,
    val lines: List<LyricLine>,
    val plainText: String = ""
)
