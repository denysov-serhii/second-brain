package com.secondbrain.server.auth.api;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String name,
        String email,
        Instant createdAt
) {}
