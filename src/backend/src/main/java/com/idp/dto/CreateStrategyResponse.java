package com.idp.dto;

import com.idp.model.StrategyVisibility;

import java.time.Instant;

public record CreateStrategyResponse(
    Long id,
    String name,
    StrategyVisibility visibility,
    Instant createdAt
) {
}
