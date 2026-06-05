package com.idp.dto;

import java.time.Instant;
import java.util.List;

public record PublicProfileResponse(
    String handle,
    String displayName,
    String bio,
    PublicReputationResponse reputation,
    List<PublicStrategySummaryResponse> publishedStrategies,
    Instant updatedAt
) {
}
