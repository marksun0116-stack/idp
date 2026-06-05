package com.idp.dto;

import com.idp.model.StrategyVisibility;

import java.math.BigDecimal;
import java.time.Instant;

public record StrategySummaryResponse(
    Long id,
    String name,
    BigDecimal startingCapital,
    StrategyVisibility visibility,
    Instant createdAt
) {
}
