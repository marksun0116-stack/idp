package com.idp.dto;

import com.idp.model.DecisionStatus;

import java.time.Instant;

public record TransitionDecisionResponse(
    Long id,
    DecisionStatus status,
    Instant transitionedAt
) {
}
