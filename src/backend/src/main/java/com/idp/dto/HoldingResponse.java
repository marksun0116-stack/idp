package com.idp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingResponse(
    Long id,
    String symbol,
    String companyName,
    BigDecimal shares,
    BigDecimal costBasis,
    LocalDate purchaseDate,
    BigDecimal manualPrice,
    BigDecimal price,
    BigDecimal value,
    BigDecimal cost,
    BigDecimal gain,
    BigDecimal gainPct,
    BigDecimal dayChange,
    BigDecimal dayChangePct,
    BigDecimal dayGain,
    boolean manualPriceActive
) {
}
