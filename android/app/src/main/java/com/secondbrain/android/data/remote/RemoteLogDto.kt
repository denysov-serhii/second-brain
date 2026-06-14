package com.secondbrain.android.data.remote

import com.google.gson.annotations.SerializedName

data class RemoteLogDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("raw_content")
    val rawContent: String?,
    @SerializedName("extracted_text")
    val extractedText: String?,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("log_type")
    val logType: String?,
    @SerializedName("embedding")
    val embedding: String?,
    @SerializedName("source_device")
    val sourceDevice: String,
    @SerializedName("created_at")
    val createdAt: String?
)

