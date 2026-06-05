package com.idp.dto;

import java.time.Instant;

public record AppendTransactionResponse(
    Long id,
    Long strategyId,
    Instant createdAt
) {
}
