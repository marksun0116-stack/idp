package com.idp.dto;

import java.math.BigDecimal;
import java.util.Map;

public record StrategyIndicatorResponse(
    Long strategyId,
    String symbol,
    String range,
    BigDecimal rsi14,
    String trendVerdict,
    String confidence,
    Integer sampleSize,
    BigDecimal medianForwardReturn,
    Map<String, Object> inputs
) {
}
