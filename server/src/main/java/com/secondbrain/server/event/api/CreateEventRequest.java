package com.secondbrain.server.event.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;

public record CreateEventRequest(
        @NotNull(message = "Timestamp is required")
        Instant timestamp,

        @NotBlank(message = "Level is required")
        @Pattern(regexp = "INFO|WARN|ERROR", message = "Level must be INFO, WARN, or ERROR")
        String level,

        @NotBlank(message = "Message is required")
        String message
) {}
