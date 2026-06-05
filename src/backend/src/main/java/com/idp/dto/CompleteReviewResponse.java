package com.idp.dto;

import com.idp.model.ReviewStatus;

import java.time.Instant;

public record CompleteReviewResponse(
    Long id,
    ReviewStatus status,
    Instant completedAt
) {
}
