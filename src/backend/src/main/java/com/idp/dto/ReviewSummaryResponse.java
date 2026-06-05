package com.idp.dto;

import com.idp.model.ReviewStatus;
import com.idp.model.ReviewType;

import java.time.Instant;
import java.time.LocalDate;

public record ReviewSummaryResponse(
    Long id,
    Long decisionId,
    ReviewType reviewType,
    LocalDate dueDate,
    ReviewStatus status,
    Instant completedAt
) {
}
