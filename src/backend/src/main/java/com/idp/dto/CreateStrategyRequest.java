package com.idp.dto;

import com.idp.model.StrategyVisibility;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateStrategyRequest(
    @NotBlank String name,
    @NotBlank String description,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal startingCapital,
    @NotNull StrategyVisibility visibility
) {
}
