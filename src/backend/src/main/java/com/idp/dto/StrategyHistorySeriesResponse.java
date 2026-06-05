package com.idp.dto;

import java.util.List;

public record StrategyHistorySeriesResponse(
    String symbol,
    List<StrategyHistoryPointResponse> data
) {
}
