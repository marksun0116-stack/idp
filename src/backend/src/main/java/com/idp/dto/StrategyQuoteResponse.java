package com.idp.dto;

import java.math.BigDecimal;

public record StrategyQuoteResponse(
    String symbol,
    BigDecimal lastPrice,
    BigDecimal change,
    BigDecimal percentChange,
    Long marketTime,
    String marketState,
    Long volume,
    BigDecimal positionWeight,
    String trackingStatus
) {
}
