package com.idp.dto;

import java.util.List;

public record StrategyQuotesResponse(
    Long strategyId,
    List<StrategyQuoteResponse> symbols,
    String dataFreshness
) {
}
