package com.secondbrain.server.event.api;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID streamId,
        Instant timestamp,
        String level,
        String message,
        Instant createdAt
) {}
