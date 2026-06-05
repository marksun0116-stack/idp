package com.idp.dto;

import com.idp.model.StrategyVisibility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record StrategyDetailResponse(
    Long id,
    String name,
    String description,
    BigDecimal startingCapital,
    StrategyVisibility visibility,
    List<TrackedSymbolResponse> trackedSymbols,
    List<StrategyTransactionResponse> transactions,
    Instant createdAt
) {
}
