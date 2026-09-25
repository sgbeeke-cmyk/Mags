package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.EqualizerPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        EqualizerPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun equalizerDao(): EqualizerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_music_player.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                val database = getDatabase(context, scope)
                                seedDatabase(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            // Seed standard presets
            EqualizerPreset.DEFAULT_PRESETS.forEach { preset ->
                database.equalizerDao().insertPreset(
                    EqualizerPresetEntity(
                        name = preset.name,
                        bandLevelsCsv = preset.bandLevels.joinToString(","),
                        bassBoost = preset.bassBoost,
                        virtualizer = preset.virtualizer,
                        isCustom = false
                    )
                )
            }

            // Seed default playlists
            database.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Hi-Res FLAC Master",
                    description = "24-bit 96kHz and studio lossless recordings",
                    accentColorHex = "#4DEEEA"
                )
            )
            database.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Late Night Ambient",
                    description = "Warm low-end, spacious soundscapes and mellow jazz",
                    accentColorHex = "#A07CFE"
                )
            )
            database.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Audiophile Acoustic",
                    description = "Clean strings, pure dynamics and lifelike staging",
                    accentColorHex = "#FFB347"
                )
            )
        }
    }
}
