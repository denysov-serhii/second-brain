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
    createdAt = createdAt?.let { rawCreatedAt ->
        runCatching { Instant.parse(rawCreatedAt).toEpochMilli() }
            .getOrElse { cause ->
                throw IllegalArgumentException(
                    "Invalid created_at format: $rawCreatedAt. Expected ISO-8601 timestamp.",
                    cause
                )
            }
    } ?: throw IllegalArgumentException(
        "Missing created_at in remote log payload. Expected ISO-8601 timestamp."
    ),
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
