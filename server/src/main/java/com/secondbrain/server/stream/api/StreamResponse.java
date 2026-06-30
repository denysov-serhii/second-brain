package com.secondbrain.server.stream.api;

import java.time.Instant;
import java.util.UUID;

public record StreamResponse(
        UUID id,
        String name,
        Instant createdAt
) {}
