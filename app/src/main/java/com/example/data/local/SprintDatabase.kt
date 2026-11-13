package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.SprintConfigEntity
import com.example.data.model.SprintTaskEntity

@Database(
    entities = [
        SprintTaskEntity::class,
        SprintConfigEntity::class,
        PomodoroSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(SprintConverters::class)
abstract class SprintDatabase : RoomDatabase() {
    abstract fun sprintDao(): SprintDao

    companion object {
        @Volatile
        private var INSTANCE: SprintDatabase? = null

        fun getInstance(context: Context): SprintDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SprintDatabase::class.java,
                    "sprint_flow.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
