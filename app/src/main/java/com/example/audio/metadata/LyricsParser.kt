package com.example.audio.metadata

import com.example.data.model.LyricLine
import com.example.data.model.LyricsData
import java.util.regex.Pattern

object LyricsParser {
    // Matches [01:23.45] or [01:23] or [1:23.456]
    private val TIME_TAG_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]")

    fun parse(rawLyrics: String?): LyricsData {
        if (rawLyrics.isNullOrBlank()) {
            return LyricsData(isSynced = false, lines = emptyList(), plainText = "")
        }

        val lines = rawLyrics.lines()
        val parsedLines = mutableListOf<LyricLine>()
        var hasTimedTag = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Check for metadata tags like [ti:Title], [ar:Artist], etc.
            if (trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") ||
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") ||
                trimmed.startsWith("[length:")
            ) {
                continue
            }

            val matcher = TIME_TAG_PATTERN.matcher(trimmed)
            val timestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            while (matcher.find()) {
                hasTimedTag = true
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val msString = matcher.group(3)
                val ms = when {
                    msString == null -> 0L
                    msString.length == 1 -> (msString.toLongOrNull() ?: 0L) * 100
                    msString.length == 2 -> (msString.toLongOrNull() ?: 0L) * 10
                    else -> msString.take(3).toLongOrNull() ?: 0L
                }
                val totalMs = (minutes * 60 + seconds) * 1000 + ms
                timestamps.add(totalMs)
                lastMatchEnd = matcher.end()
            }

            val text = trimmed.substring(lastMatchEnd).trim()
            if (timestamps.isNotEmpty()) {
                for (ts in timestamps) {
                    parsedLines.add(LyricLine(timestampMs = ts, text = text))
                }
            } else if (!hasTimedTag) {
                parsedLines.add(LyricLine(timestampMs = 0L, text = trimmed))
            }
        }

        val sortedLines = if (hasTimedTag) {
            parsedLines.sortedBy { it.timestampMs }
        } else {
            parsedLines
        }

        return LyricsData(
            isSynced = hasTimedTag && sortedLines.isNotEmpty(),
            lines = sortedLines,
            plainText = rawLyrics.trim()
        )
    }

    fun findActiveLyricIndex(lyrics: LyricsData, currentPositionMs: Long): Int {
        if (!lyrics.isSynced || lyrics.lines.isEmpty()) return -1

        var activeIndex = -1
        for (i in lyrics.lines.indices) {
            if (lyrics.lines[i].timestampMs <= currentPositionMs) {
                activeIndex = i
            } else {
                break
            }
        }
        return activeIndex
    }
}
