package com.idp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateHoldingRequest(
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal shares,
    @DecimalMin(value = "0.0") BigDecimal costBasis,
    LocalDate purchaseDate,
    @DecimalMin(value = "0.0", inclusive = false) BigDecimal manualPrice
) {
}
