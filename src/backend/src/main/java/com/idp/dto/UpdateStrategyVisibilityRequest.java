package com.idp.dto;

import com.idp.model.StrategyVisibility;
import jakarta.validation.constraints.NotNull;

public record UpdateStrategyVisibilityRequest(
    @NotNull StrategyVisibility visibility
) {
}
