package com.secondbrain.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: LocalLogEntity)

    @Query("SELECT * FROM local_logs ORDER BY created_at DESC")
    suspend fun getAllLogs(): List<LocalLogEntity>

    @Query("SELECT * FROM local_logs WHERE is_synced = 0 ORDER BY created_at ASC")
    suspend fun getPendingLogs(): List<LocalLogEntity>

    @Query("UPDATE local_logs SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
