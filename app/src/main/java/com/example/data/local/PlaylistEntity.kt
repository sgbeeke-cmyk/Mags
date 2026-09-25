package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val accentColorHex: String = "#4DEEEA"
) {
    fun toPlaylist(trackCount: Int = 0): Playlist = Playlist(
        id = id,
        name = name,
        description = description,
        trackCount = trackCount,
        createdAt = createdAt,
        accentColorHex = accentColorHex
    )
}

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: String,
    val orderIndex: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
