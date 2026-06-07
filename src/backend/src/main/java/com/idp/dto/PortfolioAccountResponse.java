package com.idp.dto;

import com.idp.model.AccountType;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioAccountResponse(
    Long id,
    String name,
    AccountType accountType,
    BigDecimal value,
    BigDecimal cost,
    BigDecimal gain,
    BigDecimal gainPct,
    List<HoldingResponse> holdings
) {
}
