package com.secondbrain.android.data.mapper

import com.secondbrain.android.data.local.LocalLogEntity
import com.secondbrain.android.data.remote.RemoteLogDto
import com.secondbrain.android.domain.model.Log
import java.time.Instant

fun LocalLogEntity.toDomain(): Log = Log(
    id = id,
    rawContent = rawContent,
    extractedText = extractedText,
    summary = summary,
    logType = logType,
    embedding = embedding,
    localFileUri = localFileUri,
    createdAt = createdAt,
    sourceDevice = sourceDevice,
    isSynced = isSynced
)

fun RemoteLogDto.toDomain(): Log = Log(
    id = id,
    rawContent = rawContent,
    extractedText = extractedText,
    summary = summary,
    logType = logType,
    embedding = embedding,
    localFileUri = null,
    createdAt = createdAt?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrDefault(System.currentTimeMillis())
    } ?: System.currentTimeMillis(),
    sourceDevice = sourceDevice,
    isSynced = true
)

fun Log.toLocalEntity(): LocalLogEntity = LocalLogEntity(
    id = id,
    rawContent = rawContent,
    extractedText = extractedText,
    summary = summary,
    logType = logType,
    embedding = embedding,
    localFileUri = localFileUri,
    createdAt = createdAt,
    sourceDevice = sourceDevice,
    isSynced = isSynced
)

fun LocalLogEntity.toRemoteDto(): RemoteLogDto = RemoteLogDto(
    id = id,
    rawContent = rawContent,
    extractedText = extractedText,
    summary = summary,
    logType = logType,
    embedding = embedding,
    sourceDevice = sourceDevice,
    createdAt = Instant.ofEpochMilli(createdAt).toString()
)

