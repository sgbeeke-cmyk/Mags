package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val accentColorHex: String,
    val trackCount: Int
)

@Dao
interface PlaylistDao {

    // --- Playlist Queries & Lifecycle ---

    @Query("""
        SELECT p.id, p.name, p.description, p.createdAt, p.updatedAt, p.accentColorHex,
               COUNT(pt.trackId) AS trackCount
        FROM playlists p
        LEFT JOIN playlist_tracks pt ON p.id = pt.playlistId
        GROUP BY p.id
        ORDER BY p.updatedAt DESC, p.createdAt DESC
    """)
    fun getAllPlaylistsWithCount(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :newName, updatedAt = :updatedAt WHERE id = :playlistId")
    suspend fun renamePlaylist(
        playlistId: Long,
        newName: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE playlists SET updatedAt = :updatedAt WHERE id = :playlistId")
    suspend fun touchPlaylist(
        playlistId: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    // --- Track Retrieval in Preserved Order ---

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.orderIndex ASC, pt.addedAt ASC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.orderIndex ASC, pt.addedAt ASC
    """)
    suspend fun getTracksForPlaylistList(playlistId: Long): List<TrackEntity>

    @Query("""
        SELECT * FROM playlist_tracks
        WHERE playlistId = :playlistId
        ORDER BY orderIndex ASC, addedAt ASC
    """)
    suspend fun getPlaylistTrackCrossRefs(playlistId: Long): List<PlaylistTrackCrossRef>

    @Query("""
        SELECT trackId FROM playlist_tracks
        WHERE playlistId = :playlistId
        ORDER BY orderIndex ASC, addedAt ASC
    """)
    suspend fun getTrackIdsForPlaylist(playlistId: Long): List<String>

    @Query("SELECT MAX(orderIndex) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxOrderIndex(playlistId: Long): Int?

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: String): Boolean

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getTrackCountForPlaylist(playlistId: Long): Int

    // --- Adding Tracks ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTracksToPlaylist(crossRefs: List<PlaylistTrackCrossRef>)

    /**
     * Appends a track to the end of the specified playlist with the next sequential orderIndex.
     * If the track already exists in the playlist, it moves it to the end.
     */
    @Transaction
    suspend fun addTrackToEnd(playlistId: Long, trackId: String): Int {
        val maxOrder = getMaxOrderIndex(playlistId) ?: -1
        val nextOrder = maxOrder + 1
        addTrackToPlaylist(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                orderIndex = nextOrder,
                addedAt = System.currentTimeMillis()
            )
        )
        touchPlaylist(playlistId)
        return nextOrder
    }

    /**
     * Appends multiple tracks to the end of the specified playlist in sequential order.
     */
    @Transaction
    suspend fun addTracksToEnd(playlistId: Long, trackIds: List<String>) {
        if (trackIds.isEmpty()) return
        var nextOrder = (getMaxOrderIndex(playlistId) ?: -1) + 1
        val now = System.currentTimeMillis()
        val crossRefs = trackIds.mapIndexed { offset, trackId ->
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                orderIndex = nextOrder + offset,
                addedAt = now + offset
            )
        }
        addTracksToPlaylist(crossRefs)
        touchPlaylist(playlistId)
    }

    // --- Removing Tracks ---

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    /**
     * Removes a track from the playlist and automatically compacts the remaining track
     * orderIndices so they remain consecutive 0, 1, 2, ...
     */
    @Transaction
    suspend fun removeTrackAndCompact(playlistId: Long, trackId: String) {
        removeTrackFromPlaylist(playlistId, trackId)
        normalizePlaylistOrder(playlistId)
        touchPlaylist(playlistId)
    }

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    // --- Reordering Tracks ---

    @Query("UPDATE playlist_tracks SET orderIndex = :newOrder WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun updateTrackOrderIndex(playlistId: Long, trackId: String, newOrder: Int)

    /**
     * Reorders all tracks in the playlist according to the provided list of track IDs.
     * Ensures consecutive orderIndices from 0 to N-1.
     */
    @Transaction
    suspend fun reorderTracks(playlistId: Long, orderedTrackIds: List<String>) {
        orderedTrackIds.forEachIndexed { index, trackId ->
            updateTrackOrderIndex(playlistId, trackId, index)
        }
        touchPlaylist(playlistId)
    }

    /**
     * Moves a single track from [fromIndex] to [toIndex] within the playlist.
     * Shifts intermediate items accordingly and updates the database atomically.
     */
    @Transaction
    suspend fun moveTrack(playlistId: Long, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val currentTracks = getPlaylistTrackCrossRefs(playlistId).toMutableList()
        if (fromIndex !in currentTracks.indices || toIndex !in currentTracks.indices) return

        val moved = currentTracks.removeAt(fromIndex)
        currentTracks.add(toIndex, moved)

        currentTracks.forEachIndexed { index, crossRef ->
            if (crossRef.orderIndex != index) {
                updateTrackOrderIndex(playlistId, crossRef.trackId, index)
            }
        }
        touchPlaylist(playlistId)
    }

    /**
     * Swaps the positions of two tracks in the playlist.
     */
    @Transaction
    suspend fun swapTrackPositions(playlistId: Long, trackIdA: String, trackIdB: String) {
        val crossRefs = getPlaylistTrackCrossRefs(playlistId)
        val refA = crossRefs.find { it.trackId == trackIdA } ?: return
        val refB = crossRefs.find { it.trackId == trackIdB } ?: return

        updateTrackOrderIndex(playlistId, trackIdA, refB.orderIndex)
        updateTrackOrderIndex(playlistId, trackIdB, refA.orderIndex)
        touchPlaylist(playlistId)
    }

    /**
     * Re-indexes all tracks in the playlist to have consecutive orderIndices starting at 0.
     */
    @Transaction
    suspend fun normalizePlaylistOrder(playlistId: Long) {
        val crossRefs = getPlaylistTrackCrossRefs(playlistId)
        crossRefs.forEachIndexed { index, ref ->
            if (ref.orderIndex != index) {
                updateTrackOrderIndex(playlistId, ref.trackId, index)
            }
        }
    }
}
