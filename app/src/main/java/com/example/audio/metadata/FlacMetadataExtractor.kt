package com.example.audio.metadata

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.data.model.Track
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FlacMetadataExtractor {
    private const val TAG = "FlacMetadataExtractor"

    data class ExtractedMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val format: String,
        val sampleRate: Int,
        val bitDepth: Int,
        val bitrateKbps: Int,
        val channels: Int,
        val isHiRes: Boolean,
        val lyricsRaw: String?,
        val year: String?,
        val genre: String?,
        val trackNumber: Int?,
        val albumArtUri: String?,
        val fileSize: Long
    )

    fun extract(context: Context, uri: Uri, fallbackTitle: String = "Unknown Track"): ExtractedMetadata {
        var fileSize = 0L
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                fileSize = pfd.statSize
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get file size for $uri", e)
        }

        // Try direct FLAC Vorbis comment parser first
        val flacParsed = tryParseFlacStream(context, uri)

        // Try MediaMetadataRetriever as companion/fallback
        var mmrTitle: String? = null
        var mmrArtist: String? = null
        var mmrAlbum: String? = null
        var mmrDurationMs: Long = 0L
        var mmrBitrate = 0
        var mmrGenre: String? = null
        var mmrYear: String? = null
        var mmrTrackNum: Int? = null
        var artUri: String? = null

        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            mmrTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            mmrArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            mmrAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            mmrDurationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            mmrBitrate = (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0) / 1000
            mmrGenre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            mmrYear = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
            mmrTrackNum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()

            val picture = mmr.embeddedPicture
            if (picture != null) {
                artUri = savePictureToCache(context, uri.toString().hashCode().toString(), picture)
            }
            mmr.release()
        } catch (e: Exception) {
            Log.w(TAG, "MediaMetadataRetriever failed for $uri: ${e.message}")
        }

        val title = flacParsed?.comments?.get("TITLE") ?: mmrTitle ?: fallbackTitle
        val artist = flacParsed?.comments?.get("ARTIST") ?: mmrArtist ?: "Unknown Artist"
        val album = flacParsed?.comments?.get("ALBUM") ?: mmrAlbum ?: "Unknown Album"
        val genre = flacParsed?.comments?.get("GENRE") ?: mmrGenre ?: "Lossless Audio"
        val year = flacParsed?.comments?.get("DATE") ?: flacParsed?.comments?.get("YEAR") ?: mmrYear
        val trackNum = flacParsed?.comments?.get("TRACKNUMBER")?.toIntOrNull() ?: mmrTrackNum

        val sampleRate = flacParsed?.sampleRate?.takeIf { it > 0 } ?: 44100
        val bitDepth = flacParsed?.bitDepth?.takeIf { it > 0 } ?: 16
        val channels = flacParsed?.channels?.takeIf { it > 0 } ?: 2
        val durationMs = if (flacParsed != null && flacParsed.durationMs > 0) {
            flacParsed.durationMs
        } else {
            mmrDurationMs
        }

        // Calculate approximate lossless bitrate if 0: (sampleRate * bitDepth * channels * duration) / 1000 compressed ~60%
        val bitrateKbps = if (mmrBitrate > 0) {
            mmrBitrate
        } else {
            ((sampleRate.toLong() * bitDepth * channels * 0.6) / 1000).toInt()
        }

        val isHiRes = sampleRate > 48000 || bitDepth > 16

        // Embedded lyrics from FLAC Vorbis comments
        val lyricsRaw = flacParsed?.comments?.get("LYRICS")
            ?: flacParsed?.comments?.get("UNSYNCEDLYRICS")
            ?: flacParsed?.comments?.get("SYNCED LYRICS")
            ?: flacParsed?.comments?.get("COMMENT")

        return ExtractedMetadata(
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            format = if (flacParsed != null || uri.toString().endsWith(".flac", ignoreCase = true)) "FLAC" else "Lossless",
            sampleRate = sampleRate,
            bitDepth = bitDepth,
            bitrateKbps = bitrateKbps,
            channels = channels,
            isHiRes = isHiRes,
            lyricsRaw = lyricsRaw,
            year = year,
            genre = genre,
            trackNumber = trackNum,
            albumArtUri = artUri,
            fileSize = fileSize
        )
    }

    private data class FlacParseResult(
        val sampleRate: Int,
        val bitDepth: Int,
        val channels: Int,
        val durationMs: Long,
        val comments: Map<String, String>
    )

    private fun tryParseFlacStream(context: Context, uri: Uri): FlacParseResult? {
        return try {
            val inputStream: InputStream = if (uri.scheme == "file" && uri.path?.contains("/android_asset/") == true) {
                val assetPath = uri.path!!.substringAfter("/android_asset/")
                context.assets.open(assetPath)
            } else {
                context.contentResolver.openInputStream(uri) ?: return null
            }

            inputStream.use { input ->
                val dataInput = DataInputStream(input)

                // 1. Magic number "fLaC"
                val magic = ByteArray(4)
                dataInput.readFully(magic)
                if (String(magic, Charsets.US_ASCII) != "fLaC") {
                    return null
                }

                var sampleRate = 0
                var bitDepth = 16
                var channels = 2
                var durationMs = 0L
                val comments = mutableMapOf<String, String>()

                var isLastBlock = false
                while (!isLastBlock) {
                    val headerByte = dataInput.readUnsignedByte()
                    isLastBlock = (headerByte and 0x80) != 0
                    val blockType = headerByte and 0x7F

                    // 24-bit block length
                    val b1 = dataInput.readUnsignedByte()
                    val b2 = dataInput.readUnsignedByte()
                    val b3 = dataInput.readUnsignedByte()
                    val blockLength = (b1 shl 16) or (b2 shl 8) or b3

                    when (blockType) {
                        0 -> {
                            // STREAMINFO (34 bytes)
                            val streamInfo = ByteArray(blockLength)
                            dataInput.readFully(streamInfo)
                            if (streamInfo.size >= 18) {
                                // Sample rate: bits in bytes 10-12 (20 bits)
                                val b10 = streamInfo[10].toInt() and 0xFF
                                val b11 = streamInfo[11].toInt() and 0xFF
                                val b12 = streamInfo[12].toInt() and 0xFF
                                sampleRate = (b10 shl 12) or (b11 shl 4) or (b12 ushr 4)

                                // Channels: 3 bits (value + 1)
                                channels = ((b12 ushr 1) and 0x07) + 1

                                // Bits per sample: 5 bits (value + 1)
                                val b13 = streamInfo[13].toInt() and 0xFF
                                bitDepth = (((b12 and 0x01) shl 4) or (b13 ushr 4)) + 1

                                // Total samples: 36 bits
                                val totalSamplesHigh = (b13 and 0x0F).toLong()
                                val b14 = streamInfo[14].toLong() and 0xFF
                                val b15 = streamInfo[15].toLong() and 0xFF
                                val b16 = streamInfo[16].toLong() and 0xFF
                                val b17 = streamInfo[17].toLong() and 0xFF
                                val totalSamples = (totalSamplesHigh shl 32) or (b14 shl 24) or (b15 shl 16) or (b16 shl 8) or b17

                                if (sampleRate > 0 && totalSamples > 0) {
                                    durationMs = (totalSamples * 1000L) / sampleRate
                                }
                            }
                        }
                        4 -> {
                            // VORBIS_COMMENT
                            val vorbisBytes = ByteArray(blockLength)
                            dataInput.readFully(vorbisBytes)
                            val buffer = ByteBuffer.wrap(vorbisBytes).order(ByteOrder.LITTLE_ENDIAN)

                            // Vendor string
                            if (buffer.remaining() >= 4) {
                                val vendorLength = buffer.int
                                if (vendorLength in 0..buffer.remaining()) {
                                    buffer.position(buffer.position() + vendorLength)
                                }
                                if (buffer.remaining() >= 4) {
                                    val userCommentListLength = buffer.int
                                    for (i in 0 until userCommentListLength) {
                                        if (buffer.remaining() < 4) break
                                        val length = buffer.int
                                        if (length < 0 || length > buffer.remaining()) break
                                        val commentBytes = ByteArray(length)
                                        buffer.get(commentBytes)
                                        val commentStr = String(commentBytes, Charsets.UTF_8)
                                        val eqIndex = commentStr.indexOf('=')
                                        if (eqIndex > 0) {
                                            val key = commentStr.substring(0, eqIndex).uppercase()
                                            val value = commentStr.substring(eqIndex + 1)
                                            comments[key] = value
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // Skip other blocks (padding, cuesheet, picture, etc.)
                            var toSkip = blockLength.toLong()
                            while (toSkip > 0) {
                                val skipped = dataInput.skip(toSkip)
                                if (skipped <= 0) break
                                toSkip -= skipped
                            }
                        }
                    }
                }

                FlacParseResult(
                    sampleRate = sampleRate,
                    bitDepth = bitDepth,
                    channels = channels,
                    durationMs = durationMs,
                    comments = comments
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error in tryParseFlacStream: ${e.message}")
            null
        }
    }

    private fun savePictureToCache(context: Context, key: String, data: ByteArray): String? {
        return try {
            val cacheDir = File(context.cacheDir, "album_art")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, "art_$key.jpg")
            FileOutputStream(file).use { out ->
                out.write(data)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
