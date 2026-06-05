package com.idp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompleteReviewRequest(
    @NotBlank String outcomeSummary,
    @Min(1) @Max(10) int thesisAccuracy,
    @Min(1) @Max(10) int riskAssessmentAccuracy,
    @NotEmpty List<@NotBlank String> lessonsLearned,
    String nextAction
) {
}
