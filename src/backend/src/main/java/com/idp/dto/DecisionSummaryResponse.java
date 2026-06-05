package com.idp.dto;

import com.idp.model.DecisionStatus;
import com.idp.model.DecisionType;

import java.time.Instant;

public record DecisionSummaryResponse(
    Long id,
    String ticker,
    DecisionType decisionType,
    String title,
    int confidence,
    DecisionStatus status,
    Instant createdAt
) {
}
