package com.idp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateDecisionRequest(
    @NotBlank String title,
    @NotBlank String thesis,
    @NotEmpty List<@NotBlank String> evidence,
    @NotEmpty List<@NotBlank String> riskFactors,
    @Min(1) @Max(10) int confidence,
    @NotBlank String timeHorizon,
    @NotEmpty List<@NotBlank String> exitCriteria
) {
}
