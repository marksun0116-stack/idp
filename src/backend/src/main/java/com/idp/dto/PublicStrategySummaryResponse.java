package com.idp.dto;

import java.time.Instant;

public record PublicStrategySummaryResponse(
    Long id,
    String name,
    String description,
    int publicTrackedSymbolCount,
    int transactionCount,
    Instant createdAt
) {
}
