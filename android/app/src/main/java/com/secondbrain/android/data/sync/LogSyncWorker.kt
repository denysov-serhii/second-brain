package com.secondbrain.android.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.secondbrain.android.data.repository.LogRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class LogSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            LogSyncWorkerEntryPoint::class.java
        )
        return runCatching {
            entryPoint.logRepository().syncPendingLogs()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LogSyncWorkerEntryPoint {
    fun logRepository(): LogRepository
}
