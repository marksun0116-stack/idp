package com.idp.dto;

import com.idp.model.TransactionSide;

import java.math.BigDecimal;
import java.time.Instant;

public record StrategyTransactionResponse(
    Long id,
    Long strategyId,
    String ticker,
    TransactionSide side,
    BigDecimal quantity,
    BigDecimal price,
    Long decisionId,
    Instant executedAt,
    Instant createdAt
) {
}
