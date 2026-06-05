package com.idp.dto;

import java.time.Instant;

public record UpdateDecisionResponse(
    Long id,
    Instant updatedAt,
    int revision
) {
}
