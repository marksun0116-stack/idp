package com.idp.dto;

import java.math.BigDecimal;

public record StrategyPerformancePointResponse(
    Long timestamp,
    BigDecimal value,
    BigDecimal cash,
    BigDecimal holdingsValue,
    BigDecimal returnPct
) {
}
