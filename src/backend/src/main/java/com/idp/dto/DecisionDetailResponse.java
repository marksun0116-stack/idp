package com.idp.dto;

import com.idp.model.DecisionStatus;
import com.idp.model.DecisionType;
import com.idp.model.Visibility;

import java.time.Instant;
import java.util.List;

public record DecisionDetailResponse(
    Long id,
    String ticker,
    DecisionType decisionType,
    String title,
    String thesis,
    List<String> evidence,
    List<String> riskFactors,
    int confidence,
    String timeHorizon,
    List<String> exitCriteria,
    Visibility visibility,
    DecisionStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
