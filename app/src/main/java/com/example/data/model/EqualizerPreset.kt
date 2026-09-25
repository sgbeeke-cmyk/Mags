package com.example.data.model

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val gainDb: Float
)

data class EqualizerPreset(
    val id: Long = 0,
    val name: String,
    val bandLevels: List<Float>, // gains in dB, typically -12f to +12f
    val bassBoost: Int = 0,      // 0 to 1000 mB
    val virtualizer: Int = 0,    // 0 to 1000 mB
    val isCustom: Boolean = false
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            EqualizerPreset(
                id = 1,
                name = "Hi-Res Master",
                bandLevels = listOf(2.5f, 1.0f, -0.5f, 2.0f, 3.5f),
                bassBoost = 150,
                virtualizer = 200
            ),
            EqualizerPreset(
                id = 2,
                name = "Audiophile Flat",
                bandLevels = listOf(0f, 0f, 0f, 0f, 0f),
                bassBoost = 0,
                virtualizer = 0
            ),
            EqualizerPreset(
                id = 3,
                name = "Deep Bass Boost",
                bandLevels = listOf(6.0f, 4.0f, 1.0f, 0.0f, 1.5f),
                bassBoost = 600,
                virtualizer = 100
            ),
            EqualizerPreset(
                id = 4,
                name = "Vocal Clarity",
                bandLevels = listOf(-1.5f, 2.0f, 4.5f, 3.0f, 1.0f),
                bassBoost = 50,
                virtualizer = 150
            ),
            EqualizerPreset(
                id = 5,
                name = "Acoustic / Strings",
                bandLevels = listOf(1.5f, 2.5f, 1.0f, 3.5f, 4.0f),
                bassBoost = 100,
                virtualizer = 250
            ),
            EqualizerPreset(
                id = 6,
                name = "Electronic Soundstage",
                bandLevels = listOf(5.0f, 2.5f, -1.0f, 2.5f, 5.0f),
                bassBoost = 400,
                virtualizer = 500
            )
        )
    }
}
