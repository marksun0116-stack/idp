package com.idp.dto;

import com.idp.model.DecisionStatus;

import java.time.Instant;

public record CreateDecisionResponse(
    Long id,
    DecisionStatus status,
    Instant createdAt
) {
}

