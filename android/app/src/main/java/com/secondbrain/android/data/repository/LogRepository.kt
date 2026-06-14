package com.secondbrain.android.data.repository

import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.secondbrain.android.data.local.LocalLogEntity
import com.secondbrain.android.data.local.LogDao
import com.secondbrain.android.data.sync.LogSyncWorker
import java.util.UUID

class LogRepository(
    private val logDao: LogDao,
    private val workManager: WorkManager
) {
    suspend fun saveAndSyncLog(text: String?, fileUri: Uri?, type: String) {
        val logId = UUID.randomUUID().toString()
        val createdAt = System.currentTimeMillis()
        val localLog = LocalLogEntity(
            id = logId,
            rawContent = text,
            extractedText = null,
            summary = null,
            logType = type,
            embedding = null,
            createdAt = createdAt,
            sourceDevice = "ANDROID"
        )
        logDao.insert(localLog)

        val inputData = Data.Builder()
            .putString("id", logId)
            .putString("raw_content", text)
            .putString("log_type", type)
            .putString("source_device", "ANDROID")
            .putString("file_uri", fileUri?.toString())
            .build()
        val request = OneTimeWorkRequestBuilder<LogSyncWorker>()
            .setInputData(inputData)
            .build()
        workManager.enqueue(request)
    }
}
