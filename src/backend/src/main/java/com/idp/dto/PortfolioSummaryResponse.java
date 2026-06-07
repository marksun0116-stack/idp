package com.idp.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryResponse(
    BigDecimal totalValue,
    BigDecimal totalCost,
    BigDecimal totalGain,
    BigDecimal totalGainPct,
    BigDecimal dailyGain,
    BigDecimal dailyGainPct,
    List<PortfolioAccountResponse> accounts
) {
}
