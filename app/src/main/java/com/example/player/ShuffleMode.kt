package com.example.player

enum class ShuffleMode {
    OFF,
    STANDARD,
    SMART;

    val isEnabled: Boolean
        get() = this != OFF

    val isSmart: Boolean
        get() = this == SMART

    val label: String
        get() = when (this) {
            OFF -> "Off"
            STANDARD -> "Shuffle"
            SMART -> "Smart Shuffle"
        }

    val description: String
        get() = when (this) {
            OFF -> "Playing in sequential order"
            STANDARD -> "Random shuffle"
            SMART -> "Prioritizing unheard tracks with logical flow"
        }

    fun next(): ShuffleMode = when (this) {
        OFF -> STANDARD
        STANDARD -> SMART
        SMART -> OFF
    }
}
