package com.secondbrain.android.domain.usecase

import com.secondbrain.android.data.repository.LogRepository
import com.secondbrain.android.domain.model.Log
import javax.inject.Inject

class GetAndSyncLogsUseCase @Inject constructor(
    private val logRepository: LogRepository
) {
    suspend operator fun invoke(forceSync: Boolean = false): List<Log> {
        val logs = logRepository.getLogs()
        if (forceSync || logs.any { !it.isSynced }) {
            logRepository.enqueueSyncWork()
        }
        return logs
    }
}

