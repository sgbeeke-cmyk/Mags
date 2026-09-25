package com.example.data.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val accentColorHex: String = "#4DEEEA"
)
