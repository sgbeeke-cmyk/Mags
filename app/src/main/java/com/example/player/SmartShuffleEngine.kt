package com.example.player

import com.example.data.model.Track
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

/**
 * Result of Smart Shuffle ordering, containing the track and the algorithmic rationale.
 */
data class SmartShuffledTrack(
    val track: Track,
    val reasonTag: String,
    val discoveryBoost: Boolean
)

object SmartShuffleEngine {

    /**
     * Reorders [tracks] using the Smart Shuffle algorithm:
     * 1. Prioritizes less-frequently heard tracks (low/zero playCount, long time since last played).
     * 2. Preserves logical consistency (artist pacing, genre continuity, avoiding jarring transitions).
     *
     * If [currentTrack] is provided, it is kept at the front (index 0), and all subsequent tracks
     * are sequenced smart-shuffled from it.
     */
    fun smartShuffle(
        tracks: List<Track>,
        currentTrack: Track? = null,
        now: Long = System.currentTimeMillis(),
        random: Random = Random.Default
    ): List<Track> {
        return smartShuffleWithReasons(tracks, currentTrack, now, random).map { it.track }
    }

    /**
     * Executes Smart Shuffle and provides human-readable algorithmic reasons for each placement.
     */
    fun smartShuffleWithReasons(
        tracks: List<Track>,
        currentTrack: Track? = null,
        now: Long = System.currentTimeMillis(),
        random: Random = Random.Default
    ): List<SmartShuffledTrack> {
        if (tracks.isEmpty()) return emptyList()
        if (tracks.size == 1) {
            return listOf(
                SmartShuffledTrack(
                    track = tracks[0],
                    reasonTag = if (tracks[0].playCount == 0) "Unheard Track" else "Current Track",
                    discoveryBoost = tracks[0].playCount == 0
                )
            )
        }

        // Map original positions for playlist context anchor
        val originalIndices = tracks.mapIndexed { index, track -> track.id to index }.toMap()
        val distinctArtistsCount = tracks.map { it.artist.trim().lowercase() }.distinct().size
        val distinctAlbumsCount = tracks.map { it.album.trim().lowercase() }.distinct().size

        val remainingPool = tracks.toMutableList()
        val result = mutableListOf<SmartShuffledTrack>()

        // 1. Establish the starting track
        val firstTrack: Track
        val firstReason: String
        val firstIsDiscovery: Boolean

        if (currentTrack != null && remainingPool.any { it.id == currentTrack.id }) {
            firstTrack = remainingPool.first { it.id == currentTrack.id }
            remainingPool.removeAll { it.id == currentTrack.id }
            firstReason = "Now Playing"
            firstIsDiscovery = firstTrack.playCount == 0
        } else {
            // Pick an opening track heavily weighted by unheard / low play frequency
            val scoredOpeners = remainingPool.map { candidate ->
                val discovery = calculateDiscoveryScore(candidate, now)
                val weight = discovery.pow(1.5).coerceAtLeast(1.0)
                candidate to weight
            }
            firstTrack = weightedSample(scoredOpeners, random)
            remainingPool.removeAll { it.id == firstTrack.id }
            val (reason, isDisc) = generateOpeningReason(firstTrack)
            firstReason = reason
            firstIsDiscovery = isDisc
        }

        result.add(SmartShuffledTrack(firstTrack, firstReason, firstIsDiscovery))

        // 2. Iteratively pick subsequent tracks balancing discovery and logical cohesion
        var previousTrack = firstTrack
        var secondPreviousTrack: Track? = null

        while (remainingPool.isNotEmpty()) {
            val candidatesWithWeights = remainingPool.map { candidate ->
                val discoveryScore = calculateDiscoveryScore(candidate, now)
                val consistencyScore = calculateConsistencyScore(
                    candidate = candidate,
                    previous = previousTrack,
                    secondPrevious = secondPreviousTrack,
                    candidateOrigIndex = originalIndices[candidate.id] ?: 0,
                    prevOrigIndex = originalIndices[previousTrack.id] ?: 0,
                    distinctArtistsCount = distinctArtistsCount,
                    distinctAlbumsCount = distinctAlbumsCount
                )

                // 60% discovery weighting (prioritizing unheard) + 40% logical consistency
                val compositeScore = (discoveryScore * 0.60 + consistencyScore * 0.40).coerceAtLeast(1.0)
                // Temperature scaling to favor top candidates while maintaining stochastic freshness
                val samplingWeight = compositeScore.pow(1.6)

                candidate to samplingWeight
            }

            val chosenTrack = weightedSample(candidatesWithWeights, random)
            remainingPool.removeAll { it.id == chosenTrack.id }

            val (reasonTag, isDiscovery) = generateTransitionReason(
                track = chosenTrack,
                previous = previousTrack,
                distinctArtistsCount = distinctArtistsCount
            )

            result.add(SmartShuffledTrack(chosenTrack, reasonTag, isDiscovery))

            secondPreviousTrack = previousTrack
            previousTrack = chosenTrack
        }

        return result
    }

