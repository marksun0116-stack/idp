package com.idp.dto;

import java.math.BigDecimal;

public record StrategyHistoryPointResponse(
    Long timestamp,
    BigDecimal close
) {
}
