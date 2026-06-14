package com.secondbrain.android.data.repository

import android.content.Context
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
import java.io.IOException
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source

class LogRepository(
    private val context: Context,
    private val logDao: LogDao,
    private val workManager: WorkManager,
    private val logApiService: LogApiService
) {
    companion object {
        private const val MAX_UPLOAD_SIZE_BYTES = 25L * 1024L * 1024L
    }

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
        var hasFailures = false
        val pendingLogs = logDao.getPendingLogs()
        pendingLogs.forEach { log ->
            runCatching {
                val response = logApiService.ingestLog(
                    rawContent = log.rawContent?.toRequestBody("text/plain".toMediaType()),
                    sourceDevice = log.sourceDevice.toRequestBody("text/plain".toMediaType()),
                    file = createFilePart(log.localFileUri)
                )
                if (!response.isSuccessful) {
                    throw IOException("Sync failed for ${log.id}: HTTP ${response.code()}")
                }
                syncedIds += log.id
            }.onFailure {
                hasFailures = true
            }
        }
        if (syncedIds.isNotEmpty()) {
            logDao.markAsSynced(syncedIds)
        }
        if (hasFailures) {
            throw IOException("One or more logs failed to sync")
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

    private fun createFilePart(fileUri: String?): MultipartBody.Part? {
        if (fileUri.isNullOrBlank()) {
            return null
        }
        val uri = Uri.parse(fileUri)
        if (uri.scheme !in setOf("content", "file")) {
            return null
        }
        val resolver = context.contentResolver
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val contentLength = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (contentLength > MAX_UPLOAD_SIZE_BYTES) {
            throw IOException("File is too large to sync (max 25MB)")
        }
        val fileName = (uri.lastPathSegment ?: "upload.bin")
            .replace("..", "_")
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "upload.bin" }
        val requestBody = object : RequestBody() {
            override fun contentType() = mimeType.toMediaTypeOrNull()

            override fun contentLength(): Long = contentLength

            override fun writeTo(sink: BufferedSink) {
                resolver.openInputStream(uri)?.use { inputStream ->
                    sink.writeAll(inputStream.source())
                } ?: throw IOException("Unable to open file URI: $uri")
            }
        }
        return MultipartBody.Part.createFormData("file", fileName, requestBody)
    }
}
