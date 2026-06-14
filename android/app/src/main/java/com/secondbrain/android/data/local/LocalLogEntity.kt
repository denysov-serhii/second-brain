package com.secondbrain.android.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_logs")
data class LocalLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "raw_content")
    val rawContent: String?,
    @ColumnInfo(name = "extracted_text")
    val extractedText: String?,
    @ColumnInfo(name = "summary")
    val summary: String?,
    @ColumnInfo(name = "log_type")
    val logType: String?,
    @ColumnInfo(name = "embedding")
    val embedding: String?,
    @ColumnInfo(name = "local_file_uri")
    val localFileUri: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "source_device")
    val sourceDevice: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean
)