    /**
     * Calculates the Discovery Score (0.0 to ~150.0).
     * High for never-played or rarely played tracks, decaying for frequently played items,
     * with penalties for tracks heard very recently in the session.
     */
    fun calculateDiscoveryScore(track: Track, now: Long = System.currentTimeMillis()): Double {
        // Base score inversely proportional to play count
        val playCountBase = when (track.playCount) {
            0 -> 100.0   // Unheard track: Maximum priority
            1 -> 65.0    // Listened once: High discovery priority
            2 -> 45.0    // Listened twice: Moderate
            3 -> 30.0
            4 -> 20.0
            else -> 100.0 / (1.0 + track.playCount * 0.85) // Steep decay for frequently heard songs
        }

        // Recency factor: discourages tracks played recently, boosts forgotten tracks
        val recencyMultiplier = if (track.lastPlayedMs <= 0L) {
            1.25 // Never played: Extra discovery boost
        } else {
            val millisAgo = (now - track.lastPlayedMs).coerceAtLeast(0L)
            val hoursAgo = millisAgo / (1000.0 * 60 * 60)
            when {
                hoursAgo < 0.5 -> 0.15   // Played in last 30 minutes: heavy penalty
                hoursAgo < 2.0 -> 0.35   // Played earlier this session
                hoursAgo < 8.0 -> 0.60   // Played earlier today
                hoursAgo < 24.0 -> 0.85  // Played yesterday
                hoursAgo < 72.0 -> 1.00  // Played a few days ago
                else -> 1.20             // Long-time no play: rediscovery bonus
            }
        }

        return playCountBase * recencyMultiplier
    }

    /**
     * Calculates the Logical Consistency Score (0.0 to ~120.0) relative to the preceding track:
     * - Artist diversity: avoids jarring consecutive duplicate artists.
     * - Album spacing: prevents consecutive tracks from the same album when alternatives exist.
     * - Genre / sonic continuity: encourages smooth aesthetic transitions.
     * - Audio resolution synergy: clusters studio master / hi-res profiles harmoniously.
     * - Original playlist proximity: subtle preservation of curated playlist narrative.
     */
    fun calculateConsistencyScore(
        candidate: Track,
        previous: Track,
        secondPrevious: Track?,
        candidateOrigIndex: Int,
        prevOrigIndex: Int,
        distinctArtistsCount: Int,
        distinctAlbumsCount: Int
    ): Double {
        var baseScore = 50.0

        // 1. Artist Pacing (Anti-Clustering)
        val sameArtistAsPrev = candidate.artist.trim().equals(previous.artist.trim(), ignoreCase = true)
        val sameArtistAsSecondPrev = secondPrevious?.artist?.trim()?.equals(candidate.artist.trim(), ignoreCase = true) == true

        if (distinctArtistsCount > 1) {
            if (sameArtistAsPrev) {
                baseScore *= 0.20 // Heavy suppression for back-to-back same artist
            } else if (sameArtistAsSecondPrev) {
                baseScore *= 0.55 // Mild suppression for artist appearing 2 of 3 tracks
            } else {
                baseScore += 15.0 // Variety bonus
            }
        }

        // 2. Album Spacing
        val sameAlbum = candidate.album.trim().equals(previous.album.trim(), ignoreCase = true)
        if (distinctAlbumsCount > 1) {
            if (sameAlbum) {
                baseScore *= 0.50
            } else {
                baseScore += 10.0
            }
        }

        // 3. Genre & Musical Vibe Continuity
        val candidateGenre = candidate.genre?.trim()?.lowercase().orEmpty()
        val prevGenre = previous.genre?.trim()?.lowercase().orEmpty()

        if (candidateGenre.isNotEmpty() && prevGenre.isNotEmpty()) {
            if (candidateGenre == prevGenre) {
                baseScore += 30.0 // Direct genre harmony
            } else if (areGenresCompatible(candidateGenre, prevGenre)) {
                baseScore += 20.0 // Compatible genre transition
            }
        } else {
            baseScore += 8.0 // Neutral
        }

        // 4. Hi-Res & Audiophile Profile Cohesion
        if (candidate.isHiRes && previous.isHiRes) {
            baseScore += 10.0
        }
        if (candidate.bitDepth == previous.bitDepth && candidate.sampleRate == previous.sampleRate) {
            baseScore += 5.0
        }

        // 5. Original Playlist Proximity (Soft Context Anchor)
        val indexDistance = abs(candidateOrigIndex - prevOrigIndex)
        val playlistContextBonus = max(0.0, 12.0 - indexDistance * 1.2)
        baseScore += playlistContextBonus

        return baseScore.coerceAtLeast(1.0)
    }

