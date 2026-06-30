package com.secondbrain.server.stream.api;

import jakarta.validation.constraints.NotBlank;

public record CreateStreamRequest(
        @NotBlank(message = "Stream name is required")
        String name
) {}
