package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EqualizerDao {
    @Query("SELECT * FROM equalizer_presets ORDER BY isCustom ASC, id ASC")
    fun getAllPresets(): Flow<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPresetEntity): Long

    @Query("DELETE FROM equalizer_presets WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomPreset(id: Long)

    @Query("SELECT COUNT(*) FROM equalizer_presets")
    suspend fun getPresetCount(): Int
}
