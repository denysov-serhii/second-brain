package com.secondbrain.android.domain.model

data class Log(
    val id: String,
    val rawContent: String?,
    val extractedText: String?,
    val summary: String?,
    val logType: String?,
    val embedding: String?,
    val localFileUri: String?,
    val createdAt: Long,
    val sourceDevice: String,
    val isSynced: Boolean
)

