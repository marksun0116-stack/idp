package com.idp.dto;

import com.idp.model.DecisionStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionDecisionRequest(
    @NotNull DecisionStatus status,
    String reason
) {
}
