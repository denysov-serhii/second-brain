package com.secondbrain.server.logs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.secondbrain.server.logs.domain.LogType;
import com.secondbrain.server.logs.domain.SourceDevice;
import java.time.Instant;
import java.util.UUID;

public record LogIngestionResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("raw_content") String rawContent,
        @JsonProperty("extracted_text") String extractedText,
        @JsonProperty("summary") String summary,
        @JsonProperty("log_type") LogType logType,
        @JsonProperty("source_device") SourceDevice sourceDevice,
        @JsonProperty("created_at") Instant createdAt
) {
}
