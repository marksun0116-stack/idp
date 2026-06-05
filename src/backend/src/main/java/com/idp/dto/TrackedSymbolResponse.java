package com.idp.dto;

import com.idp.model.SymbolVisibility;

import java.time.Instant;
import java.util.List;

public record TrackedSymbolResponse(
    Long strategyId,
    String symbol,
    String trackingStatus,
    String note,
    List<String> tags,
    SymbolVisibility visibility,
    Instant createdAt
) {
}
