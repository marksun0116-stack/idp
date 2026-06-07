package com.idp.dto;

import java.util.List;

public record StrategyHistoryResponse(
    Long strategyId,
    String range,
    List<StrategyHistorySeriesResponse> series,
    List<StrategyPerformancePointResponse> performance
) {
}
