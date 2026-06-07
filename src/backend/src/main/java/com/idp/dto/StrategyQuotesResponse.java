package com.idp.dto;

import java.util.List;
import java.math.BigDecimal;

public record StrategyQuotesResponse(
    Long strategyId,
    List<StrategyQuoteResponse> symbols,
    BigDecimal startingCapital,
    BigDecimal cashBalance,
    BigDecimal holdingsValue,
    BigDecimal totalStrategyValue,
    BigDecimal totalGain,
    BigDecimal totalGainPct,
    String dataFreshness
) {
}
