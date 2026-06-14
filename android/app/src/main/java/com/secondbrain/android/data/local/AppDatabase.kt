package com.secondbrain.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocalLogEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}

