package com.idp.dto;

import com.idp.model.TransactionSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record AppendTransactionRequest(
    @NotBlank String ticker,
    @NotNull TransactionSide side,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
    @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
    Long decisionId,
    @NotNull Instant executedAt
) {
}
