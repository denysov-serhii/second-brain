package com.secondbrain.android.data.repository

import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import com.secondbrain.android.data.local.LocalLogEntity
import com.secondbrain.android.data.local.LogDao
import com.secondbrain.android.data.mapper.toDomain
import com.secondbrain.android.data.remote.LogApiService
import com.secondbrain.android.data.sync.LogSyncWorker
import com.secondbrain.android.domain.model.Log
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LogRepository(
    private val logDao: LogDao,
    private val workManager: WorkManager,
    private val logApiService: LogApiService
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
            localFileUri = fileUri?.toString(),
            createdAt = createdAt,
            sourceDevice = "ANDROID",
            isSynced = false
        )
        logDao.insert(localLog)

        enqueueSyncWork()
    }

    suspend fun getLogs(): List<Log> {
        return logDao.getAllLogs().map { it.toDomain() }
    }

    suspend fun syncPendingLogs() {
        val syncedIds = mutableListOf<String>()
        val pendingLogs = logDao.getPendingLogs()
        pendingLogs.forEach { log ->
            val response = logApiService.ingestLog(
                rawContent = log.rawContent?.toRequestBody("text/plain".toMediaType()),
                sourceDevice = log.sourceDevice.toRequestBody("text/plain".toMediaType()),
                file = null
            )
            if (response.isSuccessful) {
                syncedIds += log.id
            }
        }
        if (syncedIds.isNotEmpty()) {
            logDao.markAsSynced(syncedIds)
        }
    }

    fun enqueueSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString("source_device", "ANDROID")
            .build()
        val request = OneTimeWorkRequestBuilder<LogSyncWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            "log_sync_work",
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