    /**
     * Checks if two genre strings share a musical vibe or stylistic family.
     */
    private fun areGenresCompatible(g1: String, g2: String): Boolean {
        if (g1 in g2 || g2 in g1) return true

        val electronicGroup = setOf("synthwave", "electronic", "ambient", "techno", "cyberpunk", "edm", "chillwave")
        val acousticGroup = setOf("acoustic", "folk", "nordic", "ensemble", "classical", "instrumental", "chamber")
        val jazzSoulGroup = setOf("jazz", "soul", "blues", "audiophile", "r&b", "lounge")
        val rockGroup = setOf("rock", "indie", "alternative", "prog")

        val inElectronic = electronicGroup.any { g1.contains(it) } && electronicGroup.any { g2.contains(it) }
        val inAcoustic = acousticGroup.any { g1.contains(it) } && acousticGroup.any { g2.contains(it) }
        val inJazz = jazzSoulGroup.any { g1.contains(it) } && jazzSoulGroup.any { g2.contains(it) }
        val inRock = rockGroup.any { g1.contains(it) } && rockGroup.any { g2.contains(it) }

        return inElectronic || inAcoustic || inJazz || inRock
    }

    /**
     * Generates a descriptive rationale for why this track was placed.
     */
    private fun generateOpeningReason(track: Track): Pair<String, Boolean> {
        return when {
            track.playCount == 0 -> "Unheard Gem • 0 plays" to true
            track.playCount == 1 -> "Rarely Heard • Deep Cut" to true
            else -> "Opening Track" to false
        }
    }

    /**
     * Generates a descriptive rationale for next track transitions.
     */
    private fun generateTransitionReason(
        track: Track,
        previous: Track,
        distinctArtistsCount: Int
    ): Pair<String, Boolean> {
        val candGenre = track.genre?.trim().orEmpty()
        val prevGenre = previous.genre?.trim().orEmpty()

        return when {
            track.playCount == 0 -> "Unheard Gem • 0 plays" to true
            track.playCount in 1..2 -> "Deep Cut • Rare play" to true
            candGenre.isNotEmpty() && prevGenre.isNotEmpty() && candGenre.equals(prevGenre, ignoreCase = true) ->
                "Genre Flow • $candGenre" to false
            distinctArtistsCount > 1 && !track.artist.equals(previous.artist, ignoreCase = true) && track.playCount < 5 ->
                "Fresh Artist • Discovery" to true
            track.isHiRes && previous.isHiRes -> "Master Audio Flow" to false
            else -> "Playlist Harmony" to false
        }
    }

    /**
     * Weighted sampling according to weights.
     */
    private fun weightedSample(candidates: List<Pair<Track, Double>>, random: Random): Track {
        val totalWeight = candidates.sumOf { it.second }.coerceAtLeast(0.0001)
        var target = random.nextDouble() * totalWeight

        for ((track, weight) in candidates) {
            target -= weight
            if (target <= 0.0) {
                return track
            }
        }

        return candidates.last().first
    }
}
