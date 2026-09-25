package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EqualizerPreset

@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bandLevelsCsv: String, // comma separated floats
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val isCustom: Boolean = true
) {
    fun toPreset(): EqualizerPreset = EqualizerPreset(
        id = id,
        name = name,
        bandLevels = bandLevelsCsv.split(",").mapNotNull { it.trim().toFloatOrNull() },
        bassBoost = bassBoost,
        virtualizer = virtualizer,
        isCustom = isCustom
    )

    companion object {
        fun fromPreset(preset: EqualizerPreset): EqualizerPresetEntity = EqualizerPresetEntity(
            id = preset.id,
            name = preset.name,
            bandLevelsCsv = preset.bandLevels.joinToString(","),
            bassBoost = preset.bassBoost,
            virtualizer = preset.virtualizer,
            isCustom = preset.isCustom
        )
    }
}
